package org.remote.invocation.starter;

import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.config.InvocationConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 配置入口
 *
 * @author liucheng
 * @create 2018-05-11 18:30
 **/
@Configuration
@ComponentScan({"org.remote.invocation.starter"})
@EnableConfigurationProperties(InvocationProperties.class)
public class InvocationAutoConfiguration implements ApplicationContextAware {

    @Autowired
    InvocationProperties properties;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public Producer producer() {
        return Producer.builder()
                .name(properties.getName())
                .port(properties.getPort())
                .isRegister(properties.getIsRegister() == null ? Boolean.TRUE : properties.getIsRegister())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Consumes consumes() {
        return Consumes.builder()
                .name(properties.getName())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public InvocationConfig invocationConfig() {
        return new InvocationConfig(applicationContext);
    }

}
