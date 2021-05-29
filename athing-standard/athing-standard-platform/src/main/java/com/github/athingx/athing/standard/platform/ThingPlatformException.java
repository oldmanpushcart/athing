package com.github.athingx.athing.standard.platform;

/**
 * 设备平台异常
 */
public class ThingPlatformException extends Exception {

    private final String platformCode;

    /**
     * 设备平台异常
     *
     * @param platformCode 设备平台代码
     * @param message      错误信息
     * @param cause        错误原因
     */
    public ThingPlatformException(String platformCode, String message, Throwable cause) {
        super(message, cause);
        this.platformCode = platformCode;
    }

    /**
     * 设备平台异常
     *
     * @param platformCode 设备平台代码
     * @param message      错误信息
     */
    public ThingPlatformException(String platformCode, String message) {
        super(message);
        this.platformCode = platformCode;
    }

    /**
     * 获取设备平台代码
     *
     * @return 设备平台代码
     */
    public String getPlatformCode() {
        return platformCode;
    }

    @Override
    public String getLocalizedMessage() {
        return String.format("platform:/%s occur error: %s", getPlatformCode(), getMessage());
    }

}
