package com.example.producer;

/**
 * @author liucheng
 * @create 2018-05-30 14:55
 **/
public interface TestInvocationService {
    String findOne(Long id);

    String update(Long id, Integer type);
}
