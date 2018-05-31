package org.remote.invocation.starter.invoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 代理执行
 * @author liucheng
 * @create 2018-05-31 14:34
 **/
@Component
@Scope
public class BeanInvoke {
    @Autowired
    ApplicationContext applicationContext;

    /**
     * 获得spring对应的类
     *
     * @param name 别名
     * @return 返回对象
     */
    protected Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 代理执行
     *
     * @param obj     实例对象
     * @param method  要执行的方法
     * @param objects 调用参数
     * @return 返回对象
     * @throws Exception
     */
    protected Object invoke(Object obj, Method method, Object[] objects) throws Exception {
        return method.invoke(obj, objects);
    }
}
