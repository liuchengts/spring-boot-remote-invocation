package org.remote.invocation.starter.network.client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.*;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.network.BaseHandle;
import org.remote.invocation.starter.network.Message;

@Slf4j
public class NodeClient extends BaseHandle implements Handler<AsyncResult<NetSocket>> {
    public NodeClient(Vertx vertx, int leaderPort) {
        this.leaderPort = leaderPort;
        this.vertx = vertx;
    }
    /**
     * 启动客户端
     *
     * @param host 目标host
     */
    public void start(String host) {
        try {
            NetClientOptions options = new NetClientOptions()
                    .setConnectTimeout(10000)
                    .setReconnectAttempts(10)
                    .setReconnectInterval(500);
            NetClient client = vertx.createNetClient(options);
            client.connect(leaderPort, host, this);
        } catch (Exception e) {
            log.error("客户端连接失败", e);
        }
        //初始化公共处理器
        init();
    }

    @Override
    public void handle(AsyncResult<NetSocket> result) {
        if (!result.succeeded()) {
            return;
        }
        NetSocket socket = result.result();
        //存储 NetSocket 实例
        if (!mapNetSocket.containsKey(socket.remoteAddress())) {
            mapNetSocket.put(socket.remoteAddress(), socket);
        }
        socket.handler(buffer -> {
            log.info("客户端接收到内容: " + buffer.toString());
            handlerMsg(buffer.toJsonObject().mapTo(Message.class));
        });
    }
}
