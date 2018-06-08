package org.remote.invocation.starter.network.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.network.BaseHandler;

/**
 * @author liucheng
 * @create 2018-05-31 10:11
 **/
@Slf4j
@Data
public class NetworkServerHandler extends BaseHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                loss_connect_time++;
                log.info("Server 30 秒没有接收到客户端的信息了");
//                if (loss_connect_time > 30) {
//                    log.info("Server 关闭这个不活跃的channel");
//                    ctx.channel().close();
//                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("Server 收到客户端发来的消息:" + ctx.channel().remoteAddress() + "->Server :" + objToJson(msg));
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

}
