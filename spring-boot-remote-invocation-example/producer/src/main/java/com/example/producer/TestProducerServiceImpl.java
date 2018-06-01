package com.example.producer;

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
        return "findOne:" + id;
    }

    @Override
    public String update(Long id, Integer type) {
        return "update:" + id;
    }

    public String findOne2(Long id) {
        return "findOne2:" + id;
    }

    private String findOne3(Long id) {
        return "findOne2:" + id;
    }

    String findOne5(Long id) {
        return "findOne2:" + id;
    }

    protected String findOne4(Long id) {
        return "findOne2:" + id;
    }

    @Override
    public String find2One(Long id) {
        return null;
    }

    @Override
    public String update2(Long id, Integer type) {
        return null;
    }
}
