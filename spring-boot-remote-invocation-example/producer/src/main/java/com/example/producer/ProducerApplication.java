package com.example.producer;

import org.remote.invocation.starter.annotation.EnableInvocationConfiguration;
import org.remote.invocation.starter.network.point.service.impl.PotintProxyServiceImpl;
import org.remote.invocation.starter.utils.ASMUtils;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpringBootApplication
@EnableInvocationConfiguration
public class ProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    //    @Bean(name = "/serviceProducers")
//    public HessianServiceExporter serviceProducers() {
//        HessianServiceExporter exporter = new HessianServiceExporter();
//        exporter.setService(applicationContext.getBean(TestProducerServiceImpl.class));
//        exporter.setServiceInterface(TestProducerService.class);
//        return exporter;
//    }

}
