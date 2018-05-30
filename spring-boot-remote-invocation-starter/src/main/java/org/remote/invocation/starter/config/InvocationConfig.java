package org.remote.invocation.starter.config;

import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.scan.ConsumesScan;
import org.remote.invocation.starter.scan.ProducerScan;
import org.remote.invocation.starter.utils.IPUtils;

/**
 * 调用的配置中心
 *
 * @author liucheng
 * @create 2018-05-29 18:00
 **/
public class InvocationConfig {
    Producer producer;
    Consumes consumes;
    ProducerScan producerScan;
    ConsumesScan consumesScan;

    public InvocationConfig(Producer producer,
                            Consumes consumes,
                            ProducerScan producerScan,
                            ConsumesScan consumesScan) {
        this.producer = producer;
        this.consumes = consumes;
        this.producerScan = producerScan;
        this.consumesScan = consumesScan;
        addressConfig();
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

}
