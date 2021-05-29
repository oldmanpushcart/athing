package com.github.athingx.athing.aliyun.platform;

import com.github.athingx.athing.standard.platform.ThingPlatformException;

import static com.github.athingx.athing.aliyun.framework.Constants.THING_PLATFORM_CODE;

/**
 * 阿里云平台异常
 */
public class AliyunThingPlatformException extends ThingPlatformException {

    /**
     * 阿里云平台异常
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public AliyunThingPlatformException(String message, Throwable cause) {
        super(THING_PLATFORM_CODE, message, cause);
    }

    /**
     * 阿里云平台异常
     *
     * @param message 异常信息
     */
    public AliyunThingPlatformException(String message) {
        super(THING_PLATFORM_CODE, message);
    }

}
