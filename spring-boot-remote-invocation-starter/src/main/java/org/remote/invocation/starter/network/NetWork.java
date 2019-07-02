package org.remote.invocation.starter.network;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.network.client.NodeClient;
import org.remote.invocation.starter.network.server.LeaderServer;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.concurrent.*;

/**
 * 网络组件
 *
 * @author liucheng
 * @create 2018-06-06 14:00
 **/
@Slf4j
public class NetWork extends Thread {
    InvocationConfig invocationConfig;
    int leaderPort;
    Vertx vertx = Vertx.vertx();
    LeaderServer leaderServer;
    NodeClient nodeClient;
    String localIp;
    boolean isLeader;

    public NetWork(InvocationConfig invocationConfig) {
        this.invocationConfig = invocationConfig;
        this.leaderPort = invocationConfig.getLeaderPort();
    }

    @Override
    public void run() {
        initNetWork();
        leaderServerStart();
        leaderClientLocalStart();
        publishLeader();
        syncRouteCacheAll();
    }


    /**
     * 初始化通讯组件
     */
    private void initNetWork() {
        leaderServer = new LeaderServer(vertx, leaderPort);
        nodeClient = new NodeClient(vertx, leaderPort);
        localIp = IPUtils.getLocalIP();
    }

    /**
     * 竞争leader
     */
    private void leaderServerStart() {
        isLeader = leaderServer.start();
    }

    /**
     * 创建本机客户端，连接到本地leader
     */
    private void leaderClientLocalStart() {
        nodeClient.start(localIp);
    }

    /**
     * 创建连接到远程leader的客户端
     */
    private void publishLeader() {
        if (StringUtils.isEmpty(invocationConfig.getNetSyncIp())) {
            return;
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        Set<String> ipSet = IPUtils.getLocalIPs();
        ipSet.remove(IPUtils.getLocalIP());
        String[] ips = invocationConfig.getNetSyncIp().split(",");
        for (String ip : ips) {
            ipSet.add(ip);
            log.info("add ip " + ip);
        }
        try {
            final CountDownLatch cdOrder = new CountDownLatch(1);
            final CountDownLatch cdAnswer = new CountDownLatch(ipSet.size());
            for (String ip : ipSet) {
                Runnable runnable = () -> {
                    try {
                        cdOrder.await(); // 处于等待状态
                        try {
                            if (IPUtils.checkConnected(ip, leaderPort)) {
                                nodeClient.start(ip);
                            }
                        } catch (Exception e) {
                            // XXX: handle exception
                            return;
                        }
                        cdAnswer.countDown(); // 任务执行完毕，cdAnswer减1。

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                executor.execute(runnable);// 为线程池添加任务
            }
            cdOrder.countDown();//-1
            cdAnswer.await();
        } catch (Exception e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
        executor.shutdown();
    }

    /**
     * 请求同步远程路由信息
     */
    private void syncRouteCacheAll() {
        if (isLeader) {
            //公布新的Leader
        }
        //发送本地路由到远程服务器
        nodeClient.sendAllMsg(Message.builder()
                .instruction(Message.InstructionEnum.ADD_ROUTE)
                .obj(invocationConfig.getServiceRoute())
                .time(System.currentTimeMillis())
                .build());

        //发送指令要求远程服务器响应路由
//        nodeClient.sendAllMsg(Message.builder()
//                .instruction(Message.InstructionEnum.SEIZELEADER)
//                .time(System.currentTimeMillis())
//                .build());
    }
}
