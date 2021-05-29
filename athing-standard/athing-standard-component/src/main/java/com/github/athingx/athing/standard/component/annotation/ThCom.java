package com.github.athingx.athing.standard.component.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 设备组件注解
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ThCom {

    /**
     * 组件ID
     *
     * @return 事件ID
     */
    String id();

    /**
     * 组件名称
     *
     * @return 组件名称
     */
    String name();

    /**
     * 组件描述
     *
     * @return 组件描述
     */
    String desc() default "";

}
