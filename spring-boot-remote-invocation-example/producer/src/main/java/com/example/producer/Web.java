package com.example.producer;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.scan.ConsumesScan;
import org.remote.invocation.starter.scan.ProducerScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liucheng
 * @create 2018-06-04 14:59
 **/
@RestController
public class Web {
    @InvocationResource
    private TestProducerService testProducerService;
    @Autowired
    private TestProducer2Service testProducer2Service;
    @Autowired
    private ConsumesScan consumesScan;
    @Autowired
    private ProducerScan producerScan;

    @RequestMapping("/c")
    public void d() {
        System.out.println("WEB层 测试资源获取========");
        consumesScan.outPrintConfig();
        producerScan.outPrintConfig();
//        testProducerService.findOne(2l);
//        testProducer2Service.find2One(2l);
    }
}
