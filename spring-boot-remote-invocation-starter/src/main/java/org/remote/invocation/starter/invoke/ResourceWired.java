package org.remote.invocation.starter.invoke;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.Consumes;
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
     * 获得消费者者
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
                    //TODO 这里可以加上参数实现是否检查远程服务
                    try {
                        Class<?> cla = field.getType();
                        if (!cla.isInterface()) {
                            return;
                        }
                        field.setAccessible(true);
                        field.set(obj, routeCache.getServiceObjectImpl(cla));
                        field.setAccessible(false);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
