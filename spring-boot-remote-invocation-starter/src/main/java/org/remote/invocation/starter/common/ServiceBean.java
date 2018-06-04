package org.remote.invocation.starter.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 服务抽象
 *
 * @author liucheng
 * @create 2018-06-01 15:41
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBean implements Serializable {

    /**
     * 服务class
     */
    Class objectClass;
    /**
     * 服务接口路径
     */
    Set<String> interfacePath;
    /**
     * 服务方法
     */
    Set<MethodBean> methods;
}
