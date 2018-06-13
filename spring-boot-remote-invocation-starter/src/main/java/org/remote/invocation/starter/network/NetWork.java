package org.remote.invocation.starter.network;

import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.common.ServiceRoute;
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
    static Map<String, NetWorkClient> mapNetworkClient = new ConcurrentHashMap<>();
    NetWorkClient netWorkClientLocal;
    NetWorkServer netWorkServerLeader;

    public NetWork(InvocationConfig invocationConfig) {
        this.invocationConfig = invocationConfig;
        this.leaderPort = invocationConfig.getLeaderPort();
    }


    @Override
    public void run() {
        leaderServerStart(leaderPort);
        leaderClientLocalStart();
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
            netWorkServerLeader = new NetWorkServer(leaderPort, invocationConfig);
            netWorkServerLeader.start();
            while (true) {
                if (netWorkServerLeader.getState().equals(State.WAITING)) {
                    return;
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            log.info("leader已存在");
        }
    }

    /**
     * 竞争leader
     */
    public void seizeLeaderServer() {
        leaderServerStart(leaderPort);
        //告诉所有的远程leader，发来最新的路由信息
        mapNetworkClient.values().forEach(client -> {
            client.requestRouteCache();
        });
        log.info("leaderServer竞争完成" + System.currentTimeMillis());
    }

    /**
     * 创建本机客户端，连接到本地leader
     */
    public void leaderClientLocalStart() {
        String localIp = IPUtils.getLocalIP();
        leaderClientStart(localIp, leaderPort);
        netWorkClientLocal = mapNetworkClient.get(localIp);
    }

    /**
     * 创建本机客户端，连接到远程leader
     *
     * @param leaderPort leader端口
     */
    public boolean leaderClientStart(String ip, int leaderPort) {
        if (mapNetworkClient.containsKey(ip)) {
            log.info(ip + ":" + leaderPort + " Client已存在");
            return true;
        }
        try {
            NetWorkClient netWorkClient = new NetWorkClient(leaderPort, ip, invocationConfig);
            netWorkClient.start();
            while (true) {
                if (netWorkClient.getState().equals(State.WAITING)) {
                    mapNetworkClient.put(ip, netWorkClient);
                    return true;
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将本机leader发布出去
     *
     * @param leaderPort 约定的leader端口
     */
    private void publishLeader(int leaderPort) {
        ExecutorService executor = Executors.newCachedThreadPool();
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

    /**
     * 根据ip获得一个客户端
     *
     * @param ip ip
     * @return 返回客户端
     */
    public NetWorkClient getMapNetworkClient(String ip) {
        if (mapNetworkClient.containsKey(ip)) {
            return mapNetworkClient.get(ip);
        }
        return null;
    }

    /**
     * 根据id删除一个客户端缓存
     *
     * @param ip ip
     */
    public void removeMapNetworkClient(String ip) {
        if (mapNetworkClient.containsKey(ip)) {
            mapNetworkClient.remove(ip);
        }
    }
}
