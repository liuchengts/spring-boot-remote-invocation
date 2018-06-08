package org.remote.invocation.starter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.remote.invocation.starter.InvocationProperties;
import org.remote.invocation.starter.annotation.EnableInvocationConfiguration;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.invoke.BeanProxy;
import org.remote.invocation.starter.network.Network;
import org.remote.invocation.starter.network.server.HeartBeatServer;
import org.remote.invocation.starter.scan.ConsumesScan;
import org.remote.invocation.starter.scan.ProducerScan;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

/**
 * 调用的配置中心
 *
 * @author liucheng
 * @create 2018-05-29 18:00
 **/
@Data
public class InvocationConfig {

    ApplicationContext applicationContext;
    InvocationProperties invocationProperties;
    ObjectMapper objectMapper = new ObjectMapper();
    Producer producer;
    Consumes consumes;
    ConsumesScan consumesScan;
    ProducerScan producerScan;
    int leaderPort;

    public InvocationConfig(ApplicationContext applicationContext, InvocationProperties invocationProperties) {
        this.applicationContext = applicationContext;
        this.invocationProperties = invocationProperties;
        getModel();
        initServiceModelConfig();
        initScanPath();
        initScan();
        initBeanProxy();
        initNetwork();
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
    }

    /**
     * 配置服务暴露model
     */
    private void initServiceModelConfig() {
        //获得当前内网ip
        producer.setLocalIp(IPUtils.getLocalIP());
        producer.setName(invocationProperties.getName() + "-producer");
        producer.setPort(invocationProperties.getPort());
        consumes.setName(invocationProperties.getName() + "-consumes");
    }

    /**
     * 初始化扫描路径
     */
    public void initScanPath() {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(EnableInvocationConfiguration.class);
        if (beanNames != null) {
            Object object = applicationContext.getBean(beanNames[0]);
            EnableInvocationConfiguration enableInvocationConfiguration = object.getClass().getAnnotation(EnableInvocationConfiguration.class);
            String value = enableInvocationConfiguration.value();
            //leaderPort选择 配置文件优先
            if (StringUtils.isEmpty(invocationProperties.getLeaderPort())) {
                leaderPort = enableInvocationConfiguration.leaderPort();
            } else {
                leaderPort = invocationProperties.getLeaderPort();
            }
            if (StringUtils.isEmpty(value)) {
                consumes.setScanPath(object.getClass().getPackage().getName());
            } else {
                consumes.setScanPath(value);
            }
            System.out.println("扫描起点：" + consumes.getScanPath());
        }
    }

    /**
     * 初始化扫描
     */
    private void initScan() {
        producerScan.init(this);
        consumesScan.init(this);
    }

    /**
     * 初始化服务beanProxy
     */
    private void initBeanProxy() {
        new BeanProxy(this);
    }

    /**
     * 初始化网络模块
     */
    private void initNetwork() {
        new Network(leaderPort).start();
    }

    /**
     * 输出配置
     */
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
