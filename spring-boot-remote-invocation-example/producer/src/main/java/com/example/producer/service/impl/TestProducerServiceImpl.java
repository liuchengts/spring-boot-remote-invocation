package com.example.producer.service.impl;

import com.example.api.TestProducer2Service;
import com.example.api.TestProducerService;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.annotation.InvocationService;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.stereotype.Component;

/**
 * @author liucheng
 * @create 2018-05-30 14:51
 **/
@Slf4j
@InvocationService
@Component("testProducers")
public class TestProducerServiceImpl implements TestProducerService, TestProducer2Service {
    static {
        log.info("我被初始化了" + void.class);
    }

    @Override
    public String findOne(Long id) {
        log.info("findOne");
        return "findOne" + ": ip" + IPUtils.getNetIP();
    }


    @Override
    public String find2One(Long id) {
        log.info("find2One");
        return "find2One" + ": ip" + IPUtils.getNetIP();
    }

    @Override
    public String update(Long id, Integer type) {
        log.info("update");
        return "update:" + ": ip" + IPUtils.getNetIP();
    }

    @Override
    public String update2(Long id, Integer type) {
        log.info("update2");
        return "update2:" + ": ip" + IPUtils.getNetIP();
    }
}
