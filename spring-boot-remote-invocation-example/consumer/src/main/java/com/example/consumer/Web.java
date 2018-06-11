package com.example.consumer;

import com.caucho.hessian.client.HessianProxyFactory;
import com.example.api.TestProducerService;
import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.scan.ConsumesScan;
import org.remote.invocation.starter.scan.ProducerScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;

/**
 * @author liucheng
 * @create 2018-06-04 14:59
 **/
@RestController
public class Web {
    @InvocationResource
    private TestProducerService testProducerService;
    //    @InvocationResource
//    private TestProducer2Service testProducer2Service;
    @Autowired
    private ConsumesScan consumesScan;
//    @Autowired
//    private ProducerScan producerScan;

    @RequestMapping("/hessian")
    public String hessian() {
        String url = "http://localhost:8080/TestProducerService";
//        String url = "http://localhost:8080/testProducerService";
        HessianProxyFactory factory = new HessianProxyFactory();
        try {
            testProducerService = (TestProducerService) factory.create(TestProducerService.class, url);
            return testProducerService.findOne(22l);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/c")
    public String d() {
        System.out.println("WEB层 测试资源获取========");
        consumesScan.outPrintConfig();
//        producerScan.outPrintConfig();
        return testProducerService.findOne(2l);
//        testProducer2Service.find2One(2l);
    }


}
