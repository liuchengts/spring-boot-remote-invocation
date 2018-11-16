package org.remote.invocation.starter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.InvocationProperties;
import org.remote.invocation.starter.annotation.EnableInvocationConfiguration;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceRoute;
import org.remote.invocation.starter.invoke.ResourceWired;
import org.remote.invocation.starter.network.NetWork;
import org.remote.invocation.starter.scan.ConsumesScan;
import org.remote.invocation.starter.scan.ProducerScan;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 调用的配置中心
 *
 * @author liucheng
 * @create 2018-05-29 18:00
 **/
@Slf4j
@Data
@Component
@EnableConfigurationProperties(InvocationProperties.class)
public class InvocationConfig {
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    InvocationProperties invocationProperties;
    ObjectMapper objectMapper = new ObjectMapper();
    Producer producer;
    Consumes consumes;
    ConsumesScan consumesScan;
    ProducerScan producerScan;
    ServiceRoute serviceRoute;
    int leaderPort;
    String netSyncIp;
    NetWork netWork;

    public void initInvocationConfig() {
        getModel();
        initServiceModelConfig();
        initScanPath();
        initScan();
        initServiceRoute();
        initRouteCache();
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
        serviceRoute = applicationContext.getBean(ServiceRoute.class);
        netSyncIp = invocationProperties.getNetSyncIp();
    }

    /**
     * 配置服务暴露model
     */
    private void initServiceModelConfig() {
        //获得当前内网ip
        producer.setLocalIp(IPUtils.getLocalIP());
        //获得当前外网ip
        producer.setNetIp(IPUtils.getNetIP());
        log.info("localIp:" + producer.getLocalIp() + " | netIp:" + producer.getNetIp() + " | netSyncIp:" + netSyncIp);
        producer.setName(invocationProperties.getName() + "-producer");
        producer.setPort(invocationProperties.getPort());
        consumes.setName(invocationProperties.getName() + "-consumes");
        serviceRoute.setKey(ServiceRoute.createKey(producer.getLocalIp(), producer.getNetIp(), producer.getPort()));
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
            log.debug("扫描起点：" + consumes.getScanPath());
        }
    }

    /**
     * 初始化扫描
     */
    private void initScan() {
        consumesScan.init(this);
    }

    /**
     * 初始化服务路由ServiceRoute
     */
    private void initServiceRoute() {
        serviceRoute.setProducer(producer);
    }

    /**
     * 初始化路由组件
     */
    private void initRouteCache() {
        RouteCache.getInstance().initRouteCache(this);

    }

    /**
     * 初始化网络模块
     */
    private void initNetwork() {
        netWork = new NetWork(this);
        netWork.start();
    }

    /**
     * 输出配置
     */
    private void outPrin() {
        log.debug("配置输出：");
        verifyProducerJSON();
        verifyConsumesJSON();
    }

    /**
     * 校验生产者配置结果
     */
    public void verifyProducerJSON() {
        try {
            String producerJson = objectMapper.writeValueAsString(producer);
            log.debug("producerJson:" + producerJson);
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
            log.debug("consumesJson:" + consumesJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


}
