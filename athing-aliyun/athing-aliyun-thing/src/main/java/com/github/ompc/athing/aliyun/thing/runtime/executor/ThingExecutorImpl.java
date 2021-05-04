package com.github.ompc.athing.aliyun.thing.runtime.executor;

import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备执行器实现
 */
public class ThingExecutorImpl implements ThingExecutor {

    private final static ThreadLocal<Strategy> strategyRef = new ThreadLocal<>();
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

    private Executor choice(Strategy strategy) {
        return null == strategy
                ? inline
                : workers;
    }

    @Override
    public void execute(Runnable command) {
        final Strategy strategy = strategyRef.get();
        choice(strategy).execute(() -> {

            final Strategy current;
            if (null == strategy) {
                strategyRef.set(current = new Strategy());
            } else {
                current = strategy;
            }

            try {
                current.inlineCnt.incrementAndGet();
                command.run();
            } finally {
                if (current.inlineCnt.decrementAndGet() == 0) {
                    strategyRef.remove();
                }
            }
        });
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
     * 执行策略
     */
    private class Strategy {

        private final AtomicInteger inlineCnt = new AtomicInteger(0);

    }

}
