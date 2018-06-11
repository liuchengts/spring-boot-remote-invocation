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
 * 服务消费者实体模型
 *
 * @author liucheng
 * @create 2018-05-29 17:25
 **/
@Component
@Scope
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consumes implements Serializable {
    /**
     * 名称
     */
    String name;

    /**
     * 消费服务 service->ServiceBean
     */
    Map<String, ServiceBean> services;
    /**
     * 扫描路径
     */
    String scanPath;
}
