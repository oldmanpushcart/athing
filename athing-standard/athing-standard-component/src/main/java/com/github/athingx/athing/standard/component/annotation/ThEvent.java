package com.github.athingx.athing.standard.component.annotation;

import com.github.athingx.athing.standard.component.ThingEvent;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 事件注解
 */
@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(ThEvents.class)
public @interface ThEvent {

    /**
     * 事件ID
     *
     * @return 事件ID
     */
    String id();

    /**
     * 事件名称
     *
     * @return 事件名称
     */
    String name() default "";

    /**
     * 描述
     *
     * @return 事件描述
     */
    String desc() default "";

    /**
     * 事件数据类型
     *
     * @return 数据类型
     */
    Class<? extends ThingEvent.Data> type();

    /**
     * 等级
     *
     * @return 事件等级
     */
    Level level() default Level.INFO;

    /**
     * 事件等级
     */
    enum Level {

        INFO,
        WARN,
        ERROR

    }

}
