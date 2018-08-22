package org.remote.invocation.starter.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.remote.invocation.starter.common.MethodBean;

import java.io.Serializable;
import java.util.Set;

/**
 * 路由基础信息
 *
 * @author liucheng
 * @create 2018-06-25 09:40
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cache implements Serializable {
    /**
     * 唯一key
     */
    String key;

    /**
     * 端口
     */
    Integer port;

    /**
     * ip
     */
    String ip;

    /**
     * 服务classPath
     */
    String objectClass;
    /**
     * 服务接口class
     */
    Class interfaceClass;
    /**
     * 服务方法
     */
    Set<MethodBean> methods;

    /**
     * 获得key
     *
     * @return 返回key
     */
    public String getKey() {
        return this.ip + ":" + this.port;
    }
}
