package com.example.producer.service;

public interface TestService {
    String findOne(Long id);

    String update(Long id, Integer type);

    String find2One(Long id);

    String update2(Long id, Integer type);
}
