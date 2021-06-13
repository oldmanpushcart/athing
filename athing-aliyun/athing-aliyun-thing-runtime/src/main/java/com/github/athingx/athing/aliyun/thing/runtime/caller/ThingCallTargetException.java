package com.github.athingx.athing.aliyun.thing.runtime.caller;

/**
 * 设备方法访问异常
 * <p>
 * 设备方法同步访问过程中，方法自身抛出的异常
 * </p>
 */
public class ThingCallTargetException extends RuntimeException {

    /**
     * 设备方法访问异常
     *
     * @param cause 目标异常
     */
    public ThingCallTargetException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return String.format("call target failure, %s", getCause().getLocalizedMessage());
    }

}
