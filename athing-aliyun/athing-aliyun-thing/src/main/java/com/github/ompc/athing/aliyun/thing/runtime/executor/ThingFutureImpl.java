package com.github.ompc.athing.aliyun.thing.runtime.executor;

import com.github.ompc.athing.standard.thing.Thing;
import com.github.ompc.athing.standard.thing.ThingFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ThingFutureImpl<V> implements ThingFuture<V> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final AtomicReference<StateResult> resultRef = new AtomicReference<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    public ThingFutureImpl(Thing thing) {
        this.thing = thing;
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
    public Thing getThing() {
        return thing;
    }

    @Override
    public boolean isFailure() {
        return _isFailure(resultRef.get());
    }

    @Override
    public boolean isSuccess() {
        return _isSuccess(resultRef.get());
    }

    @Override
    public boolean isException() {
        return _isException(resultRef.get());
    }

    @Override
    public boolean isCancelled() {
        return _isCancelled(resultRef.get());
    }

    @Override
    public boolean isDone() {
        return _isDone(resultRef.get());
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
        if (_isCancelled(result)) {
            throw (CancellationException) result.value;
        }
        if (_isSuccess(result)) {
            return (V) result.value;
        }
        throw new IllegalStateException(String.format("state: %s", result.state));
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        latch.await();
        return _get(resultRef.get());
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!latch.await(timeout, unit)) {
            throw new TimeoutException();
        }
        return _get(resultRef.get());
    }

    @Override
    public Throwable getException() {
        final StateResult result = resultRef.get();
        return _isFailure(result) ? (Throwable) result.value : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getSuccess() {
        final StateResult result = resultRef.get();
        return _isSuccess(result) ? (V) result.value : null;
    }

    /**
     * 尝试取消
     *
     * @return TRUE | FALSE
     */
    public boolean tryCancel() {
        final StateResult result = new StateResult(State.CANCEL, new CancellationException());
        if (resultRef.compareAndSet(null, result)) {
            latch.countDown();
            return true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} try cancel but failure, state already: {} ", this, resultRef.get().state);
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
            latch.countDown();
            return true;
        }

        // TRACE
        if (logger.isTraceEnabled()) {
            logger.trace("{} try exception but failure, state already: {}", this, resultRef.get().state, cause);
        }

        // DEBUG
        else if (logger.isDebugEnabled()) {
            logger.debug("{} try exception but failure, state already: {} ", this, resultRef.get().state);
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
            latch.countDown();
            return true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} try success but failure, state already: {} ", this, resultRef.get().state);
        }

        return false;
    }

    /**
     * 尝试成功，并设置一个{@code null}
     *
     * @return TRUE | FALSE
     */
    public boolean trySuccess() {
        return trySuccess(null);
    }

    @Override
    public ThingFuture<V> sync() throws InterruptedException, ExecutionException, CancellationException {
        get();
        return this;
    }

    @Override
    public ThingFuture<V> syncUninterruptible() throws ExecutionException, CancellationException {
        boolean isInterrupted = false;
        try {
            while (true) {
                try {
                    sync();
                    break;
                } catch (InterruptedException e) {
                    isInterrupted = true;
                }
            }
        } finally {
            if (isInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return this;
    }

    @Override
    public ThingFuture<V> await() throws InterruptedException {
        latch.await();
        return this;
    }

    @Override
    public ThingFuture<V> awaitUninterruptible() {
        boolean isInterrupted = false;
        try {
            while (true) {
                try {
                    await();
                    break;
                } catch (InterruptedException e) {
                    isInterrupted = true;
                }
            }
        } finally {
            if (isInterrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return this;
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
