package com.example.producer;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.springframework.stereotype.Component;

/**
 * 消费者
 *
 * @author liucheng
 * @create 2018-06-01 16:30
 **/
@Component
public class ConsumesServiceImpl {

    @InvocationResource
    private TestProducerService testProducerService;
    @InvocationResource
    private TestProducer2Service testProducer2Service;

    public  void d(){
        testProducerService.findOne(2l);
    }
}