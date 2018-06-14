package org.remote.invocation.starter.network.point.service;

/**
 * 代理业务层
 *
 * @author liucheng
 * @create 2018-06-14 15:56
 **/
public interface PotintProxyService {

    /**
     * 根据接口获得一个代理对象
     * @param interfaceClasssName class to string
     * @return 返回代理对象
     */
    Object getPointProxyBean(String interfaceClasssName);

}
