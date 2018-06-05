package org.remote.invocation.starter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.remote.invocation.starter.annotation.EnableInvocationConfiguration;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.scan.ConsumesScan;
import org.remote.invocation.starter.scan.ProducerScan;
import org.remote.invocation.starter.utils.Http;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用的配置中心
 *
 * @author liucheng
 * @create 2018-05-29 18:00
 **/
@Data
public class InvocationConfig {

    ApplicationContext applicationContext;
    ObjectMapper objectMapper;
    Producer producer;
    Consumes consumes;
    ConsumesScan consumesScan;
    ProducerScan producerScan;
    List<Producer> producerInvocationCachelist = new ArrayList<>();
    Map<String, Class> services = new HashMap<>();

    public InvocationConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        getModel();
        initNetwork();
        initScan();
        addressConfig();
        outPrin();
    }

    /**
     * 从spring中获得基础的model
     */
    private void getModel() {
        producer = applicationContext.getBean(Producer.class);
        consumes = applicationContext.getBean(Consumes.class);
        consumesScan = applicationContext.getBean(ConsumesScan.class);
        producerScan = applicationContext.getBean(ProducerScan.class);
        objectMapper = applicationContext.getBean(ObjectMapper.class);
    }

    /**
     * 初始化网络模块
     */
    private void initNetwork() {
        //TODO 这里的代码在网络模块完成后要删除
//        String json = Http.sendPost("http://localhost:8080/producers", null);
//        try {
//            System.out.println("远端提供者服务：" + json);
//            if (StringUtils.isEmpty(json)) {
//                return;
//            }
//            addProducerInvocationCache(objectMapper.readValue(json, Producer.class));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 初始化扫描
     */
    private void initScan() {
        getScanPath();
        producerScan.init(this);
        consumesScan.init(this);
    }

    /**
     * 初始化扫描路径
     */
    public void getScanPath() {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(EnableInvocationConfiguration.class);
        if (beanNames != null) {
            Object object = applicationContext.getBean(beanNames[0]);
            EnableInvocationConfiguration enableInvocationConfiguration = object.getClass().getAnnotation(EnableInvocationConfiguration.class);
            String value = enableInvocationConfiguration.value();
            if (StringUtils.isEmpty(value)) {
                consumes.setScanPath(object.getClass().getPackage().getName());
            } else {
                consumes.setScanPath(value);
            }
            System.out.println("扫描起点：" + consumes.getScanPath());
        }
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
     * 处理远程服务提供，解析成Map<String, ServiceBean> 结构
     */
    public void addProducerInvocationCache(Producer producer) {
        producerInvocationCachelist.add(producer);
        handleProducerInvocationCachelist();
    }

    /**
     * 处理远程服务提供，解析成Map<String, ServiceBean> 结构
     */
    public void handleProducerInvocationCachelist() {
        if (producerInvocationCachelist.isEmpty()) {
            producerInvocationCachelist.add(producer);
        }
        producerInvocationCachelist.forEach(producer -> {
            //TODO 这里暂时没考虑多同serviceName 多实例的情况
            producer.getServices().values().forEach(serviceBean -> {
                serviceBean.getInterfacePath().forEach(name -> {
                    services.put(name, serviceBean.getObjectClass());
                });
            });
        });
    }

    /**
     * 根据服务名获得接口实现
     *
     * @param serviceName 服务名称
     * @return ServiceObject
     */
    public Object getServiceObject(String serviceName) throws IllegalAccessException, InstantiationException {
        if (!services.containsKey(serviceName)) {
            return null;
        }
        return services.get(serviceName).newInstance();
    }

    private void outPrin() {
        System.out.println("初始化invocation资源完成 配置输出：");
        verifyProducerJSON();
        verifyConsumesJSON();
    }

    /**
     * 校验生产者配置结果
     */
    public void verifyProducerJSON() {
        try {
            String producerJson = objectMapper.writeValueAsString(producer);
            System.out.println("producerJson:" + producerJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 校验消费者配置结果
     */
    public void verifyConsumesJSON() {
        try {
            String consumesJson = objectMapper.writeValueAsString(consumes);
            System.out.println("consumesJson:" + consumesJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
