package org.remote.invocation.starter.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 服务消费者实体模型
 *
 * @author liucheng
 * @create 2018-05-29 17:25
 **/
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
     * 消费接口
     */
    Set<Class> servicePackages;
}
