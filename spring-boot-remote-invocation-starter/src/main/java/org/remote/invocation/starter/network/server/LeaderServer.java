package org.remote.invocation.starter.network.server;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.network.BaseHandle;
import org.remote.invocation.starter.network.Message;

import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
public class LeaderServer extends BaseHandle implements Handler<NetSocket> {

    public LeaderServer(Vertx vertx, int leaderPort) {
        this.leaderPort = leaderPort;
        this.vertx = vertx;
    }

    /**
     * 启动服务端
     */
    public boolean start() {
        AtomicBoolean fag = new AtomicBoolean(false);
        try {
            NetServer server = vertx.createNetServer();
            server.connectHandler(this);
            server.listen(leaderPort, res -> {
                if (res.succeeded()) {
                    log.info("服务端启动成功:" + server.actualPort());
                    fag.set(true);
                    //初始化公共处理器
                    init();
                } else {
                    log.info("服务端启动失败,leader已存在");
                }
            });
        } catch (Exception e) {
            log.info("leader已存在");
        }
        return fag.get();
    }

    @Override
    public void handle(NetSocket socket) {
        //存储 NetSocket 实例
        if (!mapNetSocket.containsKey(socket.remoteAddress())) {
            mapNetSocket.put(socket.remoteAddress(), socket);
        }
        socket.handler(buffer -> {
            log.info("服务端接收到内容: " + buffer.toString());
            handlerMsg(buffer.toJsonObject().mapTo(Message.class));
        });
    }
}
