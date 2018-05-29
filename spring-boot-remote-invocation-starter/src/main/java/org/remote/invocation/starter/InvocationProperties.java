package org.remote.invocation.starter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
@ConfigurationProperties(prefix = "spring.invocation")
public class InvocationProperties {
    /**
     * 名称
     */
    String name;

    /**
     * 扫描路径
     */
    String scanPath;

    /**
     * 端口
     */
    Integer port;
}
