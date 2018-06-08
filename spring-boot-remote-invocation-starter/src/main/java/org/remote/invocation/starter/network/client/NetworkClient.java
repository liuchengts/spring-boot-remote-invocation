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
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author liucheng
 * @create 2018-05-31 10:19
 **/
@Data
@Slf4j
public class NetworkClient extends Thread {
    int port;
    String ip;
    NetworkClientHandler handler = new NetworkClientHandler();

    public NetworkClient(int port, String ip) {
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
                            p.addLast("ping", new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                            p.addLast("decoder", new StringDecoder());
                            p.addLast("encoder", new StringEncoder());
                            p.addLast(handler);
                        }
                    });
            ChannelFuture future = b.connect(ip, port).sync();
            log.info("Client start at ：" + ip + ":" + port);
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
