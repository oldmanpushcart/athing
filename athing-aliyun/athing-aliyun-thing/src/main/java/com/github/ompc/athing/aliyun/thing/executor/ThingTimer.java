package com.github.ompc.athing.aliyun.thing.executor;

import com.github.ompc.athing.aliyun.thing.ThingPromise;
import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 设备定时器
 */
public class ThingTimer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final ExecutorService executor;
    private final ThingTimer _this;
    private final String _string;
    private final TreeSet<Timer> timers = new TreeSet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition waiter = lock.newCondition();
    private volatile boolean running = true;

    public ThingTimer(Thing thing, ExecutorService executor) {
        this.thing = thing;
        this.executor = executor;
        this._this = this;
        this._string = String.format("%s/timer", thing);
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

    private void init() {
        executor.execute(() -> {

            logger.info("{} running", _this);
            try {
                while (running) {

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
                                    timer.promise.trySuccess(null);
                                } catch (Throwable cause) {
                                    timer.promise.tryException(cause);
                                }
                            }));

                }
            } catch (InterruptedException iCause) {
                logger.warn("{} interrupted!", _this);
            } finally {
                logger.info("{} shutdown", _this);
            }

        });
    }

    /**
     * 销毁
     */
    public void destroy() {
        lock.lock();
        try {
            running = false;
            new ArrayList<>(timers).forEach(timer -> timer.promise.tryCancel());
            waiter.signal();
        } finally {
            lock.unlock();
        }
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

        final Timer timer = new Timer(unit.toMillis(time), task, new ThingPromise<>(thing));
        lock.lock();
        try {
            // 如果定时器已经被销毁，则无法再继续添加任务
            if (!running) {
                throw new IllegalStateException(String.format("%s is not running!", _this));
            }
            timers.add(timer);
            waiter.signal();
        } finally {
            lock.unlock();
        }

        return timer.promise.onCancelled(future -> {
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
        private final ThingPromise<Void> promise;

        private Timer(long time, Task task, ThingPromise<Void> promise) {
            this.time = time;
            this.task = task;
            this.promise = promise;
        }

        @Override
        public int compareTo(Timer o) {
            return Long.compare(time, o.time);
        }

    }

}
