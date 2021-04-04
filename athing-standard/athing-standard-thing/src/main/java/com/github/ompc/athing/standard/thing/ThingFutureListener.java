package com.github.ompc.athing.standard.thing;

/**
 * 设备Future监听器
 *
 * @param <V> 类型
 */
public interface ThingFutureListener<V> {

    /**
     * 设备Future完成
     *
     * @param future 设备Future
     * @throws Exception 执行出错
     */
    void onDone(ThingFuture<V> future) throws Exception;

    /**
     * 成功监听器
     *
     * @param <V> 类型
     */
    interface OnSuccess<V> extends ThingFutureListener<V> {

        @Override
        default void onDone(ThingFuture<V> future) throws Exception {
            if (future.isSuccess()) {
                onSuccess(future);
            }
        }

        /**
         * 成功
         *
         * @param future 设备future
         * @throws Exception 监听失败
         */
        void onSuccess(ThingFuture<V> future) throws Exception;

    }

    /**
     * 失败监听器
     *
     * @param <V> 类型
     */
    interface OnFailure<V> extends ThingFutureListener<V> {

        @Override
        default void onDone(ThingFuture<V> future) throws Exception {
            if (future.isFailure()) {
                onFailure(future);
            }
        }

        /**
         * 失败
         *
         * @param future 设备future
         * @throws Exception 监听失败
         */
        void onFailure(ThingFuture<V> future) throws Exception;

    }

    /**
     * 取消监听器
     *
     * @param <V> 类型
     */
    interface OnCancelled<V> extends ThingFutureListener<V> {

        @Override
        default void onDone(ThingFuture<V> future) throws Exception {
            if (future.isCancelled()) {
                onCancelled(future);
            }
        }

        /**
         * 取消
         *
         * @param future 设备future
         * @throws Exception 监听失败
         */
        void onCancelled(ThingFuture<V> future) throws Exception;

    }

    /**
     * 异常监听器
     *
     * @param <V> 类型
     */
    interface OnException<V> extends ThingFutureListener<V> {
        @Override
        default void onDone(ThingFuture<V> future) throws Exception {
            if (future.isException()) {
                onException(future);
            }
        }

        /**
         * 异常
         *
         * @param future 设备future
         * @throws Exception 监听失败
         */
        void onException(ThingFuture<V> future) throws Exception;

    }

    /**
     * 完成监听器
     *
     * @param <V> 类型
     */
    interface OnDone<V> extends ThingFutureListener<V> {


    }

}
