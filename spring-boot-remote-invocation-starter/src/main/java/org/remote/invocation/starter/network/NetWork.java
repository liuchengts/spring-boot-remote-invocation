package org.remote.invocation.starter.network;

import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.cache.ServiceRoute;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.network.client.NetWorkClient;
import org.remote.invocation.starter.network.server.NetWorkServer;
import org.remote.invocation.starter.utils.IPUtils;

import java.util.Map;
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
    final static ExecutorService executor = Executors.newCachedThreadPool();
    static Map<String, NetWorkClient> mapNetworkClient = new ConcurrentHashMap<>();

    public NetWork(InvocationConfig invocationConfig) {
        this.invocationConfig = invocationConfig;
        this.leaderPort = invocationConfig.getLeaderPort();
    }

    @Override
    public void run() {
        leaderServerStart(leaderPort);
        leaderClientLocalStart(leaderPort);
        publishLeader(leaderPort);
        regRoute();
    }

    /**
     * 创建leader
     *
     * @param leaderPort 端口
     * @return 返回创建的leader
     */
    public void leaderServerStart(int leaderPort) {
        try {
            NetWorkServer   networkLeaderServer = new NetWorkServer(leaderPort);
            networkLeaderServer.start();
            while (true) {
                if (networkLeaderServer.getState().equals(State.WAITING)) {
                    return;
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            log.debug("leader已存在");
        }
    }

    /**
     * 创建本机客户端，连接到本地leader
     *
     * @param leaderPort leader端口
     */
    public void leaderClientLocalStart(int leaderPort) {
        leaderClientStart(IPUtils.getLocalIP(), leaderPort);
    }

    /**
     * 创建本机客户端，连接到远程leader
     *
     * @param leaderPort leader端口
     */
    public void leaderClientStart(String ip, int leaderPort) {
        if (mapNetworkClient.containsKey(ip)) {
            log.debug(ip + ":" + leaderPort + " Client已存在");
            return;
        }
        try {
            NetWorkClient netWorkClient = new NetWorkClient(leaderPort, ip);
            netWorkClient.start();
            while (true) {
                if (netWorkClient.getState().equals(State.WAITING)) {
                    mapNetworkClient.put(ip, netWorkClient);
                    return;
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            return;
        }
    }

    /**
     * 将本机leader发布出去
     *
     * @param leaderPort  约定的leader端口
     */
    private void publishLeader(int leaderPort) {
        Set<String> ipSet = IPUtils.getLocalIPs();
        ipSet.remove(IPUtils.getLocalIP());
        try {
            final CountDownLatch cdOrder = new CountDownLatch(1);//将军
            final CountDownLatch cdAnswer = new CountDownLatch(ipSet.size());//小兵 10000
            for (String ip : ipSet) {
                Runnable runnable = () -> {
                    try {
                        cdOrder.await(); // 处于等待状态
                        try {
                            if (IPUtils.checkConnected(ip, leaderPort)) {
                                leaderClientStart(ip, leaderPort);
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
        log.debug("publishLeader结果:" + (mapNetworkClient.size() - 1));
    }

    /**
     * 将本地路由信息推送推送到所有leader
     */
    private void regRoute() {
        ServiceRoute serviceRoute = invocationConfig.getServiceRoute();
        mapNetworkClient.values().forEach(client -> {
            client.sendMsg(serviceRoute);
        });
    }
}
