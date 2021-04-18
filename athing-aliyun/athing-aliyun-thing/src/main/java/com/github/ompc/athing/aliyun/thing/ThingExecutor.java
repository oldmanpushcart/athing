package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.Thing;

import java.util.concurrent.Executor;

/**
 * 设备引擎
 */
public class ThingExecutor implements Executor {

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
     * 设备引擎（内联）
     *
     * @param thing 设备
     */
    public ThingExecutor(Thing thing) {
        this.thing = thing;
        this.executor = inline;
    }

    /**
     * 设备引擎
     *
     * @param thing    设备
     * @param executor 引擎
     */
    public ThingExecutor(Thing thing, Executor executor) {
        this.thing = thing;
        this.executor = executor;
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
