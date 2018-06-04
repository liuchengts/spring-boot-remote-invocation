package org.remote.invocation.starter.scan;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.annotation.InvocationService;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.common.ServiceBean;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 消费者扫描配置
 *
 * @author liucheng
 * @create 2018-06-04 14:16
 **/
@Component
public class ConsumesScan {
    InvocationConfig invocationConfig;

    /**
     * 初始化
     */
    public void init(InvocationConfig invocationConfig) {
        this.invocationConfig = invocationConfig;
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
