package org.remote.invocation.starter.invoke;

import lombok.extern.slf4j.Slf4j;
import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.Consumes;
import org.remote.invocation.starter.common.Producer;
import org.remote.invocation.starter.config.InvocationConfig;
import org.remote.invocation.starter.utils.ReflexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 注入资源
 *
 * @author liucheng
 * @create 2018-06-11 11:42
 **/
@Slf4j
public class ResourceWired {
    ApplicationContext applicationContext;
    InvocationConfig invocationConfig;


    /**
     * 初始化
     *
     * @param invocationConfig
     */
    public ResourceWired(InvocationConfig invocationConfig) {
        this.invocationConfig = invocationConfig;
        this.applicationContext = invocationConfig.getApplicationContext();
    }

    /**
     * 获得生产者
     *
     * @return
     */
    public Producer getProducer() {
        return invocationConfig.getProducer();
    }

    /**
     * 获得消费者
     *
     * @return
     */
    public Consumes getConsumes() {
        return invocationConfig.getConsumes();
    }

    /**
     * 注入实现类
     */
    public void wiredConsumes(RouteCache routeCache) {
        invocationConfig.getConsumes().getServices().values().forEach(consume -> {
            Class aClass = null;
            try {
                aClass = ReflexUtils.loaderClass(consume.getObjectClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Object obj = applicationContext.getBean(aClass);
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(InvocationResource.class)) {
                    try {
                        Class<?> cla = field.getType();
                        if (!cla.isInterface()) {
                            return;
                        }
                        field.setAccessible(true);
                        Object objImpl = routeCache.getServiceObjectImpl(cla);
                        field.set(obj, objImpl);
                        log.info("注入时间:" + System.currentTimeMillis() + " interface:" + cla.getSimpleName() + "  interfaceImpl:" + objImpl);
                        field.setAccessible(false);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
