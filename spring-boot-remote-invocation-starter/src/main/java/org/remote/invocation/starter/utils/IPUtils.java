package org.remote.invocation.starter.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获得ip
 *
 * @author liucheng
 * @create 2018-05-30 11:18
 **/
@Slf4j
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
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
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
        //优先选择192.168段的地址
        String strIp = "";
        for (String ip : getLocalIPList()) {
            if (isIntranetIp(ip)) {
                strIp = ip;
                if (ip.startsWith("192.168.")) {
                    return ip;
                }
            }
        }
        return strIp;
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

    /**
     * 获得当前内网中所有的ip
     *
     * @return ip列表
     */
    public static Set<String> getLocalIPs() {
        Set<String> ipSet = new LinkedHashSet<>();
        String localIp = getLocalIP();
        String startIp = localIp.substring(0, localIp.lastIndexOf("."));
        for (int i = 1; i < 255; i++) {
            ipSet.add(startIp + "." + i);
        }
        return ipSet;
    }

    /**
     * 用Socket判断网络是否连通
     *
     * @param ip         要判断的ip
     * @param leaderPort 要判断的端口
     * @return true表示连通
     */
    public static boolean checkConnected(String ip, Integer leaderPort) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, leaderPort), 15);
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        return true;
    }

    /**
     * 获得外网ip
     *
     * @return 返回外网ip
     */
    public static String getNetIP() {
        String ip = "";
        String chinaz = "http://ip.chinaz.com";

        StringBuilder inputLine = new StringBuilder();
        String read = "";
        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader( new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            while((read=in.readLine())!=null){
                inputLine.append(read+"\r\n");
            }
            //System.out.println(inputLine.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
        Matcher m = p.matcher(inputLine.toString());
        if(m.find()){
            String ipstr = m.group(1);
            ip = ipstr;
        }
        return ip;
    }

    public static void main(String[] args) {
        log.debug("外网地址：" + getNetIP());
        log.debug("内网地址：" + getLocalIP());
        System.out.println(IPUtils.checkConnected("118.25.41.63", 3399));
//        for (String ip : getLocalIPs()) {
//            log.debug(ip);
//        }
    }

}
