package org.remote.invocation.starter.scan;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 消费者扫描配置
 *
 * @author liucheng
 * @create 2018-06-04 14:16
 **/
@Scope
@Component
public class ConsumesScan {
    volatile InvocationConfig invocationConfig;
    volatile ApplicationContext applicationContext;

    /**
     * 初始化
     */
    public void init(InvocationConfig invocationConfig) {
        this.invocationConfig = invocationConfig;
        this.applicationContext = invocationConfig.getApplicationContext();
        Consumes consumes = invocationConfig.getConsumes();
        if (StringUtils.isEmpty(consumes.getScanPath())) {
            return;
        }
        Map<String, ServiceBean> services = new HashMap<>();
        Set<String> classSet = ReflexUtils.doScan(consumes.getScanPath());
        classSet.forEach(path -> {
            try {
                Class aClass = ReflexUtils.loaderClass(path);
                if (aClass != null) {
                    ServiceBean serviceBean = new ServiceBean();
                    serviceBean.setObjectClass(path);
                    Set<Class> interfacePaths = new HashSet<>();
                    Field[] fields = aClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(InvocationResource.class)) {
                            interfacePaths.add(field.getType());
                        }
                    }
                    if (!interfacePaths.isEmpty()) {
                        serviceBean.setInterfaceClasss(interfacePaths);
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
     * 配置打印
     */
    public void outPrintConfig() {
        invocationConfig.verifyConsumesJSON();
    }

    /**
     * 获得消费者者
     *
     * @return
     */
    public Consumes getConsumes() {
        return invocationConfig.getConsumes();
    }
}
