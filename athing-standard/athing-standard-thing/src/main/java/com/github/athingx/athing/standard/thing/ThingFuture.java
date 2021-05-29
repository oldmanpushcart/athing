package com.github.athingx.athing.standard.thing;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 设备凭证
 * <pre>
 * <table>
 *     <tr>
 *         <th>METHOD</th>
 *         <th>CANCEL</th>
 *         <th>EXCEPTION</th>
 *         <th>SUCCESS</th>
 *     </tr>
 *     <tr>
 *         <td>{@link #isSuccess()}</td>
 *         <td>{@code false}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isFailure()}</td>
 *         <td>{@code true}</td>
 *         <td>{@code true}</td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isCancelled()}</td>
 *         <td>{@code true}</td>
 *         <td>{@code false}</td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isException()}</td>
 *         <td>{@code false}</td>
 *         <td>{@code true}</td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #isDone()}</td>
 *         <td>{@code true}</td>
 *         <td>{@code true}</td>
 *         <td>{@code true}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #getException()}</td>
 *         <td>{@code null}</td>
 *         <td>{@code cause}</td>
 *         <td>{@code null}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link #getSuccess()}</td>
 *         <td>{@code null}</td>
 *         <td>{@code null}</td>
 *         <td>{@code value}</td>
 *     </tr>
 * </table>
 * </pre>
 *
 * @param <V> 类型
 */
public interface ThingFuture<V> extends Future<V> {

    /**
     * 获取设备
     *
     * @return 设备
     */
    Thing getThing();

    /**
     * 是否失败
     *
     * @return TRUE | FALSE
     */
    boolean isFailure();

    /**
     * 是否成功
     *
     * @return TRUE | FALSE
     */
    boolean isSuccess();

    /**
     * 是否异常
     *
     * @return TRUE | FALSE
     */
    boolean isException();

    /**
     * 是否取消
     *
     * @return TRUE | FALSE
     */
    @Override
    boolean isCancelled();

    /**
     * 获取异常
     *
     * @return 异常
     */
    Throwable getException();

    /**
     * 获取返回值
     *
     * @return 获取返回值
     */
    V getSuccess();

    /**
     * 添加监听器
     * <p>
     * {@link #isDone()} == true 的时候触发
     * </p>
     *
     * @param listener Future监听器
     * @return this
     */
    ThingFuture<V> appendListener(ThingFutureListener<V> listener);

    /**
     * 添加完成监听器
     * <p>
     * {@link #isDone()} == true 的时候触发
     * </p>
     *
     * @param listener Future监听器
     * @return this
     */
    default ThingFuture<V> onDone(ThingFutureListener.OnDone<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加成功监听器
     * <p>
     * {@link #isSuccess()} == true 的时候触发
     * </p>
     *
     * @param listener Future监听器
     * @return this
     */
    default ThingFuture<V> onSuccess(ThingFutureListener.OnSuccess<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加失败监听器
     * <p>
     * {@link #isCancelled()} ()} == true 或者 {@link #isException()} == true 的时候触发
     * </p>
     *
     * @param listener Future监听器
     * @return this
     */
    default ThingFuture<V> onFailure(ThingFutureListener.OnFailure<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加取消监听器
     * <p>
     * {@link #isCancelled()} == true 的时候触发
     * </p>
     *
     * @param listener Future监听器
     * @return this
     */
    default ThingFuture<V> onCancelled(ThingFutureListener.OnCancelled<V> listener) {
        return appendListener(listener);
    }

    /**
     * 添加异常监听器
     * <p>
     * {@link #isException()} == true 的时候触发
     * </p>
     *
     * @param listener Future监听器
     * @return this
     */
    default ThingFuture<V> onException(ThingFutureListener.OnException<V> listener) {
        return appendListener(listener);
    }

    /**
     * 移除监听器
     *
     * @param listener Future监听器
     * @return this
     */
    ThingFuture<V> removeListener(ThingFutureListener<V> listener);

    /**
     * 同步等待结果
     *
     * @return this
     * @throws InterruptedException  等待过程被中断
     * @throws ExecutionException    结果为异常
     * @throws CancellationException 结果为取消
     */
    ThingFuture<V> sync() throws InterruptedException, ExecutionException, CancellationException;

    /**
     * 同步等待结果，等待过程被中断后不会抛出异常，将继续往下执行
     *
     * @return this
     * @throws ExecutionException    结果为异常
     * @throws CancellationException 结果为取消
     */
    ThingFuture<V> syncUninterruptible() throws ExecutionException, CancellationException;

    /**
     * 阻塞并等待完成
     *
     * @return this
     * @throws InterruptedException 等待过程被中断
     */
    ThingFuture<V> await() throws InterruptedException;

    /**
     * 阻塞并等待完成，等待过程被中断后不会抛出异常，将继续往下执行
     *
     * @return this
     */
    ThingFuture<V> awaitUninterruptible();

}
