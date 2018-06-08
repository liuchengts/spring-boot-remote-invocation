package org.remote.invocation.starter.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端处理消息的base
 *
 * @author liucheng
 * @create 2018-06-08 10:31
 **/
@Slf4j
@Data
public abstract class BaseHandler extends ChannelInboundHandlerAdapter {
    public static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",
            CharsetUtil.UTF_8));
    public ObjectMapper objectMapper = new ObjectMapper();
    public List<Object> msgList = new ArrayList<>();
    public ChannelHandlerContext ctx;
    public int currentTime = 0;
    public int loss_connect_time = 0;


    /**
     * 处理接收到的消息
     * @param msg
     */
    private void handlerMsg(Object msg){

    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMsg(Object msg) {
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
     * 将对象转换为json
     */
    public String objToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 处理待发送队列
     */
    public void sendQueue() {
        log.info("待发送消息队列启动");
        try {
            while (true) {
                if (msgList.isEmpty()) {
                    Thread.sleep(1000);
                } else {
                    List<Object> del = new ArrayList<>();
                    for (Object msg : msgList) {
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
