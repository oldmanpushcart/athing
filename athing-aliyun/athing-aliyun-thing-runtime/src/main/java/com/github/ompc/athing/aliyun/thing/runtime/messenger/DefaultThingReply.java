package com.github.ompc.athing.aliyun.thing.runtime.messenger;

import com.github.ompc.athing.standard.thing.ThingReply;

/**
 * 默认设备应答实现
 *
 * @param <T> 应答数据类型
 */
public class DefaultThingReply<T> implements ThingReply<T> {

    /**
     * 成功
     */
    public static final int ALINK_REPLY_OK = 200;

    /**
     * 内部错误，解析请求时发生错误
     */
    public static final int ALINK_REPLY_REQUEST_ERROR = 400;

    /**
     * 内部错误，处理请求时发生错误
     */
    public static final int ALINK_REPLY_PROCESS_ERROR = 500;

    /**
     * 设备服务尚未定义
     */
    public static final int ALINK_REPLY_SERVICE_NOT_PROVIDED = 5161;

    private final String id;
    private final int code;
    private final String message;
    private final T data;

    private DefaultThingReply(String token, int code, String message, T data) {
        this.id = token;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Override
    public String getToken() {
        return id;
    }

    @Override
    public boolean isOk() {
        return code == ALINK_REPLY_OK;
    }

    @Override
    public String getCode() {
        return String.valueOf(code);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public T getData() {
        return data;
    }

    /**
     * 成功应答
     *
     * @param token 令牌
     * @param data  应答数据
     * @param <T>   数据类型
     * @return 应答
     */
    public static <T> ThingReply<T> success(String token, T data) {
        return new DefaultThingReply<>(token, ALINK_REPLY_OK, "success", data);
    }

    /**
     * 成功应答
     *
     * @param token 令牌
     * @return 应答
     */
    public static ThingReply<Void> success(String token) {
        return success(token, null);
    }

    /**
     * 失败应答
     *
     * @param token   令牌
     * @param code    应答代码
     * @param message 应答消息
     * @return 应答
     */
    public static ThingReply<?> failure(String token, int code, String message) {
        return new DefaultThingReply<>(token, code, message, null);
    }

    public static ThingReply<Void> empty(ThingReply<?> reply) {
        return new DefaultThingReply<Void>(reply.getToken(), Integer.parseInt(reply.getCode()), reply.getMessage(), null);
    }

}
