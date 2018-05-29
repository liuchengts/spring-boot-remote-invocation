package org.remote.invocation.starter.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 服务提供者实体模型
 *
 * @author liucheng
 * @create 2018-05-29 16:23
 **/
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
     * ip
     */
    String ip;

    /**
     * 服务包
     */
    Set<Class> servicePackages;

    /**
     * 是否注册
     */
    @Builder.Default
    Boolean isRegister = Boolean.TRUE;
}
