package org.remote.invocation.starter.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * 服务路由
 *
 * @author liucheng
 * @create 2018-06-08 09:49
 **/
@Component
@Scope
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRoute implements Serializable {

    /**
     * 唯一key,ip+端口
     */
    String key;
    /**
     * 消费者
     */
    Consumes consumes;

    /**
     * 生产者
     */
    Producer producer;

    /**
     * 版本
     */
    Long version = System.currentTimeMillis();
}
