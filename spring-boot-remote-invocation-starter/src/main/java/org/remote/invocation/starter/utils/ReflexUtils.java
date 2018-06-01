package org.remote.invocation.starter.utils;

import java.lang.reflect.Method;

public class ReflexUtils {
    static ClassLoader classLoader;
    static Thread thread;

    static {

        thread = Thread.currentThread();
        classLoader = thread.getContextClassLoader();
    }

    /**
     * 获得当前线程
     *
     * @return
     */
    public static Thread getThread() {
        return thread;
    }

    /**
     * 获得加载器
     *
     * @return
     */
    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 获得对象属性的值
     */
    @SuppressWarnings("unchecked")
    public static Object invokeMethod(Object owner, String methodName, Object[] args) throws Exception {
        Class<?> ownerClass = owner.getClass();
        methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        Method method = null;
        try {
            method = ownerClass.getMethod("get" + methodName);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
            return " can't find 'get" + methodName + "' method";
        }
        return method.invoke(owner);
    }


    /**
     * 加载一个类
     *
     * @param path 路径
     * @return 返回class
     * @throws Exception
     */
    public static Class loaderClass(String path) throws Exception {
        return classLoader.loadClass(path);
    }
}
