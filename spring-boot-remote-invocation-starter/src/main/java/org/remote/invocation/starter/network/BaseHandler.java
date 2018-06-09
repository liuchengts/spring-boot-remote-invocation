package org.remote.invocation.starter.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.cache.ServiceRoute;

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
    public ObjectMapper objectMapper = new ObjectMapper();
    public List<Object> msgList = new ArrayList<>(); //待处理的消息
    public ChannelHandlerContext ctx; //通讯连接上下文
    public RouteCache routeCache = RouteCache.getInstance(); //路由缓存
    public String HEARTBEAT = "Heartbeat";
    public Long HEARTBEAT_TIME = 3000l;// 心跳固定时长
    public String name; //当前处理器的名称

    /**
     * 处理接收到的消息
     *
     * @param msg 接收到的消息
     */
    public void handlerMsg(Object msg) throws Exception {
        log.info("[" + name + "]收到消息:" + ctx.channel().remoteAddress() + "->msg :" + objToJson(msg));
        if (msg instanceof ServiceRoute) {
            //将接收到的路由消息放入路由缓存
            ServiceRoute route = (ServiceRoute) msg;
            routeCache.addServiceRoute(route.getKey(), route);
        } else if (msg instanceof String) {
            String m = (String) msg;
            if (m.startsWith(HEARTBEAT)) {
                Long time = Long.valueOf(m.replace(HEARTBEAT, ""));
                log.info("[" + name + "]心跳连接维持 " + (System.currentTimeMillis() - time));
                receipt();
            }
        }
    }

    /**
     * 消息回执，保持心跳
     */
    public void receipt() {
        Long time = System.currentTimeMillis();
        try {
            Thread.sleep(HEARTBEAT_TIME);
            ctx.writeAndFlush(HEARTBEAT + time);
        } catch (Exception e) {
            log.error("[" + name + "]心跳连接发送异常", e);
        }

    }

    /**
     * 发送消息
     *
     * @param msg 要发送的消息
     */
    public void sendMsg(Object msg) {
        try {
            if (ctx == null) {
                log.info("消息加入待发送队列");
                msgList.add(msg);
            } else {
                ctx.channel().writeAndFlush(msg);
            }
        } catch (Exception e) {
            msgList.add(msg);
            log.warn("发送异常，消息加入待发送队列", e);
        }
    }

    /**
     * 将对象转换为json
     *
     * @param obj 要转换的对象
     * @return 返回json
     */
    public String objToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[" + name + "]处理消息json转换异常", e);
        }
        return null;
    }

    /**
     * 处理待发送队列
     */
    public void sendQueue() {
        log.info("[" + name + "]待发送消息队列启动");
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
            log.error("[" + name + "]处理消息队列异常", e);
        }
    }
}