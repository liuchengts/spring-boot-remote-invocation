package org.remote.invocation.starter.config;
import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.annotation.InvocationService;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.context.ApplicationContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 调用的配置中心
 *
 * @author liucheng
 * @create 2018-05-29 18:00
 **/
public class InvocationConfig {
    ApplicationContext applicationContext;
    Producer producer;
    Consumes consumes;

    public InvocationConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        getModel();
        addressConfig();
        producerScan();
    }

    /**
     * 从spring中获得基础的model
     */
    private void getModel() {
        producer = applicationContext.getBean(Producer.class);
        consumes = applicationContext.getBean(Consumes.class);
    }

    /**
     * 绑定ip
     */
    private void addressConfig() {
        //获得当前外网ip
        producer.setIp(IPUtils.getInternetIP());
        //获得当前内网ip
        producer.setLocalIp(IPUtils.getLocalIP());
    }

    /**
     * 获得生产者
     */
    public void producerScan() {
        Set<Class> servicePackages = new HashSet<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(InvocationService.class);
        if (beanNames != null) {
            for (String str : beanNames) {
                servicePackages.add(applicationContext.getBean(str).getClass());
            }
        }
        producer.setServicePackages(servicePackages);
    }

    /**
     * 获得消费者
     */
    public void consumesScan() {
        Set<Class> servicePackages = new HashSet<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(InvocationResource.class);
        if (beanNames != null) {
            for (String str : beanNames) {
                servicePackages.add(applicationContext.getBean(str).getClass());
            }
        }
        consumes.setServicePackages(servicePackages);
    }

}
