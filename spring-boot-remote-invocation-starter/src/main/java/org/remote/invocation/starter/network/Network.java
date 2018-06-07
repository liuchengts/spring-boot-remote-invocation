package org.remote.invocation.starter.network;

import org.remote.invocation.starter.network.client.HeartBeatsClient;
import org.remote.invocation.starter.network.server.HeartBeatServer;
import org.remote.invocation.starter.utils.IPUtils;
import org.remote.invocation.starter.utils.OS;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
    final static ExecutorService service = Executors.newCachedThreadPool();

    public Network(int leaderPort) {
        this.leaderPort = leaderPort;
    }

    @Override
    public void run() {
        Thread threadServer = new Thread(() -> heartBeatServerStart(leaderPort));
        threadServer.start();
        Thread threadClient = new Thread(() -> heartBeatClientrStart(leaderPort));
        threadClient.start();
        publishLeader(leaderPort);
    }

    /**
     * 创建leader
     *
     * @param leaderPort 端口
     * @return 返回创建的leader
     */
    public static HeartBeatServer heartBeatServerStart(int leaderPort) {
        HeartBeatServer heartBeatServer = null;
        try {
            heartBeatServer = new HeartBeatServer(leaderPort);
        } catch (Exception e) {
            System.out.println("leader已存在");
        }
        return heartBeatServer;
    }

    /**
     * 创建本机客户端，连接到本地leader
     *
     * @param leaderPort leader端口
     */
    public static void heartBeatClientrStart(int leaderPort) {
        new HeartBeatsClient(leaderPort, "localhost").getHeartBeatClientHandler();
    }

    private static void publishLeader(int leaderPort) {
        Set<String> ipSet = IPUtils.getLocalIPs();
        try {
            final CountDownLatch cdOrder = new CountDownLatch(1);//将军
            final CountDownLatch cdAnswer = new CountDownLatch(ipSet.size());//小兵 10000
            for (String ip : ipSet) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            cdOrder.await(); // 处于等待状态
                            try {
                                if (IPUtils.checkConnected(ip, leaderPort)) {
                                    System.out.println(ip + ":" + leaderPort);
                                    //进行远程发布
                                    new HeartBeatsClient(leaderPort, ip);
                                }
                            } catch (Exception e) {
                                // XXX: handle exception
                                return;
                            }
                            cdAnswer.countDown(); // 任务执行完毕，cdAnswer减1。

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                service.execute(runnable);// 为线程池添加任务
            }
            cdOrder.countDown();//-1
            cdAnswer.await();
        } catch (Exception e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
        service.shutdown();
        System.out.println("线程任务处理完成");
    }


}
