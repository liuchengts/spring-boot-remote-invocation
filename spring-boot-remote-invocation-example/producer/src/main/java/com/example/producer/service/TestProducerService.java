package com.example.producer.service;

/**
 * @author liucheng
 * @create 2018-05-30 14:55
 **/
public interface TestProducerService {
    String findOne(Long id);

    String update(Long id, Integer type);
}
