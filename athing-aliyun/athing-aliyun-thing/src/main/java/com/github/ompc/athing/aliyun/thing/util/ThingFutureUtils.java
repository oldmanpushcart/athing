package com.github.ompc.athing.aliyun.thing.util;

import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingFutureListener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 设备承诺工具类
 */
public class ThingFutureUtils {

    /**
     * 包装成不可取消承诺
     * @param future 承诺
     * @param <V> 类型
     * @return 不可取消的承诺
     */
    public static <V> ThingFuture<V> uncancellable(ThingFuture<V> future) {
        return new ThingFuture<V>() {

            @Override
            public Thing getThing() {
                return future.getThing();
            }

            @Override
            public boolean isFailure() {
                return future.isFailure();
            }

            @Override
            public boolean isSuccess() {
                return future.isSuccess();
            }

            @Override
            public boolean isException() {
                return future.isException();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {

                /*
                 * 因为是不可取消，所以直接返回false
                 */
                return false;

            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public V get() throws InterruptedException, ExecutionException {
                return future.get();
            }

            @Override
            public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return future.get(timeout, unit);
            }

            @Override
            public Throwable getException() {
                return future.getException();
            }

            @Override
            public V getSuccess() {
                return future.getSuccess();
            }

            @Override
            public ThingFuture<V> appendListener(ThingFutureListener<V> listener) {
                return future.appendListener(listener);
            }

            @Override
            public ThingFuture<V> removeListener(ThingFutureListener<V> listener) {
                return future.removeListener(listener);
            }

            @Override
            public ThingFuture<V> sync() throws InterruptedException, ExecutionException, CancellationException {
                return future.sync();
            }

            @Override
            public ThingFuture<V> syncUninterruptible() throws ExecutionException, CancellationException {
                return future.syncUninterruptible();
            }

            @Override
            public ThingFuture<V> await() throws InterruptedException {
                return future.await();
            }

            @Override
            public ThingFuture<V> awaitUninterruptible() {
                return future.awaitUninterruptible();
            }
        };
    }

}
