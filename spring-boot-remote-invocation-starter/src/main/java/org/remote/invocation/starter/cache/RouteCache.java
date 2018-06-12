package org.remote.invocation.starter.cache;

import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.invoke.HessianServiceHandle;
import org.remote.invocation.starter.invoke.ResourceWired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由缓存
 *
 * @author liucheng
 * @create 2018-06-08 15:24
 **/
@Slf4j
public class RouteCache implements Serializable {
    private static class LazyHolder {
        private static final RouteCache INSTANCE = new RouteCache();
    }

    private RouteCache() {

    }

    public static final RouteCache getInstance() {
        return LazyHolder.INSTANCE;
    }


    //资源注入对象
    ResourceWired resourceWired;

    //ipAndPort ,ServiceRoute
    static Map<String, ServiceRoute> cache = new ConcurrentHashMap<>();

    //interfaceClasss ,hostport,interfaceClasssImpl
    static Map<Class, Map<String, Object>> projects = new ConcurrentHashMap<>();

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
     * 批量更新路由缓存
     */
    public void updateRouteCache(Map<String, ServiceRoute> cacheMap) {
        if (cacheMap == null || cacheMap.isEmpty()) {
            return;
        }
        cacheMap.values().forEach(serviceRoute -> {
            addServiceRoute(serviceRoute);
        });
        log.debug("批量更新路由完成");
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
                log.debug("已存在更新版本的路由，不进行加入：key[" + serviceRoute.getKey() + "]");
                return;
            }
        }
        cache.put(serviceRoute.getKey(), serviceRoute);
        log.debug("加入了一个路由：key[" + serviceRoute.getKey() + "]");
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
            log.debug("加入了一个远程服务缓存： hostAndPort:" + hostAndPort + " interfaceClasss:" + interfaceClasss.getName());
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
}
