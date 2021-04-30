package com.github.ompc.athing.aliyun.thing.runtime.logger;

import java.util.function.Supplier;

/**
 * 设备日志
 */
public interface ThingLogger {

    /**
     * 封装可变参为数组
     *
     * @param arguments 可变参
     * @return 数组
     */
    static Object[] arguments(Object... arguments) {
        return arguments;
    }

    /**
     * 追踪
     *
     * @param format   日志格式
     * @param supplier 日志参数（提供）
     */
    void trace(String format, Supplier<Object[]> supplier);

    /**
     * 调试
     *
     * @param format   日志格式
     * @param supplier 日志参数（提供）
     */
    void debug(String format, Supplier<Object[]> supplier);

    /**
     * 信息
     *
     * @param format   日志格式
     * @param supplier 日志参数（提供）
     */
    void info(String format, Supplier<Object[]> supplier);


    /**
     * 警告
     *
     * @param format   日志格式
     * @param supplier 日志参数（提供）
     */
    void warn(String format, Supplier<Object[]> supplier);


    /**
     * 错误
     *
     * @param format   日志格式
     * @param supplier 日志参数（提供）
     */
    void error(String format, Supplier<Object[]> supplier);

    /**
     * 追踪
     *
     * @param format    日志格式
     * @param arguments 日志参数（可变参）
     */
    void trace(String format, Object... arguments);

    /**
     * 调试
     *
     * @param format    日志格式
     * @param arguments 日志参数（可变参）
     */
    void debug(String format, Object... arguments);

    /**
     * 信息
     *
     * @param format    日志格式
     * @param arguments 日志参数（可变参）
     */
    void info(String format, Object... arguments);

    /**
     * 警告
     *
     * @param format    日志格式
     * @param arguments 日志参数（可变参）
     */
    void warn(String format, Object... arguments);

    /**
     * 错误
     *
     * @param format    日志格式
     * @param arguments 日志参数（可变参）
     */
    void error(String format, Object... arguments);

}
