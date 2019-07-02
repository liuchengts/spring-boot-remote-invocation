package com.example.producer;

import org.remote.invocation.starter.annotation.EnableInvocationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
