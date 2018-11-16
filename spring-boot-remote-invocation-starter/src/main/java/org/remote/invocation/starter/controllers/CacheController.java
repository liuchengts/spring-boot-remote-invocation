package org.remote.invocation.starter.controllers;

import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.scan.ProducerScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 路由缓存接口输出
 *
 * @author liucheng
 * @create 2018-06-05 09:29
 **/
@RestController
@RequestMapping("/route_cache")
public class CacheController {

    RouteCache routeCache = RouteCache.getInstance();

    @RequestMapping("/all")
    public Object all() {
        return routeCache.getRouteCache();
    }

}
