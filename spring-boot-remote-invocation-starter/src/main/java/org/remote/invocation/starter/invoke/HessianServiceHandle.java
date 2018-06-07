package org.remote.invocation.starter.invoke;

import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;

/**
 * rpc的调用
 *
 * @author liucheng
 * @create 2018-06-06 10:28
 **/
public class HessianServiceHandle {

    /**
     * 获得一个远程接口实现对象
     *
     * @param interfaceClass 接口
     * @param ip             远程ip
     * @param port           远程调用端口
     * @return 返回obj实例
     */
    public static Object getHessianService(Class interfaceClass, String ip, Integer port) throws MalformedURLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        return factory.create(interfaceClass, getUrl(interfaceClass, ip, port));
    }

    /**
     * 获得远程服务地址
     *
     * @param interfaceClass 接口
     * @param ip             远程ip
     * @param port           远程调用端口
     * @return 返回远程服务地址
     */
    public static String getUrl(Class interfaceClass, String ip, Integer port) {
        return "http://" + ip + ":" + port + "/" + interfaceClass.getSimpleName();
    }
}
