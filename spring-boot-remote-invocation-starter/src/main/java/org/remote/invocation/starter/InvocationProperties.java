package org.remote.invocation.starter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * 属性配置
 *
 * @author liucheng
 * @create 2018-05-29 16:47
 **/
@Scope
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@Validated
@ConfigurationProperties(prefix = "spring.invocation")
public class InvocationProperties implements Serializable {
    /**
     * 名称
     */
    @NonNull
    String name;

    /**
     * 端口
     */
    @NonNull
    Integer port;

    /**
     * 外网同步的ip,支持多个ip，用英文逗号隔开
     */
    String netSyncIp;

    /**
     * leader通信端口，此配置优先应用
     */
    Integer leaderPort;

    /**
     * 是否注册为生产者
     */
    Boolean isRegister = Boolean.TRUE;
}
