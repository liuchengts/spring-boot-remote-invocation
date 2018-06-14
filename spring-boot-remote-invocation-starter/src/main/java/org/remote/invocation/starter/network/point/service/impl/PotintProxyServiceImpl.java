package org.remote.invocation.starter.network.point.service.impl;

import org.remote.invocation.starter.annotation.InvocationService;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.network.point.service.PotintProxyService;
import org.springframework.stereotype.Component;

/**
 * 代理实现
 *
 * @author liucheng
 * @create 2018-06-14 15:57
 **/
@Component
@InvocationService
public abstract class PotintProxyServiceImpl implements PotintProxyService {
    RouteCache routeCache = RouteCache.getInstance();

    static {
        System.out.println("我被初始化了" + void.class);
    }

    @Override
    public Object getPointProxyBean(String interfaceClasssName) {
        System.out.println(interfaceClasssName);
        return "PotintProxyServiceImpl";
    }
}
