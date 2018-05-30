package org.remote.invocation.starter.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 获得ip
 *
 * @author liucheng
 * @create 2018-05-30 11:18
 **/
public class IPUtils {

    /**
     * 获得所有网卡的地址 Inet4Address
     *
     * @return 网卡地址列表
     */
    public static List<String> getLocalIPList() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                System.out.println(networkInterface.getName());
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        System.out.println(ip);
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
    }


    /**
     * 获得外网ip
     *
     * @return 返回外网ip
     */
    public static String getInternetIP() {
        for (String ip : getLocalIPList()) {
            if (!isIntranetIp(ip)) {
                return ip;
            }
        }
        return "";
    }

    /**
     * 获得内网ip
     *
     * @return 返回内网ip
     */
    public static String getLocalIP() {
        for (String ip : getLocalIPList()) {
            if (isIntranetIp(ip)) {
                return ip;
            }
        }
        return "";
    }

    /**
     * 判断是否为内网IP
     * tcp/ip协议中, 专门保留了三个IP地址区域作为私有地址, 其地址范围如下:
     * 10.0.0.0/8: 10.0.0.0～10.255.255.255
     * 172.16.0.0/12: 172.16.0.0～172.31.255.255
     * 192.168.0.0/16: 192.168.0.0～192.168.255.255
     *
     * @param ip 传入的ip
     * @return false表示是内外，true表示是外网
     */
    private static boolean isIntranetIp(String ip) {
        try {
            if (ip.startsWith("10.") || ip.startsWith("192.168.")) return true;
            // 172.16.x.x～172.31.x.x
            String[] ns = ip.split("\\.");
            int ipSub = Integer.valueOf(ns[0] + ns[1]);
            if (ipSub >= 17216 && ipSub <= 17231) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("外网地址：" + getInternetIP());
        System.out.println("内网地址：" + getLocalIP());
    }

}
