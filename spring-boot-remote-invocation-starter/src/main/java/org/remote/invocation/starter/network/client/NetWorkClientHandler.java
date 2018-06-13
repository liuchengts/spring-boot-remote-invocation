package org.remote.invocation.starter.network.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.network.BaseHandler;
import org.remote.invocation.starter.network.NetWork;
import org.remote.invocation.starter.utils.IPUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author liucheng
 * @create 2018-05-31 10:18
 **/
@Slf4j
public class NetWorkClientHandler extends BaseHandler {

    String ip;
    Integer port;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                receipt();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        ip = insocket.getAddress().getHostAddress();
        port = insocket.getPort();
        log.info("Client " + ip + ":" + port + " 停止时间是：" + new Date());
        System.out.println(IPUtils.checkConnected(ip, port));
        //3s后每隔3s重新连接服务器,直到连接成功
        new Thread(this::leaderClientStart).start();
        seizeLeaderServer();
        System.out.println(IPUtils.checkConnected(ip, port));
    }

    private void seizeLeaderServer() {
        invocationConfig.getNetWork().seizeLeaderServer();
    }

    private void leaderClientStart() {
        NetWork netWork = invocationConfig.getNetWork();
        netWork.removeMapNetworkClient(ip);
        boolean fag = false;
        while (!fag) {
            if (ctx.channel().isActive()) {
                log.info("客户端可以正常连接");
                return;
            }
            try {
                Thread.sleep(3000l);
                fag = netWork.leaderClientStart(ip, port);
            } catch (Exception e) {
                log.error("重新创建客户端失败", e);
            }
        }

    }
}
