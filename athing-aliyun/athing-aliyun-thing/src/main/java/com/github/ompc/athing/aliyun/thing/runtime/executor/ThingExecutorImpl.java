package com.github.ompc.athing.aliyun.thing.runtime.executor;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 设备执行器实现
 */
public class ThingExecutorImpl implements ThingExecutor {

    private final static ThreadLocal<Strategy> strategyRef = new ThreadLocal<>();

    /**
     * 内联执行引擎
     */
    private final static Executor inline = Runnable::run;

    /**
     * 设备
     */
    private final Thing thing;

    /**
     * 独立执行引擎
     */
    private final Executor executor;

    /**
     * 设备定时器
     */
    private final ThingTimer timer;

    /**
     * 设备执行器实现
     *
     * @param thing    设备
     * @param executor 工作线程池
     */
    public ThingExecutorImpl(Thing thing, Executor executor) {
        this.thing = thing;
        this.executor = executor;
        this.timer = begin(executor, new ThingTimer());
    }

    private ThingTimer begin(Executor executor, ThingTimer timer) {
        executor.execute(() -> {
            final Thread thread = Thread.currentThread();
            thread.setName(String.format("%s/timer", this));
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    timer.select(System.currentTimeMillis())
                            .forEach(expire -> execute(expire.getTask()));
                }
            } catch (InterruptedException cause) {
                Thread.currentThread().interrupt();
            } finally {
                timer.cancel();
            }
        });
        return timer;
    }

    @Override
    public <T> ThingFuture<T> submit(Callable<T> task) {
        final ThingPromise<T> promise = new ThingPromiseImpl<>(thing, this);
        execute(() -> {
            try {
                promise.trySuccess(task.call());
            } catch (InterruptedException cause) {
                promise.tryCancel();
                Thread.currentThread().interrupt();
            } catch (Throwable cause) {
                promise.tryException(cause);
            }
        });
        return promise;
    }

    @Override
    public ThingFuture<Void> submit(Runnable task) {
        final ThingPromise<Void> promise = new ThingPromiseImpl<>(thing, this);
        execute(() -> {
            try {
                task.run();
                promise.trySuccess(null);
            } catch (Throwable cause) {
                promise.tryException(cause);
            }
        });
        return promise;
    }

    @Override
    public ThingFuture<Void> submit(long delay, TimeUnit unit, Runnable task) {
        final ThingPromise<Void> promise = new ThingPromiseImpl<>(thing, this);
        try {
            promise.onCancelled(future -> timer.add(delay, unit, () -> {
                try {
                    task.run();
                    promise.trySuccess(null);
                } catch (Throwable cause) {
                    promise.tryException(cause);
                }
            }).cancel());
        } catch (Throwable cause) {
            promise.tryException(cause);
        }
        return promise;
    }

    @Override
    public <V> ThingPromise<V> promise(Fulfill<V> fulfill) {
        return promise(new ThingPromiseImpl<>(thing, this), fulfill);
    }

    @Override
    public <V, T extends ThingPromise<V>> T promise(T promise, Fulfill<V> fulfill) {
        execute(() -> {
            try {
                fulfill.fulfilling(promise);
            } catch (InterruptedException cause) {
                promise.tryCancel();
                Thread.currentThread().interrupt();
            } catch (Throwable cause) {
                promise.tryException(cause);
            }
        });
        return promise;
    }

    private Executor choice() {
        return Strategy.INLINE == strategyRef.get()
                ? inline
                : executor;
    }

    @Override
    public void execute(Runnable command) {
        choice().execute(() -> {
            try {
                strategyRef.set(Strategy.INLINE);
                command.run();
            } finally {
                strategyRef.remove();
            }
        });
    }

    /**
     * 执行策略
     */
    private enum Strategy {

        /**
         * 内联
         */
        INLINE

    }

}
