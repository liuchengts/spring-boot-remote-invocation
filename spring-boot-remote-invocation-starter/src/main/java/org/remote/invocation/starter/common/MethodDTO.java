package org.remote.invocation.starter.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 方法抽象
 *
 * @author liucheng
 * @create 2018-05-31 14:51
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodDTO implements Serializable {
    /**
     * 可执行方法的包路径
     */
    String objectPath;
    /**
     * 方法名
     */
    String name;
    /**
     * 方法返回类型
     */
    Class returnType;
    /**
     * 入参个数
     */
    Integer parameterCount;
    /**
     * 参数key ->参数类型
     */
    Map<String, Class> parameters;
}
