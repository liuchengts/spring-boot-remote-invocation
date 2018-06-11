package org.remote.invocation.starter.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * 服务提供者实体模型
 *
 * @author liucheng
 * @create 2018-05-29 16:23
 **/
@Component
@Scope
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producer implements Serializable {

    /**
     * 名称
     */
    String name;

    /**
     * 端口
     */
    Integer port;

    /**
     * 内网ip
     */
    String localIp;

    /**
     * 提供的服务名 service->ServiceBean
     */
    Map<String, ServiceBean> services;

    /**
     * 是否注册
     */
    @Builder.Default
    Boolean isRegister = Boolean.TRUE;
}
