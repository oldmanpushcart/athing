package com.github.ompc.athing.aliyun.thing.executor.impl;

import com.github.ompc.athing.standard.thing.ThingReply;

public class ThingReplyImpl<T> implements ThingReply<T> {

    private final boolean isReplySuccess;
    private final String replyCode;
    private final String replyMessage;
    private final T replyData;

    public ThingReplyImpl(boolean isReplySuccess, String replyCode, String replyMessage, T replyData) {
        this.isReplySuccess = isReplySuccess;
        this.replyCode = replyCode;
        this.replyMessage = replyMessage;
        this.replyData = replyData;
    }

    /**
     * 构建设备平台成功应答
     *
     * @param reply Alink协议的应答
     * @param data  应答数据
     * @param <T>   应答类型
     * @return 设备平台应答
     */
    public static <T> ThingReply<T> success(AlinkReplyImpl<?> reply, T data) {
        return new ThingReplyImpl<>(
                true,
                String.valueOf(reply.getCode()),
                reply.getMessage(),
                data
        );
    }

    /**
     * 构建设备平台失败应答
     *
     * @param reply Alink协议的应答
     * @param <T>   应答类型
     * @return 设备平台应答
     */
    public static <T> ThingReply<T> failure(AlinkReplyImpl<?> reply) {
        return new ThingReplyImpl<>(
                false,
                String.valueOf(reply.getCode()),
                reply.getMessage(),
                null
        );
    }

    /**
     * 构建设备平台成功应答
     *
     * @param reply Alink协议的应答
     * @param <T>   应答类型
     * @return 设备平台应答
     */
    public static <T> ThingReply<T> empty(AlinkReplyImpl<?> reply) {
        return new ThingReplyImpl<>(
                reply.isOk(),
                String.valueOf(reply.getCode()),
                reply.getMessage(),
                null
        );
    }

    @Override
    public boolean isOk() {
        return isReplySuccess;
    }

    @Override
    public String getCode() {
        return replyCode;
    }

    @Override
    public String getMessage() {
        return replyMessage;
    }

    @Override
    public T getData() {
        return replyData;
    }
}
