package org.remote.invocation.starter.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由缓存
 *
 * @author liucheng
 * @create 2018-06-08 15:24
 **/
public class RouteCache implements Serializable {

    private static class LazyHolder {
        private static final RouteCache INSTANCE = new RouteCache();
    }

    private RouteCache() {

    }

    public static final RouteCache getInstance() {
        return LazyHolder.INSTANCE;
    }

    static Map<String, ServiceRoute> cache = new ConcurrentHashMap<>();

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
     * @param ipAndPort    key
     * @param serviceRoute 路由服务
     */
    public void addServiceRoute(String ipAndPort, ServiceRoute serviceRoute) {
        if (cache.containsKey(ipAndPort)) {
            ServiceRoute route = cache.get(ipAndPort);
            if (serviceRoute.getVersion() - route.getVersion() < 0) {
                return;
            }
        }
        cache.put(ipAndPort, serviceRoute);
    }

}
