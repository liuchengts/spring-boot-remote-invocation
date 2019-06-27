package org.remote.invocation.starter.annotation;

import java.lang.annotation.*;

/**
 * Enable Invocation Service
 *
 * @author liucheng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InvocationService {

    Class<?> interfaceClass() default void.class;
}
