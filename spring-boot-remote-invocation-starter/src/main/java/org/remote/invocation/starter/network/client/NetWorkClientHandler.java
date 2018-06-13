package org.remote.invocation.starter.network.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.network.BaseHandler;
import org.remote.invocation.starter.network.NetWork;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;

/**
 * @author liucheng
 * @create 2018-05-31 10:18
 **/
@Slf4j
public class NetWorkClientHandler extends BaseHandler {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String iP = insocket.getAddress().getHostAddress();
        Integer port = insocket.getPort();
        log.info("Client " + iP + ":" + port + " 停止时间是：" + new Date());
        //重新加载网络模块
        invocationConfig.restartNetwork();
        log.info("leaderServer竞争完成" + System.currentTimeMillis());
    }
}
