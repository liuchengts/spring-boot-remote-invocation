package org.remote.invocation.starter.network.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.config.InvocationConfig;

import java.util.concurrent.TimeUnit;

/**
 * @author liucheng
 * @create 2018-05-31 10:19
 **/
@Data
@Slf4j
public class NetWorkClient extends Thread {
    int port;
    String ip;
    NetWorkClientHandler handler;
    ChannelFutureListener channelFutureListener;
    Bootstrap bootstrap;

    public NetWorkClient(int port, String ip, InvocationConfig invocationConfig) {
        this.port = port;
        this.ip = ip;
        handler = new NetWorkClientHandler();
        handler.invocationConfig = invocationConfig;
    }

    @Override
    public void run() throws RuntimeException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("ping", new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            p.addLast("decoder", new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                            p.addLast("encoder", new ObjectEncoder());
                            p.addLast(handler);
                        }
                    });
            //设置TCP协议的属性
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_TIMEOUT, 5000);
            initChannelFutureListener();
            doConnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建客户端失败" + ip + ":" + port, e);
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 重连的监听
     */
    private void initChannelFutureListener() {
        channelFutureListener = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    log.info("重新连接服务器成功");
                } else {
                    log.info("重新连接服务器失败");
                    //  3秒后重新连接
                    restartConnect();
                }
            }
        };
    }

    /**
     * 发起连接
     *
     * @throws Exception
     */
    public void doConnect() throws Exception {
        ChannelFuture future = bootstrap.connect(ip, port).sync();
        future.addListener(channelFutureListener);
        future.channel().closeFuture().sync();
    }

    /**
     * 发起重连
     *
     * @throws Exception
     */
    public void restartConnect() throws Exception {
        handler.ctx.channel().eventLoop().schedule(() -> {
            try {
                doConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * 发送消息
     *
     * @param msg 要发送的消息
     */
    public void sendMsg(Object msg) {
        handler.sendMsg(msg);
    }

    /**
     * 请求所有远程服务器发回最新的路由缓存
     */
    public void requestRouteCache() {
        handler.sendMsg(handler.SEIZELEADER);
    }
}
