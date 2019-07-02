package org.remote.invocation.starter.network;

import com.fasterxml.jackson.databind.JavaType;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.ServiceRoute;
import org.remote.invocation.starter.network.client.NodeClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class BaseHandle {
    protected int leaderPort;
    protected Vertx vertx;
    protected static Map<SocketAddress, NetSocket> mapNetSocket = new ConcurrentHashMap<>();//会话实例存储
    RouteCache routeCache = RouteCache.getInstance(); //服务路由缓存
    List<Message> messageQueue = new ArrayList<>(); //待处理的消息队列
    Long MAINTAIN_TIME = 3000l;//路由维护休眠时间
    String name; //当前处理器的名称

    /**
     * 初始化
     */
    public void init() {
        this.name = this.getClass().getSimpleName();
        //客户端启动路由维护
        if (name.startsWith(NodeClient.class.getSimpleName())) {
            new Thread(this::maintain).start();
        }
    }

    /**
     * 处理接收到的消息
     *
     * @param message 接收到的消息
     */
    protected void handlerMsg(Message message) {
        if (message.getInstruction().equals(Message.InstructionEnum.ADD_ROUTE)) {
            //将接收到的路由消息放入路由缓存
            ServiceRoute route = JsonObject.mapFrom(message.getObj()).mapTo(ServiceRoute.class);
            routeCache.addServiceRoute(route);
            //给所有客户端推送消息
            pushAllRouteCache();
        } else if (message.getInstruction().equals(Message.InstructionEnum.SEIZELEADER)) {
            //请求回发当前服务器路由信息
            pushAllRouteCache();
        } else if (message.getInstruction().equals(Message.InstructionEnum.BATCH_ADD_ROUTE)) {
            //批量增加路由缓存
            try {
                List<ServiceRoute> list = JsonObject.mapFrom(message.getObj()).getMap().values()
                        .stream().map(e -> JsonObject.mapFrom(e).mapTo(ServiceRoute.class))
                        .collect(Collectors.toList());
                routeCache.updateRouteCache(list);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    private JavaType getCollectionType(Class collectionClass, Class elementClasses) {
        return Json.mapper.getTypeFactory().constructCollectionType(collectionClass, elementClasses);
    }

    /**
     * 推送路由缓存信息给所有的客户端
     */
    protected void pushAllRouteCache() {
        Map map = routeCache.getRouteCache();
        if (map.isEmpty()) {
            return;
        }
        sendAllMsg(Message.builder()
                .instruction(Message.InstructionEnum.BATCH_ADD_ROUTE)
                .obj(map)
                .time(System.currentTimeMillis())
                .build());
    }

    /**
     * 发送消息
     *
     * @param message 要发送的消息
     */
    public void sendMsg(Message message, int port, String host) {
        Buffer buffer = JsonObject.mapFrom(message).toBuffer();
        SocketAddress socketAddress = SocketAddress.inetSocketAddress(port, host);
        message.setSocketAddress(socketAddress);
        try {
            mapNetSocket.get(socketAddress).write(buffer);
        } catch (Exception e) {
            messageQueue.add(message);
        }

    }

    /**
     * 给所有服务端发送消息
     *
     * @param message 要发送的消息
     */
    public void sendAllMsg(Message message) {
        Buffer buffer = JsonObject.mapFrom(message).toBuffer();
        for (NetSocket socket : mapNetSocket.values()) {
            message.setSocketAddress(socket.remoteAddress());
            try {
                socket.write(buffer);
            } catch (Exception e) {
                messageQueue.add(message);
            }
        }
    }

    /**
     * 维护路由
     */
    private void maintain() {
        log.info("路由维护线程启动");
        while (true) {
            try {
                if (routeCache.checkRoute()) {
                    //移除之后进行广播
                    pushAllRouteCache();
                }
                Thread.sleep(MAINTAIN_TIME);
            } catch (Exception e) {
                log.error("路由维护异常", e);
            }
        }
    }

}
