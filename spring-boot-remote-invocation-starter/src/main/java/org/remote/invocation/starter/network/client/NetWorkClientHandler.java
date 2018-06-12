package org.remote.invocation.starter.network.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.network.BaseHandler;

import java.util.Date;

/**
 * @author liucheng
 * @create 2018-05-31 10:18
 **/
@Slf4j
public class NetWorkClientHandler extends BaseHandler {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.name = this.getClass().getSimpleName();
        log.info("[" + name + "]启动" + ctx.channel().remoteAddress());
        new Thread(this::sendQueue).start();
        new Thread(this::receipt).start();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println(ctx);
        log.info("Client  停止时间是：" + new Date());
    }
}
