package org.remote.invocation.starter.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;


/**
 * @author liucheng
 * @create 2018-05-31 10:15
 **/
@Data
@Slf4j
public class NetworkServer extends Thread {
    int port;
    NetworkServerHandler handler = new NetworkServerHandler();

    public NetworkServer(int port) {
        this.port = port;
    }

    @Override
    public void run() throws RuntimeException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sbs = new ServerBootstrap().group(bossGroup,
                    workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast("decoder", new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                            ch.pipeline().addLast("encoder", new ObjectEncoder());
                            ch.pipeline().addLast(handler);
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口，开始接收进来的连接
            ChannelFuture future = sbs.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            throw new RuntimeException("创建服务端失败" + port, e);
        }
    }

    /**
     * 发送消息
     *
     * @param msg 要发送的消息
     */
    public void sendMsg(Object msg) {
        handler.sendMsg(msg);
    }
}
