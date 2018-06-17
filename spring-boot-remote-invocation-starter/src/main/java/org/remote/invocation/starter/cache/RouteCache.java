package org.remote.invocation.starter.cache;

import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.common.ServiceRoute;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.invoke.HessianServiceHandle;
import org.remote.invocation.starter.invoke.ResourceWired;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 路由缓存
 *
 * @author liucheng
 * @create 2018-06-08 15:24
 **/
@Slf4j
public class RouteCache {
    private static class LazyHolder {
        private static final RouteCache INSTANCE = new RouteCache();
    }

    private RouteCache() {

    }

    public static final RouteCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 服务网络路由 ipAndPort ,ServiceRoute
     */
    static Map<String, ServiceRoute> cache = new ConcurrentHashMap<>();
    /***
     * 服务对象缓存 interfaceClasss ,hostport,interfaceClasssImpl
     */
    static Map<Class, Map<String, Object>> projects = new ConcurrentHashMap<>();
    /***
     * 服务资源的注入工具
     */
    ResourceWired resourceWired;

    public void initRouteCache(InvocationConfig invocationConfig) {
        this.resourceWired = new ResourceWired(invocationConfig);
    }

    /**
     * 获得所有的路由缓存信息
     *
     * @return 路由缓存信息
     */
    public Map<String, ServiceRoute> getRouteCache() {
        return cache;
    }

    /**
     * 批量更新路由缓存，这里只增加
     */
    public void updateRouteCache(Map<String, ServiceRoute> cacheMap) {
        if (cacheMap == null || cacheMap.isEmpty()) {
            return;
        }
        cacheMap.values().forEach(serviceRoute -> {
            addServiceRoute(serviceRoute);
        });
    }

    /**
     * 根据ip+端口获得一个路由服务
     *
     * @param ipAndPort key
     * @return 路由服务
     */
    public ServiceRoute getServiceRoute(String ipAndPort) {
        if (cache.containsKey(ipAndPort)) {
            return cache.get(ipAndPort);
        }
        return null;
    }

    /**
     * 增加一个路由服务到缓存
     *
     * @param serviceRoute 路由服务
     */
    public void addServiceRoute(ServiceRoute serviceRoute) {
        if (StringUtils.isEmpty(serviceRoute.getKey())) {
            return;
        }
        if (cache.containsKey(serviceRoute.getKey())) {
            ServiceRoute route = cache.get(serviceRoute.getKey());
            if (serviceRoute.getVersion() - route.getVersion() <= 0) {
                return;
            }
        }
        //排除掉没有服务的路由
        if (serviceRoute.getProducer().getServices().isEmpty()) {
            return;
        }
        cache.put(serviceRoute.getKey(), serviceRoute);
        log.info("加入了一个路由：key[" + serviceRoute.getKey() + "]");
        //判断可用服务通讯是否正常
        Set<String> ipSet = new HashSet<>();
        ipSet.add(serviceRoute.getProducer().createLocalKey());
        ipSet.add(serviceRoute.getProducer().createNetKey());
        ipSet = checkRouteClassImpl(ipSet);
        //加入服务实例缓存
        Set<String> finalIpSet = ipSet;
        resourceWired.getConsumes().getServices().values().forEach(serviceBean -> {
            serviceBean.getInterfaceClasss().forEach(interfaceClasss -> {
                addProjects(interfaceClasss, finalIpSet);
            });
        });
        resourceWired.wiredConsumes(this);
    }


    /**
     * 增加一个服务缓存
     *
     * @param interfaceClasss 接口class
     * @param ipSet           可用的ip和端口
     */
    public void addProjects(Class interfaceClasss, Set<String> ipSet) {
        if (ipSet.isEmpty()) {
            return;
        }
        Map<String, Object> objectMap = new ConcurrentHashMap<>();
        if (projects.containsKey(interfaceClasss)) {
            objectMap = projects.get(interfaceClasss);
        }
        for (String hostAndPort : ipSet) {
            try {
                objectMap.put(hostAndPort, HessianServiceHandle.getHessianService(interfaceClasss, hostAndPort));
                log.info(interfaceClasss.getSimpleName() + "加入" + hostAndPort);
                projects.put(interfaceClasss, objectMap);
                log.info("加入了一个远程服务缓存： hostAndPort:" + hostAndPort + " interfaceClasss:" + interfaceClasss.getName());
            } catch (MalformedURLException e) {
                log.error("根据路由获得代理对象失败 hostAndPort:" + hostAndPort + " interfaceClasss:" + interfaceClasss.getName());
            }
        }
        log.info(System.currentTimeMillis() + "路由情况 interfaceClasss:" + interfaceClasss.getName());
        for (String key : objectMap.keySet()) {
            log.info(key + " | " + objectMap.get(key));
        }
    }

    /**
     * 获得接口class对应的远程服务实例
     * 网络负载及可用性检测
     *
     * @param interfaceClasss 接口class
     * @return 远程服务对象
     */
    public Object getServiceObjectImpl(Class interfaceClasss) {
        if (projects.containsKey(interfaceClasss)) {
            Map<String, Object> objectMap = projects.get(interfaceClasss);
            if (objectMap.isEmpty()) {
                return null;
            }
            //优先选择本机内网服务
            String localIp = resourceWired.getProducer().getLocalIp();
            String netIp = resourceWired.getProducer().getNetIp();
            Object objDefault = null;
            Object objNet = null;
            for (String key : objectMap.keySet()) {
                if (key.startsWith(localIp)) {
                    objDefault = objectMap.get(key);
                    log.info(interfaceClasss.getSimpleName() + "选择本地服务实例=====key:" + key);
                    break;
                } else if (key.startsWith(netIp)) {
                    objNet = objectMap.get(key);
                }
            }
            if (objDefault == null && objNet == null) {
                objDefault = objectMap.values().iterator().next();
            } else if (objDefault == null && objNet != null) {
                objDefault = objNet;
            }
            return objDefault;
        }
        return null;
    }

    /**
     * 检测路由是否可用
     */
    public boolean checkRoute() {
        if (cache.size() <= 0) {
            return false;
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        Set<ServiceRoute> routeSet = new HashSet<>(cache.values());
        try {
            final CountDownLatch cdOrder = new CountDownLatch(1);//将军
            final CountDownLatch cdAnswer = new CountDownLatch(routeSet.size());//小兵 10000
            for (ServiceRoute route : routeSet) {
                Runnable runnable = () -> {
                    try {
                        cdOrder.await(); // 处于等待状态
                        try {
                            if (IPUtils.checkConnected(route.getProducer().getLocalIp(), route.getProducer().getPort())
                                    || IPUtils.checkConnected(route.getProducer().getNetIp(), route.getProducer().getPort())) {
                                routeSet.remove(route);
                            }
                        } catch (Exception e) {
                            // XXX: handle exception
                            return;
                        }
                        cdAnswer.countDown(); // 任务执行完毕，cdAnswer减1。
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                executor.execute(runnable);// 为线程池添加任务
            }
            cdOrder.countDown();//-1
            cdAnswer.await();
        } catch (Exception e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
        executor.shutdown();
        //routeSet剩下的是需要从路由中移除的
        log.debug("需要移除的路由数量" + routeSet.size());
        removeRouteCache(routeSet);
        return routeSet.size() > 0;
    }

    /**
     * 移除路由信息
     */
    private void removeRouteCache(Set<ServiceRoute> routeSet) {
        if (routeSet.isEmpty()) {
            return;
        }
        routeSet.forEach(serviceRoute -> {
            //移除路由
            cache.remove(serviceRoute.getKey());
            //移除远程对象
            projects.values().forEach(map -> {
                map.remove(serviceRoute.getProducer().getLocalIp() + serviceRoute.getProducer().getPort());
                map.remove(serviceRoute.getProducer().getNetIp() + serviceRoute.getProducer().getPort());
            });
        });
        //重新加载本地的远程资源
        resourceWired.wiredConsumes(this);
        Set<String> rmset = routeSet.stream().map(ServiceRoute::getKey).collect(Collectors.toSet());
        log.info("清除无用的路由完成{}", rmset);
    }

    /**
     * 检查暴露服务的通讯地址是否可用
     *
     * @param ipSet ip+端口的集合
     * @return 返回可用的集合
     */
    private Set<String> checkRouteClassImpl(Set<String> ipSet) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            final CountDownLatch cdOrder = new CountDownLatch(1);//将军
            final CountDownLatch cdAnswer = new CountDownLatch(ipSet.size());//小兵 10000
            for (String hostAndPort : ipSet) {
                Runnable runnable = () -> {
                    try {
                        cdOrder.await(); // 处于等待状态
                        try {
                            String ip = hostAndPort.substring(0, hostAndPort.indexOf(":"));
                            Integer prot = Integer.valueOf(hostAndPort.substring(ip.length() + 1));
                            if (!IPUtils.checkConnected(ip, prot)) {
                                ipSet.remove(hostAndPort);
                            }
                        } catch (Exception e) {
                            // XXX: handle exception
                            return;
                        }
                        cdAnswer.countDown(); // 任务执行完毕，cdAnswer减1。
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                executor.execute(runnable);// 为线程池添加任务
            }
            cdOrder.countDown();//-1
            cdAnswer.await();
        } catch (Exception e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
        executor.shutdown();
        return ipSet;
    }
}

