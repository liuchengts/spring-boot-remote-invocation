package org.remote.invocation.starter;

import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.config.InvocationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 配置入口
 *
 * @author liucheng
 * @create 2018-05-11 18:30
 **/
@Configuration
@ComponentScan({"org.remote.invocation.starter"})
@EnableConfigurationProperties(InvocationProperties.class)
public class InvocationAutoConfiguration {

    @Autowired
    InvocationProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public Producer producer() {
        return Producer.builder()
                .name(properties.name)
                .port(properties.port)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Consumes consumes() {
        return Consumes.builder()
                .name(properties.name)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public InvocationConfig invocationConfig() {
        return new InvocationConfig();
    }
}
