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

package java.util.concurrent;

/**
 * 返回结果并可能引发异常的任务。实现者定义了一个没有参数的方法{@code call}。
 *
 * <p>{@code Callable}接口类似于{@link java.lang。因为它们都是为那些实例可能由另一个线程执行的类设计的。
 * 但是，{@code Runnable}不返回结果，也不能抛出已检查的异常。
 *
 * <p>{@link Executor}类包含实用程序方法，用于将其他常见表单转换为{@code Callable}类。
 *
 * @see Executor
 * @since 1.5
 * @author Doug Lea
 * @param <V> the result type of method {@code call}
 */
@FunctionalInterface
public interface Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V call() throws Exception;
}
