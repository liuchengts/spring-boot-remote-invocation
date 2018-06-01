package org.remote.invocation.starter.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.annotation.InvocationService;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.MethodDTO;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.utils.IPUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public InvocationConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        getModel();
        addressConfig();
        producerScan();
        consumesScan();
        verify();
    }

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
        Map<String, Set<MethodDTO>> services = new HashMap<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(InvocationService.class);
        if (beanNames != null) {
            for (String beanPath : beanNames) {
                services.put(beanPath, addMethodDTOS(beanPath));
            }
        }
        producer.setServices(services);
    }


    /**
     * 获得消费者
     */
    protected void consumesScan() {
        Map<String, Set<MethodDTO>> services = new HashMap<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(InvocationResource.class);
        if (beanNames != null) {
            for (String beanPath : beanNames) {
                services.put(beanPath, addMethodDTOS(beanPath));
            }
        }
        consumes.setServices(services);
    }

    /**
     * 构造方法结果
     *
     * @param beanPath 实例路径
     * @return 返回构造结果
     */
    private Set<MethodDTO> addMethodDTOS(String beanPath) {
        Set<MethodDTO> methodDTOS = new HashSet<>();
        try {
            Object object = applicationContext.getBean(beanPath);
            Class objClass = object.getClass();
            String objClassParh = objClass.toString();
            Class<?> interfaces[] = objClass.getInterfaces();//获得Dog所实现的所有接口
            for (Class<?> inte : interfaces) {
                System.out.println("实现接口：" + inte);
                Method[] methods = inte.getDeclaredMethods();
                for (Method method : methods) {
                    System.out.println(method.toGenericString());
                    methodDTOS.add(MethodDTO.builder()
                            .name(method.getName())
                            .objectPath(objClassParh)
                            .returnType(method.getReturnType())
                            .parameters(handleParameters(method))
                            .parameterCount(method.getParameterCount())
                            .build());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return methodDTOS;
    }

    /**
     * 获得方法入参的属性
     *
     * @param method 方法
     * @return 返回属性结果
     */
    private Map<String, Class> handleParameters(Method method) {
        Map<String, Class> map = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            System.out.println(parameter.getName() + " | " + parameter.getType());
            map.put(parameter.getName(), parameter.getType());
        }
        return map;
    }
}
