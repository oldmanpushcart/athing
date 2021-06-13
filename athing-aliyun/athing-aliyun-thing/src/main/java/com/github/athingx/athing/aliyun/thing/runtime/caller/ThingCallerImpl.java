package com.github.athingx.athing.aliyun.thing.runtime.caller;

import com.github.athingx.athing.aliyun.thing.runtime.executor.Fulfill;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingExecutor;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.athingx.athing.standard.thing.ThingFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备方法访问者实现
 */
public class ThingCallerImpl implements ThingCaller {

    private final ThingExecutor executor;
    private final ThreadLocal<AtomicInteger> countRef = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    public ThingCallerImpl(ThingExecutor executor) {
        this.executor = executor;
    }

    @Override
    public <T> T byReturn(Fulfill<T> fulfill) {
        final ThingPromise<T> promise = executor.promise(fulfill);
        if (isCall()) {
            throw new ThingCallReturnException(promise);
        } else {
            try {
                return promise.get();
            } catch (InterruptedException cause) {
                throw new ThingCallTargetException(cause);
            } catch (ExecutionException cause) {
                throw new ThingCallTargetException(cause.getCause());
            }
        }
    }

    @Override
    public void byEmptyReturn(Fulfill<Void> fulfill) {
        byReturn(fulfill);
    }

    @Override
    public <T> ThingFuture<T> call(Fulfill<T> fulfill) {
        return executor.promise(promise -> {
            try {
                final AtomicInteger counter = countRef.get();
                counter.incrementAndGet();
                try {
                    fulfill.fulfilling(promise);
                } finally {
                    counter.decrementAndGet();
                }
            } catch (ThingCallReturnException cause) {
                @SuppressWarnings("unchecked") final ThingPromise<T> causeP = (ThingPromise<T>) (cause.getPromise());
                promise.acceptDone(causeP);
            }
        });
    }

    @Override
    public boolean isCall() {
        return countRef.get().get() > 0;
    }

}
