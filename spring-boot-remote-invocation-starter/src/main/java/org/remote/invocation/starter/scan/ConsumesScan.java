package org.remote.invocation.starter.scan;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.invoke.BeanProxy;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.context.ApplicationContext;
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
@Component
public class ConsumesScan {
    transient volatile InvocationConfig invocationConfig;
    transient volatile ApplicationContext applicationContext;

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
                    Set<String> interfacePaths = new HashSet<>();
                    Field[] fields = aClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(InvocationResource.class) && wired(aClass, field)) {
                            interfacePaths.add(field.getName());
                        }
                    }
                    if (!interfacePaths.isEmpty()) {
                        serviceBean.setInterfacePath(interfacePaths);
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
        return invocationConfig.getServiceObject(serviceName);
    }
}
