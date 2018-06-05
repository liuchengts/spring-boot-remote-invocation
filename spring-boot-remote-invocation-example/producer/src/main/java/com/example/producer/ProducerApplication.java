package com.example.producer;

import com.example.api.TestProducerService;
import com.example.producer.service.impl.TestProducerServiceImpl;
import org.remote.invocation.starter.annotation.EnableInvocationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.caucho.HessianServiceExporter;

@SpringBootApplication
@EnableInvocationConfiguration
public class ProducerApplication {
    @Autowired
    ApplicationContext applicationContext;

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
