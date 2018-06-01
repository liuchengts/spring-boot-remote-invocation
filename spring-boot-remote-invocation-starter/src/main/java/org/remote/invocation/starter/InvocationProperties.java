package org.remote.invocation.starter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * 属性配置
 *
 * @author liucheng
 * @create 2018-05-29 16:47
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 是否注册为生产者
     */
    Boolean isRegister = Boolean.TRUE;
}
