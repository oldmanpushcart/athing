package com.github.athingx.athing.standard.platform.util;

/**
 * 设备平台返回工具类
 */
public class TpReturnHelper {

    /**
     * 获取平台返回
     *
     * @param getReturn 获取返回值
     * @param <T>       返回值类型
     * @return 平台返回
     * @throws Exception 获取平台返回值异常
     */
    public static <T> TpReturn<T> getTpReturn(GetReturn<T> getReturn) throws Exception {

        // 进入运行时
        TpRuntime.enter();
        try {

            // 在运行时中执行平台的访问
            // 设备平台的实现方需要按照约定在运行时中完成上下文设置
            final T data = getReturn.getReturn();
            final TpRuntime tpRuntime = TpRuntime.getRuntime();
            return new TpReturn<>(tpRuntime.getReqId(), data);

        } finally {

            // 退出运行时
            TpRuntime.exit();
        }

    }

    /**
     * 获取空返回
     *
     * @param getEmptyReturn 获取空返回，只单纯执行不关注返回值
     * @return 空返回
     * @throws Exception 执行方法异常
     */
    public static TpEmptyReturn getTpEmptyReturn(GetEmptyReturn getEmptyReturn) throws Exception {
        // 进入运行时
        TpRuntime.enter();
        try {

            // 在运行时中执行Tp的访问
            // 实现方需要按照约定在运行时中完成上下文设置
            getEmptyReturn.getEmptyReturn();
            final TpRuntime tpRuntime = TpRuntime.getRuntime();
            return new TpEmptyReturn(tpRuntime.getReqId());

        } finally {

            // 退出运行时
            TpRuntime.exit();
        }
    }

    /**
     * 执行具体组件方法，获取返回值
     *
     * @param <T> 返回值类型
     */
    public interface GetReturn<T> {

        /**
         * 获取返回值
         *
         * @return 返回值
         * @throws Exception 执行方法异常
         */
        T getReturn() throws Exception;
    }

    /**
     * 获取空返回值
     */
    public interface GetEmptyReturn {

        /**
         * 获取空返回
         *
         * @throws Exception 执行方法异常
         */
        void getEmptyReturn() throws Exception;

    }

}
