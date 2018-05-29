package org.remote.invocation.starter.config;

import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 调用的配置中心
 *
 * @author liucheng
 * @create 2018-05-29 18:00
 **/
public class InvocationConfig {
    @Autowired
    Producer producer;
    @Autowired
    Consumes consumes;

    public InvocationConfig(){

    }
}
