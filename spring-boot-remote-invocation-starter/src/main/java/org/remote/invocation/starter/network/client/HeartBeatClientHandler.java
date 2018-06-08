package org.remote.invocation.starter.network.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liucheng
 * @create 2018-05-31 10:18
 **/
@Slf4j
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",
            CharsetUtil.UTF_8));

    private static final int TRY_TIMES = 3;
    private int currentTime = 0;
    ChannelHandlerContext ctx;
    List<String> msgList = new ArrayList<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        log.info("Client channelActive 激活时间是：" + new Date());
        Thread threadsendQueue = new Thread(() -> sendQueue());
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
                ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
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


    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMsg(String msg) {
        try {
            if (ctx == null) {
                msgList.add(msg);
            } else {
                ctx.channel().writeAndFlush(msg);
            }
        } catch (Exception e) {
            msgList.add(msg);
        }
    }

    /**
     * 处理待发送队列
     */
    private void sendQueue() {
        log.info("待发送消息队列启动");
        try {
            while (true) {
                if (msgList.isEmpty()) {
                    Thread.sleep(1000);
                } else {
                    List<String> del = new ArrayList<>();
                    for (String msg : msgList) {
                        ctx.channel().writeAndFlush(msg);
                        del.add(msg);
                    }
                    msgList.removeAll(del);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
