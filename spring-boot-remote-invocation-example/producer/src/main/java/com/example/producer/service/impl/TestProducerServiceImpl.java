package com.example.producer.service.impl;

import com.example.api.TestProducer2Service;
import com.example.api.TestProducerService;
import org.remote.invocation.starter.annotation.InvocationService;
import org.springframework.stereotype.Component;

/**
 * @author liucheng
 * @create 2018-05-30 14:51
 **/
@InvocationService
@Component("testProducers")
public class TestProducerServiceImpl implements TestProducerService, TestProducer2Service {
    static {
        System.out.println("我被初始化了" + void.class);
    }

    @Override
    public String findOne(Long id) {
        System.out.println("findOne");
        return "findOne:" + id;
    }


    @Override
    public String find2One(Long id) {
        System.out.println("find2One");
        return "find2One:" + id;
    }

    @Override
    public String update(Long id, Integer type) {
        System.out.println("update");
        return "update:" + id;
    }

    @Override
    public String update2(Long id, Integer type) {
        System.out.println("update2");
        return "update2:" + id;
    }
}
