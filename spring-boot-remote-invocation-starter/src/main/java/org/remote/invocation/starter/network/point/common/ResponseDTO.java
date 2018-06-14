package org.remote.invocation.starter.network.point.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.remote.invocation.starter.common.ServiceBean;

import java.io.Serializable;

/**
 * 网关代理返回的模型
 *
 * @author liucheng
 * @create 2018-06-14 16:08
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO implements Serializable {
    /**
     * 消费者代理网关ip
     */
    String consumesPointIp;
    /**
     * 消费者代理网关端口
     */
    Integer consumesPointPort;
    /**
     * 需要的服务
     */
    ServiceBean serviceBean;
}
