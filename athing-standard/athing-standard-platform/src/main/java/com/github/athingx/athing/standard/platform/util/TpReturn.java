package com.github.athingx.athing.standard.platform.util;

/**
 * Tp返回
 *
 * @param <T> 返回值类型
 */
public class TpReturn<T> {

    private final String reqId;
    private final T data;

    /**
     * Tp返回
     *
     * @param reqId 请求ID
     * @param data  返回结果
     */
    public TpReturn(String reqId, T data) {
        this.reqId = reqId;
        this.data = data;
    }

    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    public String getReqId() {
        return reqId;
    }

    /**
     * 获取返回结果
     *
     * @return 返回结果
     */
    public T getData() {
        return data;
    }
}
