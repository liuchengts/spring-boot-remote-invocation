package org.remote.invocation.starter;

import org.remote.invocation.starter.config.InvocationConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;

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
    InvocationProperties invocationProperties;
    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public InvocationConfig invocationConfig() {
        return new InvocationConfig(applicationContext,invocationProperties);
    }


}
