package org.remote.invocation.starter.invoke.service;

import org.remote.invocation.starter.common.ServiceRoute;

import java.util.Map;

/**
 * 管理rpc的接口
 *
 * @author liucheng
 * @create 2018-06-14 15:38
 **/
public interface ManageRpcService {

    /**
     * 获得当前节点缓存的所有路由信息
     * @return 路由信息
     */
    Map<String, ServiceRoute> findAllRouteCache();

    Map<String,String> getIp();
}
