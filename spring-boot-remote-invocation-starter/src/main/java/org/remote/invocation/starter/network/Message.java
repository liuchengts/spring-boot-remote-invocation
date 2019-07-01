package org.remote.invocation.starter.network;

import io.vertx.core.net.SocketAddress;
import lombok.*;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    /**
     * 指令
     *
     * @see InstructionEnum
     */
    private InstructionEnum instruction;
    /**
     * 数据
     */
    private Object obj;
    /**
     * 时间戳
     */
    private Long time;
    /***
     * 消息目标地址
     */
    SocketAddress socketAddress;

    /**
     * 指令枚举
     */
    @AllArgsConstructor
    @NoArgsConstructor
    public enum InstructionEnum {
        SEIZELEADER("所有通讯端口响应最新的路由缓存", "seizeLeader"),
        ADD_ROUTE("新增路由信息到本地", "addRoute"),
        BATCH_ADD_ROUTE("批量新增路由信息到本地", "batchAddRoute");


        @Getter
        @Setter
        private String name;


        @Getter
        @Setter
        private String value;
    }
}
