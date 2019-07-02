package com.example.producer.service.impl;

import com.example.producer.service.TestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.invoke.service.ManageRpcService;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liucheng
 * @create 2018-05-30 14:51
 **/
@Slf4j
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    ManageRpcService manageRpcService;
    @Autowired
    ObjectMapper objectMapper;

    private String json() {
        try {
            return objectMapper.writeValueAsString(manageRpcService.getIp());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String findOne(Long id) {
        log.info("findOne");
        return "findOne:" + json() + " time:" + System.currentTimeMillis();
    }


    @Override
    public String find2One(Long id) {
        log.info("find2One");
        return "find2One" + json() + " time:" + System.currentTimeMillis();
    }

    @Override
    public String update(Long id, Integer type) {
        log.info("update");
        return "update:" + ": ip " + IPUtils.getLocalIP() + " time:" + System.currentTimeMillis();
    }

    @Override
    public String update2(Long id, Integer type) {
        log.info("update2");
        return "update2:" + ": ip " + IPUtils.getLocalIP() + " time:" + System.currentTimeMillis();
    }
}
