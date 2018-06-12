package org.remote.invocation.starter;

import org.remote.invocation.starter.invoke.BeanProxy;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class InvocationAutoConfiguration implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanProxy beanProxy() {
        return new BeanProxy(applicationContext);
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public InvocationNotifier invocationNotifier() {
//        return new InvocationNotifier();
//    }
}
