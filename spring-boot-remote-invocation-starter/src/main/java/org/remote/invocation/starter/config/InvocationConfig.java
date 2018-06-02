package org.remote.invocation.starter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.remote.invocation.starter.annotation.EnableInvocationConfiguration;
import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.annotation.InvocationService;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.MethodBean;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.utils.IPUtils;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 调用的配置中心
 *
 * @author liucheng
 * @create 2018-05-29 18:00
 **/
public class InvocationConfig {

    ApplicationContext applicationContext;
    ObjectMapper objectMapper;
    Producer producer;
    Consumes consumes;
    String scanPath;

    public InvocationConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        getScanPath();
        getModel();
        addressConfig();
        producerScan();
        consumesScan();
        verify();
    }

    /**
     * 初始化扫描路径
     */
    private void getScanPath() {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(EnableInvocationConfiguration.class);
        if (beanNames != null) {
            Object object = applicationContext.getBean(beanNames[0]);
            EnableInvocationConfiguration enableInvocationConfiguration = object.getClass().getAnnotation(EnableInvocationConfiguration.class);
            String value = enableInvocationConfiguration.value();
            if (StringUtils.isEmpty(value)) {
                scanPath = object.getClass().getPackage().getName();
            } else {
                scanPath = value;
            }
            System.out.println("扫描起点：" + scanPath);
        }
    }


    /**
     * 从spring中获得基础的model
     */
    private void getModel() {
        producer = applicationContext.getBean(Producer.class);
        consumes = applicationContext.getBean(Consumes.class);
        objectMapper = applicationContext.getBean(ObjectMapper.class);
    }

    /**
     * 绑定ip
     */
    private void addressConfig() {
        //获得当前外网ip
        producer.setIp(IPUtils.getInternetIP());
        //获得当前内网ip
        producer.setLocalIp(IPUtils.getLocalIP());
    }

    /**
     * 获得生产者
     */
    protected void producerScan() {
        Map<String, ServiceBean> services = new HashMap<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(InvocationService.class);
        if (beanNames != null) {
            for (String beanPath : beanNames) {
                services.put(beanPath, getServiceBean(beanPath));
            }
        }
        producer.setServices(services);
    }


    /**
     * 获得消费者
     */
    protected void consumesScan() {
        if (scanPath == null) {
            return;
        }
        Map<String, ServiceBean> services = new HashMap<>();
        Set<String> classSet = doScan(scanPath);
        classSet.forEach(path -> {
            try {
                Class aClass = ReflexUtils.loaderClass(path);
                if (aClass != null) {
                    ServiceBean serviceBean = new ServiceBean();
                    serviceBean.setObjectPath(aClass.getName());
                    Set<String> interfacePaths = new HashSet<>();
                    Field[] fields = aClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(InvocationResource.class)) {
                            interfacePaths.add(field.getName());
                        }
                    }
                    if (!interfacePaths.isEmpty()) {
                        serviceBean.setInterfacePath(interfacePaths);
                        services.put(path, serviceBean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        consumes.setServices(services);
    }


    /**
     * 获得当前包下的类
     *
     * @param basePackage 包路径
     * @return 返回包含的类
     */
    protected Set<String> doScan(String basePackage) {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        Set<String> classes = new HashSet<>();
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils
                    .resolvePlaceholders(basePackage))
                    + "/**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (int i = 0; i < resources.length; i++) {
                Resource resource = resources[i];
                if (resource.isReadable()) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    classes.add(metadataReader.getClassMetadata().getClassName());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return classes;
    }

    /**
     * 根据class路径获得ServiceBean
     *
     * @param beanPath 实例路径
     * @return 返回ServiceBean
     */
    protected ServiceBean getServiceBean(String beanPath) {
        ServiceBean serviceBean = new ServiceBean();
        try {
            Object object = applicationContext.getBean(beanPath);
            Class objClass = object.getClass();
            String objClassParh = objClass.toString();
            Class<?>[] interfaces = objClass.getInterfaces();
            serviceBean.setObjectPath(objClassParh);
            Set<String> interfacePaths = new HashSet<>();
            Set<MethodBean> methodBeans = new HashSet<>();
            for (Class<?> inte : interfaces) {
                interfacePaths.add(inte.getName());
                Method[] methods = inte.getDeclaredMethods();
                for (Method method : methods) {
                    methodBeans.add(MethodBean.builder()
                            .name(method.getName())
                            .returnType(method.getReturnType())
                            .parameters(handleParameters(method))
                            .parameterCount(method.getParameterCount())
                            .build());
                }
            }
            serviceBean.setInterfacePath(interfacePaths);
            serviceBean.setMethods(methodBeans);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serviceBean;
    }

    /**
     * 获得方法入参的属性
     *
     * @param method 方法
     * @return 返回属性结果
     */
    protected LinkedHashMap<String, Class> handleParameters(Method method) {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            map.put(parameter.getName(), parameter.getType());
        }
        return map;
    }

    /**
     * 校验配置结果
     */
    private void verify() {
        try {
            String producerJson = objectMapper.writeValueAsString(producer);
            String consumesJson = objectMapper.writeValueAsString(consumes);
            System.out.println("producerJson:" + producerJson);
            System.out.println("consumesJson:" + consumesJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
