package org.remote.invocation.starter.network.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author liucheng
 * @create 2018-05-31 10:19
 **/
@Data
public class HeartBeatsClient {
    int port;
    String ip;
    HeartBeatClientHandler heartBeatClientHandler;

    public HeartBeatsClient(int port, String ip) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            heartBeatClientHandler = new HeartBeatClientHandler();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("ping", new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                            p.addLast("decoder", new StringDecoder());
                            p.addLast("encoder", new StringEncoder());
                            p.addLast(heartBeatClientHandler);
                        }
                    });
            ChannelFuture future = b.connect(ip, port).sync();
            System.out.println("Client start at ：" + ip + ":" + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException("创建客户端失败");
        } finally {
            group.shutdownGracefully();
        }
    }
}
