package com.example.producer;

import org.remote.invocation.starter.annotation.InvocationService;
import org.springframework.stereotype.Component;

/**
 * @author liucheng
 * @create 2018-05-30 14:51
 **/
@InvocationService
@Component("testInvocationService")
public class TestInvocationServiceImpl implements TestInvocationService {
    static {
        System.out.println("我被初始化了" + void.class);
    }

    @Override
    public String findOne(Long id) {
        return "findOne:" + id;
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

}
