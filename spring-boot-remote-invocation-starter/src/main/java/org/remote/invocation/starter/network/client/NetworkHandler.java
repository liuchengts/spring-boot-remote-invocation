package org.remote.invocation.starter.network.client;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.network.BaseHandler;

import java.util.Date;

/**
 * @author liucheng
 * @create 2018-05-31 10:18
 **/
@Slf4j
public class NetworkHandler extends BaseHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        log.info("Client channelActive 激活时间是：" + new Date());
        Thread threadsendQueue = new Thread(() -> this.sendQueue());
        threadsendQueue.start();
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client channelInactive 停止时间是：" + new Date());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("Client userEventTriggered 循环触发时间：" + new Date());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                log.info("Client currentTime:" + currentTime);
                currentTime++;
                ctx.channel().writeAndFlush(this.HEARTBEAT_SEQUENCE.duplicate());
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;
        log.info("Client channelRead:" + message);
        if (message.equals("Heartbeat")) {
            ctx.write("has read message from server");
            ctx.flush();
        }
        ReferenceCountUtil.release(msg);
    }

}
