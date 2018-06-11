package org.remote.invocation.starter.invoke;

import org.remote.invocation.starter.annotation.InvocationResource;
import org.remote.invocation.starter.cache.RouteCache;
import org.remote.invocation.starter.common.Consumes;
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
@Component
public class ResourceWired {
    RouteCache routeCache = RouteCache.getInstance(); //路由缓存
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    Consumes consumes;

    /**
     * 注入实现类
     */
    public void wiredConsumes() {
        consumes.getServices().values().forEach(consume -> {
           Object obj= applicationContext.getBean(consume.getObjectClass());
            Field[] fields = consume.getObjectClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(InvocationResource.class)) {
                    wired(obj,field);
                }
            }
        });
    }

    /**
     * 代理资源注入
     *
     * @param aClass 要注入的class
     * @param field  class需要远程实现的属性
     * @return 返回成功或者失败
     * @throws IllegalAccessException
     */
    private boolean wired( Object obj, Field field) {
        Class<?> cla = field.getType();
        if (!cla.isInterface()) {
            return false;
        }
        field.setAccessible(true);
        field.set(obj, getProducerOBJ(cla.getName()));
        field.setAccessible(false);
        return true;
    }

}
