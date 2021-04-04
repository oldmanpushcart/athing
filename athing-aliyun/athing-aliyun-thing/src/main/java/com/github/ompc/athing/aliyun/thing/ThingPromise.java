package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import com.github.ompc.athing.standard.thing.ThingFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 设备Promise
 *
 * @param <V> 类型
 */
public class ThingPromise<V> implements ThingFuture<V> {

    private final static AtomicInteger sequencer = new AtomicInteger(100000);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final AtomicReference<StateResult> resultRef = new AtomicReference<>();
    private final Collection<ThingFutureListener<V>> listeners = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition waiter = lock.newCondition();
    private final ThingPromise<V> _this;
    private final String _string;

    private volatile boolean notified;

    /**
     * 设备Promise
     *
     * @param thing 设备
     */
    public ThingPromise(Thing thing) {
        this(thing, null);
    }

    /**
     * 设备Promise
     *
     * @param thing       设备
     * @param initializer 初始化
     */
    public ThingPromise(Thing thing, Initializer<V> initializer) {
        this.thing = thing;
        this._this = this;
        this._string = String.format("%s/promise/%d", thing, sequencer.getAndIncrement());
        if (null != initializer) {
            try {
                initializer.initialize(this);
            } catch (Throwable cause) {
                tryException(cause);
            }
        }
    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public Thing getThing() {
        return thing;
    }

    private boolean _isDone(StateResult result) {
        return null != result;
    }

    private boolean _isCancelled(StateResult result) {
        return _isDone(result) && result.state == State.CANCEL;
    }

    private boolean _isException(StateResult result) {
        return _isDone(result) && result.state == State.EXCEPTION;
    }

    private boolean _isSuccess(StateResult result) {
        return _isDone(result) && result.state == State.SUCCESS;
    }

    private boolean _isFailure(StateResult result) {
        return _isException(result) || _isCancelled(result);
    }

    @Override
    public boolean isCancelled() {
        return _isCancelled(resultRef.get());
    }

    @Override
    public boolean isException() {
        return _isException(resultRef.get());
    }

    @Override
    public boolean isSuccess() {
        return _isSuccess(resultRef.get());
    }

    @Override
    public boolean isFailure() {
        return _isFailure(resultRef.get());
    }

    @Override
    public boolean isDone() {
        return _isDone(resultRef.get());
    }

    /**
     * 尝试取消
     *
     * @return TRUE | FALSE
     */
    public boolean tryCancel() {
        final StateResult result = new StateResult(State.CANCEL, null);
        if (resultRef.compareAndSet(null, result)) {
            wakeup();
            notifyListeners();
            return true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} try cancel but failure, state already: {} ", _this, resultRef.get().state);
        }

        return false;
    }

    /**
     * 尝试异常
     *
     * @param cause 异常原因
     * @return TRUE | FALSE
     */
    public boolean tryException(Throwable cause) {
        final StateResult result = new StateResult(State.EXCEPTION, cause);
        if (resultRef.compareAndSet(null, result)) {
            wakeup();
            notifyListeners();
            return true;
        }

        // TRACE
        if (logger.isTraceEnabled()) {
            logger.trace("{} try exception but failure, state already: {}", _this, resultRef.get().state, cause);
        }

        // DEBUG
        else if (logger.isDebugEnabled()) {
            logger.debug("{} try exception but failure, state already: {} ", _this, resultRef.get().state);
        }

        return false;
    }

    /**
     * 尝试成功
     *
     * @param value 成功值
     * @return TRUE | FALSE
     */
    public boolean trySuccess(V value) {
        final StateResult result = new StateResult(State.SUCCESS, value);
        if (resultRef.compareAndSet(null, result)) {
            wakeup();
            notifyListeners();
            return true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} try success but failure, state already: {} ", _this, resultRef.get().state);
        }

        return false;
    }

    /**
     * 唤醒所有等待的线程
     */
    private void wakeup() {
        lock.lock();
        try {
            waiter.signalAll();
        } finally {
            lock.unlock();
        }
    }


    /**
     * 当前Promise接受目标Future的全部结果，
     * 当目标Future完成时会联动当前Promise也跟随着尝试完成，并且设置为同样的结果
     *
     * @param acceptor 目标Future
     * @return this
     */
    public ThingPromise<V> accept(ThingFuture<V> acceptor) {
        acceptor.onDone(future -> {

            // 接受异常
            if (future.isException()) {
                if (tryException(future.getException())) {
                    logger.debug("{} accept {} exception", _this, acceptor);
                }
            }

            // 接受取消
            else if (future.isCancelled()) {
                if (tryCancel()) {
                    logger.debug("{} accept {} cancel", _this, acceptor);
                }
            }

            // 接受成功
            else if (future.isSuccess()) {
                if (trySuccess(future.getSuccess())) {
                    logger.debug("{} accept {} success", _this, acceptor);
                }
            }
        });
        return this;
    }

    /**
     * 当前Promise接受目标Future的全部失败，
     * 当目标Future失败时会联动当前Promise也跟随着尝试失败，并且设置为同样的失败原因
     *
     * @param acceptor 目标Future
     * @return this
     */
    public ThingPromise<V> acceptFailure(ThingFuture<?> acceptor) {
        acceptor.onFailure(future -> {

            // 接受异常
            if (future.isException()) {
                if (tryException(future.getException())) {
                    logger.debug("{} accept {} exception", _this, acceptor);
                }
            }

            // 接受取消
            else if (future.isCancelled()) {
                if (tryCancel()) {
                    logger.debug("{} accept {} cancel", _this, acceptor);
                }
            }
        });
        return this;
    }

    /**
     * 返回自己，本身无意义
     *
     * @return this
     */
    public ThingPromise<V> self() {
        return this;
    }

    /**
     * 设置取消，如果设置失败则抛出异常
     *
     * @return this
     */
    public ThingPromise<V> setCancel() {
        if (!tryCancel()) {
            throw new IllegalStateException("state already: " + resultRef.get().state);
        }
        return this;
    }

    /**
     * 设置异常，如果设置失败则抛出异常
     *
     * @param cause 异常原因
     * @return this
     */
    public ThingPromise<V> setException(Throwable cause) {
        if (!tryException(cause)) {
            throw new IllegalStateException("state already: " + resultRef.get().state);
        }
        return this;
    }

    /**
     * 设置成功，如果设置失败则抛出异常
     *
     * @param value 结果值
     * @return this
     */
    public ThingPromise<V> setSuccess(V value) {
        if (!trySuccess(value)) {
            throw new IllegalStateException("state already: " + resultRef.get().state);
        }
        return this;
    }

    @Override
    public Throwable getException() {
        final StateResult result = resultRef.get();
        return _isException(result) ? (Throwable) result.value : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getSuccess() {
        final StateResult result = resultRef.get();
        return _isSuccess(result) ? (V) result.value : null;
    }

    @Override
    public ThingFuture<V> appendListener(ThingFutureListener<V> listener) {

        // 是否需要自行通知
        final boolean self;

        // 如果已经整体通知过了，则需要自己通知
        if (notified) {
            self = true;
        } else {
            synchronized (listeners) {

                // 如果尚未整体通知，则添加到监听器集合等待整体通知
                // 如果已经整体通知，则需要自行通知
                if (!(self = notified)) {
                    listeners.add(listener);
                }

            }
        }

        // 自行通知
        if (self) {
            notifyListener(listener);
        }

        return this;
    }

    @Override
    public ThingFuture<V> removeListener(ThingFutureListener<V> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
        return this;
    }

    @Override
    public ThingFuture<V> waitingForDone() throws InterruptedException {
        if (isDone()) {
            return this;
        }
        lock.lock();
        try {
            if (!isDone()) {
                waiter.await();
            }
            return this;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 通知单个监听器
     *
     * @param listener 监听器
     */
    private void notifyListener(ThingFutureListener<V> listener) {
        try {
            listener.onDone(_this);
        } catch (Throwable cause) {
            logger.warn("{} notify listener error!", _this);
        }
    }

    /**
     * 通知所有监听器
     */
    private void notifyListeners() {

        if (notified) {
            return;
        }

        final Collection<ThingFutureListener<V>> notifies;
        synchronized (listeners) {

            if (notified) {
                return;
            }

            notified = true;
            notifies = new ArrayList<>(listeners);
            listeners.clear();

        }

        // 进行整体通知
        notifies.forEach(this::notifyListener);

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return tryCancel();
    }

    @SuppressWarnings("unchecked")
    private V _get(StateResult result) throws ExecutionException {
        if (_isException(result)) {
            throw new ExecutionException((Throwable) result.value);
        }
        if (_isSuccess(result)) {
            return (V) result.value;
        }
        return null;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        final StateResult ret1 = resultRef.get();
        if (_isDone(ret1)) {
            return _get(ret1);
        }
        lock.lock();
        try {
            final StateResult ret2 = resultRef.get();
            if (_isDone(ret2)) {
                return _get(ret2);
            }
            waiter.await();
            return _get(resultRef.get());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final StateResult ret1 = resultRef.get();
        if (_isDone(ret1)) {
            return _get(ret1);
        }
        lock.lock();
        try {
            final StateResult ret2 = resultRef.get();
            if (_isDone(ret2)) {
                return _get(ret2);
            }
            if (!waiter.await(timeout, unit)) {
                throw new TimeoutException();
            }
            return _get(resultRef.get());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 状态
     */
    private enum State {

        /**
         * 成功
         */
        SUCCESS,

        /**
         * 异常
         */
        EXCEPTION,

        /**
         * 取消
         */
        CANCEL

    }

    /**
     * 初始化
     */
    public interface Initializer<V> {

        /**
         * 开始初始化
         *
         * @param promise promise
         * @throws Throwable 初始化失败
         */
        void initialize(ThingPromise<V> promise) throws Throwable;

    }

    /**
     * 状态结果
     */
    private static class StateResult {

        private final State state;
        private final Object value;

        /**
         * 状态结果
         *
         * @param state 状态值
         * @param value 结果值
         */
        private StateResult(State state, Object value) {
            this.state = state;
            this.value = value;
        }

    }

}
