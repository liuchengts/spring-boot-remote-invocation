package com.example.producer.invocation;

import com.example.api.TestProducer2Service;
import com.example.api.TestProducerService;
import com.example.producer.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.annotation.InvocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liucheng
 * @create 2018-05-30 14:51
 **/
@Slf4j
@InvocationService
@Component("testProducers")
public class InvocationProducerServiceImpl implements TestProducerService, TestProducer2Service {

    @Autowired
    TestService testService;

    @Override
    public String findOne(Long id) {
        return testService.findOne(id);
    }

    @Override
    public String find2One(Long id) {
        return testService.find2One(id);
    }

    @Override
    public String update(Long id, Integer type) {
        return testService.update(id, type);
    }

    @Override
    public String update2(Long id, Integer type) {
        return testService.update2(id, type);
    }
}
