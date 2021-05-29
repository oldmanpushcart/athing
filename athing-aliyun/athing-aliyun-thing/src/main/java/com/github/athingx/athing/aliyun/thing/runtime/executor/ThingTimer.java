package com.github.athingx.athing.aliyun.thing.runtime.executor;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 设备定时器
 */
public class ThingTimer {

    private static final Set<Timer> empty = Collections.emptySet();
    private final TreeSet<Timer> timers = new TreeSet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition waiter = lock.newCondition();

    /**
     * 等待并提取出所有到指定时间执行的任务，
     * 如果没有找到任务则等待
     *
     * @param time 指定时间
     * @return 任务集合
     * @throws InterruptedException 等待被中断
     */
    public Set<Timer> select(long time) throws InterruptedException {
        lock.lock();
        try {

            // 任务为空，则直接无限等待
            if (timers.isEmpty()) {
                waiter.await();
                return empty;
            }

            // 第一个等待执行的任务，第一个任务是时间最靠近的任务
            final Timer first = timers.first();

            /*
             * 等待有任务可以执行，以下两种情况都表明没有任务需要执行
             * 1. 排队第一的任务时间没到
             * 2. 排队第一的任务等待过程中被提前打断
             */
            if (first.time <= time || !waiter.await(first.time - time, MILLISECONDS)) {
                return empty;
            }

            // 有任务到时间了，需要执行
            final Set<Timer> expires = new LinkedHashSet<>();

            // 循环遍历集合中所有任务，直到遇到第一个尚未到时间执行的任务为止
            for (final Iterator<Timer> timerIt = timers.iterator(); timerIt.hasNext(); ) {
                final Timer timer = timerIt.next();

                // 遇到第一个尚未到时间执行的任务，跳出循环
                if (timer.time > time) {
                    break;
                }

                // 提取出所有到时间需要执行的任务
                timerIt.remove();
                expires.add(timer);

            }

            return expires;

        } finally {
            lock.unlock();
        }

    }

    /**
     * 添加任务在指定的时间执行
     *
     * @param time 指定时间
     * @param unit 时间单位
     * @param task 执行任务
     * @return 定时器
     */
    public Timer add(long time, TimeUnit unit, Runnable task) {
        final Timer timer = new Timer(unit.toMillis(time), task);
        lock.lock();
        try {
            timers.add(timer);
            waiter.signal();
        } finally {
            lock.unlock();
        }
        return timer;
    }

    /**
     * 添加任务，在指定的时间间隔后执行
     *
     * @param time 时间间隔
     * @param unit 时间单位
     * @param task 执行任务
     * @return 定时器
     */
    public Timer delay(long time, TimeUnit unit, Runnable task) {
        return add(System.currentTimeMillis() + unit.toMillis(time), MILLISECONDS, task);
    }

    /**
     * 取消所有等待执行的任务
     */
    public void cancel() {
        final Set<Timer> cancels;
        lock.lock();
        try {
            cancels = new LinkedHashSet<>(timers);
            timers.clear();
            waiter.signal();
        } finally {
            lock.unlock();
        }
        cancels.forEach(Timer::cancel);
    }

    /**
     * 定时器
     */
    public class Timer implements Comparable<Timer> {

        private final long time;
        private final Runnable task;
        private final AtomicBoolean isCancelRef = new AtomicBoolean(false);

        private Timer(long time, Runnable task) {
            this.time = time;
            this.task = task;
        }

        /**
         * 获取执行任务
         *
         * @return 执行任务
         */
        public Runnable getTask() {
            return task;
        }

        @Override
        public int compareTo(Timer o) {
            return Long.compare(time, o.time);
        }

        /**
         * 取消任务
         */
        public void cancel() {
            if (!isCancelRef.compareAndSet(false, true)) {
                return;
            }
            lock.lock();
            try {
                if (timers.remove(this)) {
                    waiter.signal();
                }
            } finally {
                lock.unlock();
            }
        }

    }

}
