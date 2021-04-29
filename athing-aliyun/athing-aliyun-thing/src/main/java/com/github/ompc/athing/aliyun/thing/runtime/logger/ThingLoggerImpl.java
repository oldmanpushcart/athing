package com.github.ompc.athing.aliyun.thing.runtime.logger;

import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * 设备日志实现
 */
public class ThingLoggerImpl implements ThingLogger {

    private final Logger logger;

    public ThingLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(String format, Supplier<Object[]> supplier) {
        if (logger.isTraceEnabled()) {
            logger.trace(format, supplier.get());
        }
    }

    @Override
    public void debug(String format, Supplier<Object[]> supplier) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, supplier.get());
        }
    }

    @Override
    public void info(String format, Supplier<Object[]> supplier) {
        if (logger.isInfoEnabled()) {
            logger.info(format, supplier.get());
        }
    }

    @Override
    public void warn(String format, Supplier<Object[]> supplier) {
        if (logger.isWarnEnabled()) {
            logger.warn(format, supplier.get());
        }
    }

    @Override
    public void error(String format, Supplier<Object[]> supplier) {
        if (logger.isErrorEnabled()) {
            logger.error(format, supplier.get());
        }
    }
}
