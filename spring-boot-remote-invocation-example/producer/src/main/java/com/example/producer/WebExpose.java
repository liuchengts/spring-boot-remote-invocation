package com.example.producer;

import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.scan.ProducerScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 先提供一个http的暴露，作为测试，后面需要改为netty
 *
 * @author liucheng
 * @create 2018-06-05 09:29
 **/
@RestController
public class WebExpose {

    @Autowired
    private ProducerScan producerScan;

    @RequestMapping("/producers")
    public Producer d() {
        System.out.println("WEB层 模拟资源获取========");
        return producerScan.getProducer();
    }
}
