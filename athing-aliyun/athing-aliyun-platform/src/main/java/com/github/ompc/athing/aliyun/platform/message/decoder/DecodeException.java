package com.github.ompc.athing.aliyun.platform.message.decoder;

/**
 * 解码异常
 */
public class DecodeException extends Exception {

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
