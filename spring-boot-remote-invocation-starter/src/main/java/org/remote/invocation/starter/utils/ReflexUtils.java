package org.remote.invocation.starter.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

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

    /**
     * 获得当前包下的类
     *
     * @param basePackage 包路径
     * @return 返回包含的类
     */
    public static Set<String> doScan(String basePackage) {
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
     * 获得方法入参的属性
     *
     * @param method 方法
     * @return 返回属性结果
     */
    public static LinkedHashMap<String, Class> handleParameters(Method method) {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            map.put(parameter.getName(), parameter.getType());
        }
        return map;
    }
}
