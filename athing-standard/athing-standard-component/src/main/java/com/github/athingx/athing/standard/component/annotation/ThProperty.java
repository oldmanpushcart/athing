package com.github.athingx.athing.standard.component.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 属性注解
 * <p>
 * 属性注解必须在Get方法上
 * </p>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface ThProperty {

    /**
     * 属性ID
     *
     * @return 属性ID
     */
    String id() default "";

    /**
     * 属性名称
     *
     * @return 属性名称
     */
    String name() default "";

    /**
     * 描述
     *
     * @return 属性描述
     */
    String desc() default "";

    /**
     * 是否必须
     *
     * @return TRUE|FALSE
     */
    boolean isRequired() default true;

}
