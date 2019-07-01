package org.remote.invocation.starter.invoke.service.impl;

import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.ServiceRoute;
import org.remote.invocation.starter.invoke.service.ManageRpcService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 管理rpc的接口实现，主要提供一些服务暴露的信息
 *
 * @author liucheng
 * @create 2018-06-14 15:39
 **/
@Component
public class ManageRpcServiceImpl implements ManageRpcService {

    RouteCache routeCache = RouteCache.getInstance();
    @Override
    public Map<String, ServiceRoute> findAllRouteCache() {
        return routeCache.getRouteCache();
    }
}
