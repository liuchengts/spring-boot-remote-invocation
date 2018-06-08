package org.remote.invocation.starter.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRoute implements Serializable {

    /**
     * 唯一key
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
