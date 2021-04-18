package com.github.ompc.athing.aliyun.thing.op;

/**
 * 阿里云Alink协议应答
 * <p>
 * 阿里云的物模型通过MQTT协议传输的时候走的是Alink协议
 * </p>
 */
public class AlinkReply<E> {

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
    private final E data;

    AlinkReply(String id, int code, String message, E data) {
        this.id = id;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功
     *
     * @param reqId   请求ID
     * @param message 应答消息
     * @return 设备应答
     */
    public static AlinkReply<Void> success(String reqId, String message) {
        return success(reqId, message, null);
    }

    /**
     * 成功
     *
     * @param reqId   请求ID
     * @param message 应答消息
     * @param data    应答携带数据
     * @param <E>     应答携带数据类型
     * @return 设备应答
     */
    public static <E> AlinkReply<E> success(String reqId, String message, E data) {
        return new AlinkReply<>(reqId, ALINK_REPLY_OK, message, data);
    }

    /**
     * 失败
     *
     * @param reqId   请求ID
     * @param code    设备返回码
     * @param message 应答消息
     * @return 设备应答
     */
    public static AlinkReply<Void> failure(String reqId, int code, String message) {
        return new AlinkReply<>(reqId, code, message, null);
    }

    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    public String getReqId() {
        return id;
    }

    /**
     * 获取设备返回码
     *
     * @return 设备返回码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取应答消息
     *
     * @return 应答消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取应答携带数据
     *
     * @return 应答携带数据
     */
    public E getData() {
        return data;
    }

    /**
     * 应答是否成功
     *
     * @return TRUE | FALSE
     */
    public boolean isOk() {
        return getCode() == ALINK_REPLY_OK;
    }

}
