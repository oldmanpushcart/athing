package com.github.athingx.athing.aliyun.thing.runtime.caller;

import com.github.athingx.athing.aliyun.thing.runtime.executor.Fulfill;
import com.github.athingx.athing.standard.thing.ThingFuture;

/**
 * 设备方法访问者
 * <p>
 * 用于描述设备方法访问行为方式，可以将一个同步的方法调用转变为异步调用。
 * </p>
 */
public interface ThingCaller {

    /**
     * 访问方法
     * <p>
     * 调用的方法通过配合使用{@link #byReturn(Fulfill)}或{@link #byEmptyReturn(Fulfill)}来返回值，可以将一个同步的方法访问转换为异步
     * </p>
     *
     * @param fulfill 访问履约
     * @param <T>     返回类型
     * @return 访问凭证
     */
    <T> ThingFuture<T> call(Fulfill<T> fulfill);

    /**
     * 方法访问返回
     * <p>
     * 1. 该方法若在{@link #call(Fulfill)}方法的访问周期内调用，则会通过抛出一个{@link ThingCallReturnException}异常实现异步访问，
     * 该异常为{@link RuntimeException}类型，请不要捕获，避免无法返回
     * </p>
     *
     * <p>
     * 2. 该方法若不在{@link #call(Fulfill)}方法的访问周期内调用，方法访问行为将会从异步转为同步，
     * 若访问过程中，方法自身抛出异常，将会被封装为{@link ThingCallTargetException}异常进行转抛
     * </p>
     *
     * @param fulfill 访问履约
     * @param <T>     返回类型
     * @return 返回值。因为是异步所以这里的返回值没有意义，主要用来约束{@link Fulfill}的泛型
     * @throws ThingCallReturnException 设备异步返回（异常）,请不要捕获
     * @throws ThingCallTargetException 设备同步访问时，方法自身抛出异常的封装异常
     */
    <T> T byReturn(Fulfill<T> fulfill) throws ThingCallReturnException, ThingCallTargetException;

    /**
     * 方法调用返回（void）
     * <p>使用情况同{@link #byReturn(Fulfill)}</p>
     */
    void byEmptyReturn(Fulfill<Void> fulfill) throws ThingCallReturnException, ThingCallTargetException;

    /**
     * 判断当前是否来处于{@link #call(Fulfill)}访问周期内
     *
     * @return TRUE | FALSE
     */
    boolean isCall();

}
