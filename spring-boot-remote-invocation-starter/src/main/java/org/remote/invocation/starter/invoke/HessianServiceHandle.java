package org.remote.invocation.starter.invoke;

import com.caucho.hessian.client.HessianProxyFactory;
import org.springframework.lang.NonNull;

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
    @NonNull
    public static Object getHessianService(Class interfaceClass, String ip, Integer port) throws MalformedURLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        return factory.create(interfaceClass, getServiceUrl(interfaceClass, ip, port));
    }

    /**
     * 获得一个远程接口实现对象
     *
     * @param interfaceClass 接口
     * @param hostAndPort    远程ip、调用端
     * @return 返回obj实例
     */
    @NonNull
    public static Object getHessianService(Class interfaceClass, String hostAndPort) throws MalformedURLException {
        HessianProxyFactory factory = new HessianProxyFactory();
        return factory.create(interfaceClass, getServiceUrl(interfaceClass, hostAndPort));
    }

    /**
     * 获得远程服务地址
     *
     * @param interfaceClass 接口
     * @param ip             远程ip
     * @param port           远程调用端口
     * @return 返回远程服务地址
     */
    @NonNull
    public static String getServiceUrl(Class interfaceClass, String ip, Integer port) {
        return "http://" + ip + ":" + port + "/" + interfaceClass.getSimpleName();
    }

    /**
     * 获得远程服务地址
     *
     * @param interfaceClass 接口
     * @param hostAndPort    远程ip、调用端
     * @return 返回远程服务地址
     */
    @NonNull
    public static String getServiceUrl(Class interfaceClass, String hostAndPort) {
        return "http://" + hostAndPort + "/" + interfaceClass.getSimpleName();
    }

}
