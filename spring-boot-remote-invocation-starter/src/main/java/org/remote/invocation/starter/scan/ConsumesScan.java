package org.remote.invocation.starter.scan;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 消费者扫描配置
 *
 * @author liucheng
 * @create 2018-06-04 14:16
 **/
@Scope
@Component
public class ConsumesScan {
    volatile InvocationConfig invocationConfig;
    volatile ApplicationContext applicationContext;
    /**
     * 接口class，接口impl远程调用路径集合
     */
    volatile Map<Class, Set<String>> routeCache = new HashMap<>();

    /**
     * 初始化
     */
    public void init(InvocationConfig invocationConfig) {
        this.invocationConfig = invocationConfig;
        this.applicationContext = invocationConfig.getApplicationContext();
        Consumes consumes = invocationConfig.getConsumes();
        if (StringUtils.isEmpty(consumes.getScanPath())) {
            return;
        }
        Map<String, ServiceBean> services = new HashMap<>();
        Set<String> classSet = ReflexUtils.doScan(consumes.getScanPath());
        classSet.forEach(path -> {
            try {
                Class aClass = ReflexUtils.loaderClass(path);
                if (aClass != null) {
                    ServiceBean serviceBean = new ServiceBean();
                    serviceBean.setObjectClass(aClass);
                    Set<Class> interfacePaths = new HashSet<>();
                    Field[] fields = aClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(InvocationResource.class)) {
                            interfacePaths.add(field.getType());
                        }
                    }
                    if (!interfacePaths.isEmpty()) {
                        serviceBean.setInterfaceClasss(interfacePaths);
                        services.put(path, serviceBean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        consumes.setServices(services);

    }

    /**
     * 将远程的提供者加入到路由缓存中
     *
     * @param list 远程的提供者
     */
    public void initRouteCache(List<Producer> list) {
        Consumes consumes = this.getConsumes();
        for (Producer producer : list) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("http://");
            buffer.append(producer.getLocalIp());
            buffer.append(producer.getPort());
            String dns = buffer.toString();
            for (String key : producer.getServices().keySet()) {
                //只需要当前消费者需要的生产者，无关的服务抛弃掉
                if (consumes.getServices().containsKey(key)) {
                    producer.getServices().get(key).getInterfaceClasss().forEach(interfaceClass -> {
                        Set<String> urlSet = new HashSet<>();
                        if (routeCache.containsKey(interfaceClass)) {
                            urlSet = routeCache.get(interfaceClass);
                        }
                        urlSet.add(dns + "/" + interfaceClass.getSimpleName());
                        routeCache.put(interfaceClass, urlSet);
                    });
                }
            }
        }
    }

    /**
     * 代理资源注入
     *
     * @param aClass 要注入的class
     * @param field  class需要远程实现的属性
     * @return 返回成功或者失败
     * @throws IllegalAccessException
     */
    private boolean wired(Class aClass, Field field) throws IllegalAccessException, InstantiationException {
        Class<?> cla = field.getType();
        if (!cla.isInterface()) {
            return false;
        }
        Object obj = applicationContext.getBean(aClass);
        Object vobj = getProducerOBJ(cla.getName());
        field.setAccessible(true);
        field.set(obj, vobj);
        return true;
    }

    /**
     * 配置打印
     */
    public void outPrintConfig() {
        invocationConfig.verifyConsumesJSON();
    }

    /**
     * 获得消费者者
     *
     * @return
     */
    public Consumes getConsumes() {
        return invocationConfig.getConsumes();
    }

    /**
     * 获得消费者对应的生产者实例
     *
     * @return
     */
    public Object getProducerOBJ(String serviceName) throws IllegalAccessException, InstantiationException {
        return null;
    }
}
