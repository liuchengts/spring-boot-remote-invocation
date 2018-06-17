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
public class NetWorkServerHandler extends BaseHandler {
    private int loss_connect_time = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                loss_connect_time++;
                if (loss_connect_time > 2) {
//                    log.info("关闭不活跃的channel");
//                    ctx.channel().close();
                } else {
                    receipt();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 发生异常关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
