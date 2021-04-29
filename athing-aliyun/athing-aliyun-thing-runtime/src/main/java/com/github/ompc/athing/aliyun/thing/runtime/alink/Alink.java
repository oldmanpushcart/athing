package com.github.ompc.athing.aliyun.thing.runtime.alink;

import com.github.ompc.athing.standard.thing.ThingReply;

import java.lang.reflect.Type;

/**
 * alink协议支撑
 */
public interface Alink {

    /**
     * 成功
     */
    int ALINK_REPLY_OK = 200;

    /**
     * 内部错误，解析请求时发生错误
     */
    int ALINK_REPLY_REQUEST_ERROR = 400;

    /**
     * 内部错误，处理请求时发生错误
     */
    int ALINK_REPLY_PROCESS_ERROR = 500;

    /**
     * 设备服务尚未定义
     */
    int ALINK_REPLY_SERVICE_NOT_PROVIDED = 5161;

    /**
     * alink协议：生成调用令牌
     * <pre>
     *     alink协议要求每一此调用请求都必须拥有唯一ID，此ID将会在一定的有效期内保持单设备唯一！
     *     当前设计的规范是：一台设备在7天内的ID保持不变，且为正整数的字符串字面量
     * </pre>
     *
     * @return 调用令牌
     */
    String generateToken();

    /**
     * alink协议：反序列化
     * <pre>
     *     将JSON字符串反序列化为目标类型
     * </pre>
     *
     * @param json   JSON字符串
     * @param typeOf 目标类型
     * @param <T>    类型
     * @return 对象实例
     */
    <T> T deserialize(String json, Type typeOf);

    /**
     * alink协议：反序列化
     * <pre>
     *     将JSON字符串反序列化为目标类
     * </pre>
     *
     * @param json    JSON字符串
     * @param classOf 目标类型
     * @param <T>     类型
     * @return 对象实例
     */
    <T> T deserialize(String json, Class<T> classOf);

    /**
     * 反序列化为设备应答
     *
     * @param json JSON字符串
     * @param <T>  应答结果类型
     * @return 设备应答实例
     */
    <T> ThingReply<T> deserializeReply(String json);

    /**
     * 反序列化
     *
     * @param json JSON字符串
     * @param <T>  目标类型
     * @return 对象实例
     */
    <T> T deserialize(String json);

    /**
     * alink协议：序列化
     * <pre>
     *     将目标对象序列化为JSON字符串
     * </pre>
     *
     * @param target 目标对象实例
     * @return JSON字符串
     */
    String serialize(Object target);

    /**
     * 成功应答
     *
     * @param token 令牌
     * @param <T>   数据类型
     * @return 成功应答实例
     */
    <T> ThingReply<T> successReply(String token);

    /**
     * 成功应答
     *
     * @param token 令牌
     * @param data  应答数据
     * @param <T>   数据类型
     * @return 成功应答实例
     */
    <T> ThingReply<T> successReply(String token, T data);

    /**
     * 失败应答
     *
     * @param token   令牌
     * @param code    应答代码
     * @param message 应答信息
     * @param <T>     数据类型
     * @return 失败应答实例
     */
    <T> ThingReply<T> failureReply(String token, int code, String message);

}
