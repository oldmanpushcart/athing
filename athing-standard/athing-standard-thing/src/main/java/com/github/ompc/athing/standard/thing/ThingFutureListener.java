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

    interface OnSuccess<V> extends ThingFutureListener<V> {

        @Override
        default void onDone(ThingFuture<V> future) {
            if (future.isSuccess()) {
                onSuccess(future);
            }
        }

        void onSuccess(ThingFuture<V> future);

    }

    interface OnFailure<V> extends ThingFutureListener<V> {

        @Override
        default void onDone(ThingFuture<V> future) throws Exception {
            if (future.isFailure()) {
                onFailure(future);
            }
        }

        void onFailure(ThingFuture<V> future) throws Exception;

    }

    interface OnCancelled<V> extends ThingFutureListener<V> {

        @Override
        default void onDone(ThingFuture<V> future) throws Exception {
            if (future.isCancelled()) {
                onCancelled(future);
            }
        }

        void onCancelled(ThingFuture<V> future) throws Exception;

    }

    interface OnException<V> extends ThingFutureListener<V> {
        @Override
        default void onDone(ThingFuture<V> future) throws Exception {
            if (future.isException()) {
                onException(future);
            }
        }

        void onException(ThingFuture<V> future) throws Exception;

    }

    interface OnDone<V> extends ThingFutureListener<V> {


    }

}
