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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liucheng
 * @create 2018-05-31 10:19
 **/
@Data
@Slf4j
public class NetWorkClient extends Thread {
    int port;
    String ip;
    NetWorkClientHandler handler = new NetWorkClientHandler();

    public NetWorkClient(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    @Override
    public void run() throws RuntimeException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            //p.addLast("ping", new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                            p.addLast("decoder", new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                            p.addLast("encoder", new ObjectEncoder());
                            p.addLast(handler);
                        }
                    });
            ChannelFuture future = b.connect(ip, port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException("创建客户端失败" + ip + ":" + port, e);
        } finally {
            group.shutdownGracefully();
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

    /**
     * 发送心跳消息
     */
    public void receipt() {
        handler.receipt();
    }
}
