package com.github.athingx.athing.standard.platform.util;

/**
 * 设备平台运行时
 */
public class TpRuntime {

    private static final ThreadLocal<TpRuntime> tpRuntimeRef = new ThreadLocal<>();
    private String reqId;

    /**
     * 是否在运行时中
     *
     * @return TRUE | FALSE
     */
    public static boolean isInRuntime() {
        return null != tpRuntimeRef.get();
    }

    /**
     * 进入运行时
     */
    static void enter() {
        tpRuntimeRef.set(new TpRuntime());
    }

    /**
     * 获取当前运行时
     *
     * @return 运行时
     */
    public static TpRuntime getRuntime() {
        final TpRuntime tpRuntime = tpRuntimeRef.get();
        if (null == tpRuntime) {
            throw new IllegalStateException("not in runtime");
        }
        return tpRuntime;
    }

    /**
     * 退出当前运行时
     */
    static void exit() {
        tpRuntimeRef.remove();
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
     * 设置请求ID
     *
     * @param reqId 请求ID
     */
    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

}
