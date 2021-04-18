package com.github.ompc.athing.aliyun.thing.op;

import com.github.ompc.athing.aliyun.thing.ThingExecutor;
import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 设备定时器
 */
public class ThingTimer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ThingExecutor executor;
    private final String _string;
    private final TreeSet<Timer> timers = new TreeSet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition waiter = lock.newCondition();
    private volatile boolean running = true;

    /**
     * 设备定时器
     *
     * @param thing    设备
     * @param executor 设备执行器
     */
    public ThingTimer(Thing thing, ThingExecutor executor) {
        this.thing = thing;
        this.executor = executor;
        this._string = String.format("%s/timer", thing);

        // 初始化
        init();

    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 等待可被执行的定时任务到来
     *
     * @return 如果找到返回TRUE，否则返回FALSE
     * @throws InterruptedException 等待被中断
     */
    private boolean select() throws InterruptedException {
        // 没有任务则长眠
        if (timers.isEmpty()) {
            waiter.await();
            return false;
        }

        // 有任务按照第一个任务的时间差休眠
        final long now = System.currentTimeMillis();
        final Timer first = timers.first();
        return first.time <= now || !waiter.await(first.time - now, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化定时器
     */
    private void init() {

        // 定时器主线程
        executor.execute(() -> {

            logger.info("{} running", ThingTimer.this);
            try {
                while (!thing.isDestroyed() && Thread.currentThread().isInterrupted()) {

                    // 到时任务集合
                    final List<Timer> expires = new ArrayList<>();

                    lock.lock();
                    try {

                        // 等待，直到有任务
                        if (!select()) {
                            continue;
                        }

                        final long now = System.currentTimeMillis();
                        final Iterator<Timer> timerIt = timers.iterator();
                        while (timerIt.hasNext()) {
                            final Timer timer = timerIt.next();

                            // 遇到还没到时间的任务结束
                            if (timer.time > now) {
                                break;
                            }

                            // 当前任务提出来准备执行
                            timerIt.remove();
                            expires.add(timer);
                        }

                    } finally {
                        lock.unlock();
                    }


                    // 执行已经到时间的任务
                    expires.forEach(timer ->
                            executor.execute(() -> {
                                try {
                                    timer.task.execute();
                                    timer.promised.trySuccess();
                                } catch (Throwable cause) {
                                    timer.promised.tryException(cause);
                                }
                            }));

                }
            } catch (InterruptedException iCause) {
                logger.warn("{} interrupted!", ThingTimer.this);
            } finally {

                // 主动取消队列中还在排队的任务
                timers.forEach(timer -> timer.promised.tryCancel());

                // 定时任务停止
                logger.info("{} shutdown", ThingTimer.this);
            }

        });
    }

    /**
     * 添加定时任务
     * <p>任务在指定的时间点执行</p>
     *
     * @param time 时间点
     * @param unit 时间单位
     * @param task 任务
     * @return 定时任务Future
     */
    public ThingFuture<Void> task(long time, TimeUnit unit, Task task) {

        final Timer timer = new Timer(unit.toMillis(time), task, new ThingPromise<>(thing, Runnable::run));
        lock.lock();
        try {
            // 如果定时器已经被销毁，则无法再继续添加任务
            if (!running) {
                throw new IllegalStateException(String.format("%s is not running!", ThingTimer.this));
            }
            timers.add(timer);
            waiter.signal();
        } finally {
            lock.unlock();
        }

        return timer.promised
                .onCancelled(future -> {
                    lock.lock();
                    try {
                        timers.remove(timer);
                        waiter.signal();
                    } finally {
                        lock.unlock();
                    }
                });
    }

    /**
     * 销毁定时器
     */
    public void destroy() {
        lock.lock();
        try {
            running = false;
            waiter.signal();
        } finally {
            lock.unlock();
        }
        logger.info("{} is stopped!", this);
    }

    /**
     * 添加定时任务
     * <p>任务在指定的时间间隔后执行</p>
     *
     * @param time 时间间隔值
     * @param unit 时间间隔单位
     * @param task 定时任务
     * @return 定时任务future
     */
    public ThingFuture<Void> after(long time, TimeUnit unit, Task task) {
        return task(System.currentTimeMillis() + unit.toMillis(time), TimeUnit.MILLISECONDS, task);
    }

    /**
     * 任务
     */
    public interface Task {

        /**
         * 执行任务
         *
         * @throws Exception 执行任务失败
         */
        void execute() throws Exception;

    }

    /**
     * 定时器
     */
    private static class Timer implements Comparable<Timer> {

        private final long time;
        private final Task task;
        private final ThingPromise<Void> promised;

        private Timer(long time, Task task, ThingPromise<Void> promised) {
            this.time = time;
            this.task = task;
            this.promised = promised;
        }

        @Override
        public int compareTo(Timer o) {
            return Long.compare(time, o.time);
        }

    }

}
