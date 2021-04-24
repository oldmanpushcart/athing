package com.github.ompc.athing.aliyun.thing.component.alink;

/**
 * Alink应答
 *
 * @param <T> 应答数据类型
 */
public class Reply<T> {

    /**
     * 成功
     */
    public static final int REPLY_OK = 200;

    /**
     * 请求错误
     */
    public static final int REPLY_REQUEST_ERROR = 400;

    private final String id;
    private final int code;
    private final String message;
    private final T data;

    /**
     * Alink应答
     *
     * @param token   应答令牌
     * @param code    应答代码
     * @param message 应答信息
     * @param data    应答数据
     */
    public Reply(String token, int code, String message, T data) {
        this.id = token;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取应答令牌
     *
     * @return 应答令牌
     */
    public String getToken() {
        return id;
    }

    /**
     * 获取应答代码
     *
     * @return 应答代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取应答信息
     *
     * @return 应答信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取应答数据
     *
     * @return 应答数据
     */
    public T getData() {
        return data;
    }

    /**
     * 判断应答是否成功
     *
     * @return TRUE | FALSE
     */
    public boolean isOk() {
        return getCode() == REPLY_OK;
    }

}
