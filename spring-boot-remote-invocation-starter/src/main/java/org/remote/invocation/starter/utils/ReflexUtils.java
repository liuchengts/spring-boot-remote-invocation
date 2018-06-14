package org.remote.invocation.starter.utils;

import org.springframework.boot.SpringApplication;
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

    /**
     * 代理执行公有方法（public）
     *
     * @param cla              方法所在的class
     * @param publicMethodName 要执行的方法名称
     * @param parameters       方法入参
     * @param parameterTypes   方法参数类型
     * @return 返回执行的结果
     */
    public static Object methodInvokePublic(Class cla, String publicMethodName, Object[] parameters, Class<?>... parameterTypes) {
        try {
            Object obj = cla.newInstance();
            cla.getMethods();
            Method method = cla.getMethod(publicMethodName, parameterTypes);
            return method.invoke(obj, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 代理执行私有方法
     *
     * @param cla               方法所在的class
     * @param privateMethodName 要执行的方法名称
     * @param parameters        方法入参
     * @param parameterTypes    方法参数类型
     * @return 返回执行的结果
     */
    public static Object methodInvokePrivate(Class cla, String privateMethodName, Object[] parameters, Class<?>... parameterTypes) {
        try {
            Object obj = cla.newInstance();
            cla.getMethods();
            Method method = cla.getDeclaredMethod(privateMethodName, parameterTypes);
            return method.invoke(obj, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用asm字节码动态生成class，并且执行父类的public修饰的方法（必须保证该class有继承关系，否则asm生成的是一个空的class）
     *
     * @param name              要创建的class名称 相当于 class.getName()
     * @param interfaceClasss   要实现的的接口class，可以是null
     * @param extendsClasssImpl 要继承的类class
     * @param publicMethodName  要执行的公有方法名称
     * @param parameters        方法参数
     * @param parameterTypes    方法参数类型
     * @return 返回执行结果
     */
    public static Object methodInvokeASM(String name, Set<Class> interfaceClasss, Class extendsClasssImpl, String publicMethodName, Object[] parameters, Class<?>... parameterTypes) {
        Class cla = ASMUtils.createClass(name, interfaceClasss, extendsClasssImpl);
        return ReflexUtils.methodInvokePublic(cla, publicMethodName, parameters, parameterTypes);
    }

}
