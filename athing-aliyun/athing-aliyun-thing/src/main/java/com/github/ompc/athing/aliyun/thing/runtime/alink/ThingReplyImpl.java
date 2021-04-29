package com.github.ompc.athing.aliyun.thing.runtime.alink;

import com.github.ompc.athing.standard.thing.ThingReply;

import static com.github.ompc.athing.aliyun.thing.runtime.alink.Alink.ALINK_REPLY_OK;

/**
 * 默认设备应答实现
 *
 * @param <T> 应答数据类型
 */
public class ThingReplyImpl<T> implements ThingReply<T> {

    private final String id;
    private final int code;
    private final String message;
    private final T data;

    ThingReplyImpl(String token, int code, String message, T data) {
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

}
