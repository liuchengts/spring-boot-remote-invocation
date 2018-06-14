package org.remote.invocation.starter.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.ServiceRoute;
import org.remote.invocation.starter.config.InvocationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端处理消息的base
 *
 * @author liucheng
 * @create 2018-06-08 10:31
 **/
@Slf4j
@Data
@ChannelHandler.Sharable
public abstract class BaseHandler extends ChannelInboundHandlerAdapter {
    public Timer timer;
    public InvocationConfig invocationConfig;
    public ObjectMapper objectMapper = new ObjectMapper();
    public List<Object> msgList = new ArrayList<>(); //待处理的消息
    public ChannelHandlerContext ctx; //通讯连接上下文
    public RouteCache routeCache = RouteCache.getInstance(); //服务路由缓存
    public String HEARTBEAT = "Heartbeat";//指令：心跳连接
    public String SEIZELEADER = "SeizeLeader"; //指令：所有通讯端口回发最新的路由缓存
    public Long MAINTAIN_TIME = 3000l;//路由维护休眠时间
    public String name; //当前处理器的名称

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.name = this.getClass().getSimpleName() + ctx.channel().remoteAddress();
        log.info("[" + name + "]启动");
        new Thread(this::sendQueue).start();
        new Thread(this::maintain).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            this.handlerMsg(msg);
        } catch (Exception e) {
            log.error("[" + name + "]消息处理异常", e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


    /**
     * 处理接收到的消息
     *
     * @param msg 接收到的消息
     */
    public void handlerMsg(Object msg) throws Exception {
        if (msg instanceof ServiceRoute) {
            //将接收到的路由消息放入路由缓存
            ServiceRoute route = (ServiceRoute) msg;
            routeCache.addServiceRoute(route);
            //给所有客户端推送消息
            pushAllRouteCache();
        } else if (msg instanceof String) {
            String m = (String) msg;
            if (m.startsWith(HEARTBEAT)) {
                Long time = Long.valueOf(m.replace(HEARTBEAT, ""));
                log.info("[" + name + "]心跳连接维持 " + (System.currentTimeMillis() - time));
            } else if (m.startsWith(SEIZELEADER)) {
                //请求回发当前服务器路由信息
                log.info("收到回发路由请求");
                pushAllRouteCache();
            }
        } else if (msg instanceof ConcurrentHashMap) {
            //批量增加路由缓存
            routeCache.updateRouteCache((Map<String, ServiceRoute>) msg);
        }
    }


    /**
     * 推送路由缓存信息给所有的客户端
     */
    public void pushAllRouteCache() {
        sendMsg(routeCache.getRouteCache());
    }

    /**
     * 消息回执，保持心跳
     */
    public void receipt() {
        Long time = System.currentTimeMillis();
        try {
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
                if (ctx.channel().isActive()) {
                    ctx.channel().writeAndFlush(msg);
                }
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

    /**
     * 维护路由
     */
    public void maintain() {
        log.info("[" + name + "]路由维护线程启动");
        while (true) {
            try {
                routeCache.checkRoute();
                //移除之后进行广播
                pushAllRouteCache();
                Thread.sleep(MAINTAIN_TIME);
            } catch (Exception e) {
                log.error("[" + name + "]路由维护异常", e);
            }
        }
    }
}
