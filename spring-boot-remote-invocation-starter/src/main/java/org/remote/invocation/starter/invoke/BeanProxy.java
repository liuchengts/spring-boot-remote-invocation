package org.remote.invocation.starter.invoke;

import org.remote.invocation.starter.InvocationProperties;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.config.InvocationConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.util.ObjectUtils;

/**
 * 代理bean
 *
 * @author liucheng
 * @create 2018-06-04 15:21
 **/
public class BeanProxy implements BeanDefinitionRegistryPostProcessor {
    InvocationConfig invocationConfig;
    ApplicationContext applicationContext;
    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    /**
     * 初始化
     */
    public BeanProxy(ApplicationContext applicationContext, InvocationProperties invocationProperties) {
        Producer producer = applicationContext.getBean(Producer.class);
        Consumes consumes = applicationContext.getBean(Consumes.class);
        producer.setName(invocationProperties.getName() + "-producer");
        producer.setPort(invocationProperties.getPort());
        consumes.setName(invocationProperties.getName() + "-consumes");
        this.invocationConfig = new InvocationConfig(applicationContext);
        this.applicationContext = invocationConfig.getApplicationContext();
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Producer producer = invocationConfig.getProducer();
        producer.getServices().values().forEach(serviceBean -> {
            serviceBean.getInterfaceClasss().forEach(interfaceClass -> {
                registerBean(beanDefinitionRegistry, "/" + interfaceClass.getSimpleName(), HessianServiceExporter.class);
            });
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Producer producer = invocationConfig.getProducer();
        producer.getServices().values().forEach(serviceBean -> {
            serviceBean.getInterfaceClasss().forEach(interfaceClass -> {
                BeanDefinition bd = configurableListableBeanFactory.getBeanDefinition("/" + interfaceClass.getSimpleName());
                MutablePropertyValues mpv = bd.getPropertyValues();
                try {
                    //先从spring中获取，获取不到就自己创建
                    Object obj = applicationContext.getBean(serviceBean.getObjectClass());
                    if (ObjectUtils.isEmpty(obj)) {
                        obj = serviceBean.getObjectClass().newInstance();
                    }
                    mpv.addPropertyValue("service", obj);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                mpv.addPropertyValue("serviceInterface", interfaceClass);
            });
        });

    }

    /**
     * 注册bean
     *
     * @param registry  工厂对象
     * @param name      要注册的名称
     * @param beanClass 要注册的class
     */
    private void registerBean(BeanDefinitionRegistry registry, String name, Class<?> beanClass) {
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        // 可以自动生成name
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, registry));
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }
}
