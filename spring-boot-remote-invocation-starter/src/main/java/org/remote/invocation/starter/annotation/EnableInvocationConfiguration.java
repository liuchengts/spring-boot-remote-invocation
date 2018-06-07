package org.remote.invocation.starter.annotation;


import java.lang.annotation.*;

/**
 * Enable Dubbo (for provider or consumer) for spring boot application
 *
 * @author liucheng
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableInvocationConfiguration {
    /**
     * 扫描路径
     */
    String value() default "";

    /**
     * leader通信端口
     */
    int leaderPort() default 2012;
}
