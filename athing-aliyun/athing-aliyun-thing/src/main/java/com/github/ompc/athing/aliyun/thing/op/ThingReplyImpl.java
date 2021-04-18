package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.standard.thing.ThingReply;

/**
 * 设备应答实现
 *
 * @param <T> 应答数据类型
 */
public class ThingReplyImpl<T> implements ThingReply<T> {

    private final boolean isReplySuccess;
    private final String replyCode;
    private final String replyMessage;
    private final T replyData;

    /**
     * 设备应答实现
     *
     * @param isReplySuccess 是否应答成功
     * @param replyCode      应答码
     * @param replyMessage   应答信息
     * @param replyData      应答数据
     */
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
    public static <T> ThingReply<T> success(AlinkReply<?> reply, T data) {
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
    public static <T> ThingReply<T> failure(AlinkReply<?> reply) {
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
    public static <T> ThingReply<T> empty(AlinkReply<?> reply) {
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
