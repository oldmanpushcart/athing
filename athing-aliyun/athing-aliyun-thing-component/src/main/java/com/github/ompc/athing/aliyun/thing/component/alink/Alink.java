package com.github.ompc.athing.aliyun.thing.component.alink;

/**
 * Alink协议
 */
public interface Alink {

    /**
     * 生成令牌
     *
     * @return 令牌
     */
    String generateToken();

    /**
     * 应答成功
     *
     * @param token 应答令牌
     * @return 应答
     */
    Reply<Void> successReply(String token);

    /**
     * 应答失败
     *
     * @param token   应答令牌
     * @param code    应答码
     * @param message 应答信息
     * @return 应答
     */
    Reply<Void> failureReply(String token, int code, String message);

    /**
     * 应答成功
     *
     * @param token 应答令牌
     * @param data  应答数据
     * @param <T>   数据类型
     * @return 应答
     */
    <T> Reply<T> successReply(String token, T data);

}
