package org.remote.invocation.starter.network.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author liucheng
 * @create 2018-05-31 10:11
 **/
@Slf4j
@Data
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
    private int loss_connect_time = 0;
    ChannelHandlerContext ctx;
    List<String> msgList = new ArrayList<>();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                loss_connect_time++;
                log.info("Server 30 秒没有接收到客户端的信息了");
                if (loss_connect_time > 30) {
                    log.info("Server 关闭这个不活跃的channel");
                    ctx.channel().close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("Server 收到客户端发来的消息:" + ctx.channel().remoteAddress() + "->Server :" + msg.toString());
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        log.info("Server 服务监听启动");
        Thread threadsendQueue = new Thread(() -> sendQueue());
        threadsendQueue.start();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // 发生异常关闭连接
        cause.printStackTrace();
        ctx.close();
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
