package org.remote.invocation.starter.network;

import org.remote.invocation.starter.network.client.HeartBeatClient;
import org.remote.invocation.starter.network.server.HeartBeatServer;
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
public class Network extends Thread {
    int leaderPort;
    final static ExecutorService executor = Executors.newCachedThreadPool();
    static Map<String, HeartBeatClient> mapHeartBeatsClient = new ConcurrentHashMap<>();
    HeartBeatServer heartBeatServerLeader;
    HeartBeatClient heartBeatClientLocal;

    public Network(int leaderPort) {
        this.leaderPort = leaderPort;
    }

    @Override
    public void run() {
        heartBeatServerStart(leaderPort);
        heartBeatClientLocalStart(leaderPort);
        heartBeatClientLocal = mapHeartBeatsClient.get(IPUtils.getLocalIP());
        heartBeatServerLeader.sendMsg("服务端测试发送");
        heartBeatClientLocal.sendMsg("测试消息发送");
        publishLeader(leaderPort);
    }

    /**
     * 创建leader
     *
     * @param leaderPort 端口
     * @return 返回创建的leader
     */
    public void heartBeatServerStart(int leaderPort) {
        try {
            heartBeatServerLeader = new HeartBeatServer(leaderPort);
            heartBeatServerLeader.start();
            while (true) {
                if (heartBeatServerLeader.getState().equals(State.WAITING)) {
                    return;
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (Exception e) {
            System.out.println("leader已存在");
        }
    }

    /**
     * 创建本机客户端，连接到远程leader
     *
     * @param leaderPort leader端口
     */
    public void heartBeatClientLocalStart(int leaderPort) {
        heartBeatClientStart(IPUtils.getLocalIP(), leaderPort);
    }

    /**
     * 创建本机客户端，连接到远程leader
     *
     * @param leaderPort leader端口
     */
    public void heartBeatClientStart(String ip, int leaderPort) {
        if (mapHeartBeatsClient.containsKey(ip)) {
            System.out.println(ip + ":" + leaderPort + " Client已存在");
            return;
        }
        try {
            HeartBeatClient heartBeatClient = new HeartBeatClient(leaderPort, ip);
            heartBeatClient.start();
            while (true) {
                if (heartBeatClient.getState().equals(State.WAITING)) {
                    mapHeartBeatsClient.put(ip, heartBeatClient);
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
     * @param leaderPort 约定的leader端口
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
                                heartBeatClientStart(ip, leaderPort);
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
        System.out.println("publishLeader结果:" + (mapHeartBeatsClient.size() - 1));
    }

    /**
     * 推送路由信息
     */
    private void regRoute(){

    }
}
