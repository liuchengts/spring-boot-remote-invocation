package org.remote.invocation.starter.network.server;

import io.netty.channel.ChannelHandlerContext;
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.name = this.getClass().getSimpleName();
        log.info("[" + name + "]启动" + ctx.channel().remoteAddress());
        new Thread(this::sendQueue).start();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 发生异常关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
