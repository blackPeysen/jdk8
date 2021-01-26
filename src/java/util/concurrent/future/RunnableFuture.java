/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.future;

import java.util.concurrent.executor.Executor;
import java.util.concurrent.future.Future;
import java.util.concurrent.future.FutureTask;

/**
 * 一个{@link java.util.concurrent.future.Future}是{@link Runnable}。
 * 成功执行{@code run}方法会导致{@code Future}的完成，并允许访问其结果。
 *
 * @see FutureTask
 * @see Executor
 * @since 1.6
 * @author Doug Lea
 * @param <V> 这个Future的{@code get}方法返回的结果类型
 */
public interface RunnableFuture<V> extends Runnable, Future<V> {
    /**
     * 将此Future设置为其计算结果，除非它已被取消。
     */
    void run();
}
