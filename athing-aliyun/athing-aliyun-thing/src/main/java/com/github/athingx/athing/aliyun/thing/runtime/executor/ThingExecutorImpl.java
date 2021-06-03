package com.github.athingx.athing.aliyun.thing.runtime.executor;

import com.github.athingx.athing.aliyun.thing.ThingBootOption;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.ThingFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 设备执行器实现
 */
public class ThingExecutorImpl implements ThingExecutor {

    private final static ThreadLocal<Runtime> runtimeRef = new ThreadLocal<>();
    private final static Executor inline = Runnable::run;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ExecutorService workers;
    private final ThingTimer timer;
    private final String _string;

    /**
     * 设备执行器实现
     *
     * @param thing  设备
     * @param option 启动参数
     */
    public ThingExecutorImpl(Thing thing, ThingBootOption option) {
        this.thing = thing;
        this._string = String.format("%s/executor", thing);
        this.workers = initWorkers(option);
        this.timer = initTimer(workers);
    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 启动工作线程池
     *
     * @param option 启动参数
     * @return 工作线程池
     */
    private ExecutorService initWorkers(ThingBootOption option) {
        return Executors.newFixedThreadPool(option.getThreads(), r -> {
            final Thread worker = new Thread(r, String.format("%s/worker/daemon", this));
            worker.setDaemon(true);
            return worker;
        });
    }

    /**
     * 启动设备定时器
     *
     * @param worker 工作线程池
     * @return 设备定时器
     */
    private ThingTimer initTimer(ExecutorService worker) {
        final ThingTimer timer = new ThingTimer();
        worker.execute(() -> {
            final Thread thread = Thread.currentThread();
            thread.setName(String.format("%s/timer", this));
            try {
                logger.info("{}/timer is running!", this);
                while (!Thread.currentThread().isInterrupted()) {
                    timer.select(System.currentTimeMillis())
                            .forEach(expire -> execute(expire.getTask()));
                }
            } catch (InterruptedException cause) {
                Thread.currentThread().interrupt();
            } finally {
                timer.cancel();
                logger.info("{}/timer is shutdown!", this);
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
    public <V> ThingPromise<V> promise() {
        return promise(null);
    }

    @Override
    public <V> ThingPromise<V> promise(Fulfill<V> fulfill) {
        return promise(new ThingPromiseImpl<>(thing, this), fulfill);
    }

    @Override
    public <V, T extends ThingPromise<V>> T promise(T promise, Fulfill<V> fulfill) {
        if (null != fulfill) {
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
        }
        return promise;
    }


    @Override
    public void execute(Runnable command) {

        final Runtime runtime = runtimeRef.get();

        // 内联
        if (null != runtime) {
            command.run();
        }

        // 独立
        else {
            workers.execute(() -> {

                try {
                    runtimeRef.set(new Runtime());
                    command.run();
                } finally {
                    runtimeRef.remove();
                }

            });
        }

    }

    /**
     * 关闭设备执行引擎
     */
    public void shutdown() {
        if (null != workers) {
            workers.shutdownNow();
        }
        logger.info("{} is shutdown!", this);
    }

    /**
     * 执行环境
     */
    private static class Runtime {

    }

}