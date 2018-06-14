package org.remote.invocation.starter.cache;

import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.common.ServiceRoute;
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

    //ipAndPort ,ServiceRoute
    static Map<String, ServiceRoute> cache = new ConcurrentHashMap<>();
    //interfaceClasss ,hostport,interfaceClasssImpl
    static Map<Class, Map<String, Object>> projects = new ConcurrentHashMap<>();
    //资源注入对象
    ResourceWired resourceWired;

    public void initRouteCache(ResourceWired resourceWired) {
        this.resourceWired = resourceWired;
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
        log.info("批量更新路由完成");
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
                log.info("已存在更新版本的路由，不进行加入：key[" + serviceRoute.getKey() + "]");
                return;
            }
        }
        //排除掉没有服务的路由
        if (serviceRoute.getProducer().getServices().isEmpty()) {
            return;
        }
        cache.put(serviceRoute.getKey(), serviceRoute);
        log.info("加入了一个路由：key[" + serviceRoute.getKey() + "]");
        //加入服务实例缓存
        resourceWired.getConsumes().getServices().values().forEach(serviceBean -> {
            serviceBean.getInterfaceClasss().forEach(interfaceClasss -> {
                addProjects(interfaceClasss, serviceRoute.getKey());
            });
        });
    }


    /**
     * 增加一个服务缓存
     *
     * @param interfaceClasss 接口class
     * @param hostAndPort     ip+端口
     */
    public void addProjects(Class interfaceClasss, String hostAndPort) {
        Map<String, Object> objectMap = new ConcurrentHashMap<>();
        if (projects.containsKey(interfaceClasss)) {
            objectMap = projects.get(interfaceClasss);
        }
        try {
            objectMap.put(hostAndPort, HessianServiceHandle.getHessianService(interfaceClasss, hostAndPort));
            projects.put(interfaceClasss, objectMap);
            resourceWired.wiredConsumes(this);
            log.info("加入了一个远程服务缓存： hostAndPort:" + hostAndPort + " interfaceClasss:" + interfaceClasss.getName());
        } catch (MalformedURLException e) {
            log.error("根据路由获得代理对象失败 hostAndPort:" + hostAndPort + " interfaceClasss:" + interfaceClasss.getName());
        }

    }

    /**
     * 获得接口class对应的远程服务实例
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
            //TODO 这里进行负载均衡算法,暂时不实现
            return objectMap.values().iterator().next();
        }
        return null;
    }


    /**
     * 检测路由是否可用
     */
    public void checkRoute() {
        if (cache.size() <= 0) {
            return;
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
                            if (IPUtils.checkConnected(route.getProducer().getLocalIp(), route.getProducer().getPort())) {
                                log.debug("连通的连接" + route.getKey());
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
                map.remove(serviceRoute.getKey());
            });
        });
        //重新加载本地的远程资源
        resourceWired.wiredConsumes(this);
        Set<String> rmset = routeSet.stream().map(ServiceRoute::getKey).collect(Collectors.toSet());
        log.info("清除无用的路由完成{}", rmset);
    }
}
