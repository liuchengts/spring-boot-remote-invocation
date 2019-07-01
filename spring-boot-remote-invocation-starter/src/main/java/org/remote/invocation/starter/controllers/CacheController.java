package org.remote.invocation.starter.controllers;

import org.remote.invocation.starter.common.ServiceRoute;
import org.remote.invocation.starter.invoke.service.ManageRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 路由缓存接口输出
 *
 * @author liucheng
 * @create 2018-06-05 09:29
 **/
@RestController
@RequestMapping("/route_cache")
public class CacheController {

    @Autowired
    ManageRpcService manageRpcService;

    @RequestMapping("/all")
    public Map<String, ServiceRoute> all() {
        return manageRpcService.findAllRouteCache();
    }

}
