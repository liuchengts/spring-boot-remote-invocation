package org.remote.invocation.starter.scan;

import org.remote.invocation.starter.annotation.InvocationService;
import org.remote.invocation.starter.common.MethodBean;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 生产者扫描配置
 *
 * @author liucheng
 * @create 2018-06-04 14:16
 **/
@Scope
@Component
public class ProducerScan {
    Producer producer;
    ApplicationContext applicationContext;

    /**
     * 初始化
     */
    public void init(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        Producer producer = applicationContext.getBean(Producer.class);
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(InvocationService.class);
        Map<String, ServiceBean> services = new HashMap<>();
        for (String beanPath : beanNames) {
            services.put(beanPath, this.getServiceBean(beanPath));
        }
        producer.setServices(services);
    }

    /**
     * 根据class路径获得ServiceBean
     *
     * @param beanPath 实例路径
     * @return 返回ServiceBean
     */
    private ServiceBean getServiceBean(String beanPath) {
        ServiceBean serviceBean = new ServiceBean();
        try {
            Object object = applicationContext.getBean(beanPath);
            Class objClass = object.getClass();
            Class<?>[] interfaces = objClass.getInterfaces();
            serviceBean.setObjectClass(objClass.getName());
            Set<Class> interfacePaths = new HashSet<>();
            Set<MethodBean> methodBeans = new HashSet<>();
            for (Class<?> inte : interfaces) {
                interfacePaths.add(inte);
                Method[] methods = inte.getDeclaredMethods();
                for (Method method : methods) {
                    methodBeans.add(MethodBean.builder()
                            .name(method.getName())
                            .returnType(method.getReturnType())
                            .parameters(ReflexUtils.handleParameters(method))
                            .parameterCount(method.getParameterCount())
                            .build());
                }
            }
            serviceBean.setInterfaceClasss(interfacePaths);
            serviceBean.setMethods(methodBeans);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serviceBean;
    }

    /**
     * 获得生产者
     *
     * @return
     */
    public Producer getProducer() {
        return producer;
    }

}
