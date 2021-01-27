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

package java.util.concurrent.executor;

import java.util.concurrent.*;
import java.util.concurrent.exception.RejectedExecutionException;
import java.util.concurrent.future.Future;

/**
 * 一种将新异步任务的产生与完成任务的结果消耗分开的服务。生产者* {@code Submit}任务要执行。
 * 消费者{@code take}完成任务并按完成顺序处理结果。
 * 例如，{@code CompletionService}可以用于管理异步I / O，其中，
 * 执行读取的任务*在程序或系统的一部分中提交，然后在程序的不同部分中执行读取完成，可能以与请求不同的顺序进行。
 *
 * <p>通常，{@code CompletionService}依赖单独的{@link Executor}实际执行任务，在这种情况下，{{code CompletionService}仅管理内部完成队列。
 * {@link ExecutorCompletionService}类提供了此方法的实现。
 *
 * <p>内存一致性影响：将任务提交到{@code CompletionService}之前在线程中执行的操作<a href="package-summary.html#MemoryVisibility"> 在发生之前</a> 该任务执行的操作，
 *      依次happen-before从相应的{@code take（）}成功返回之后的操作。
 */
public interface CompletionService<V> {
    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.  Upon completion,
     * this task may be taken or polled.
     *
     * @param task the task to submit
     * @return a Future representing pending completion of the task
     * @throws java.util.concurrent.exception.RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    java.util.concurrent.future.Future<V> submit(Callable<V> task);

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task.  Upon completion, this task may be
     * taken or polled.
     *
     * @param task the task to submit
     * @param result the result to return upon successful completion
     * @return a Future representing pending completion of the task,
     *         and whose {@code get()} method will return the given
     *         result value upon completion
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    java.util.concurrent.future.Future<V> submit(Runnable task, V result);

    /**
     * Retrieves and removes the Future representing the next
     * completed task, waiting if none are yet present.
     *
     * @return the Future representing the next completed task
     * @throws InterruptedException if interrupted while waiting
     */
    java.util.concurrent.future.Future<V> take() throws InterruptedException;

    /**
     * Retrieves and removes the Future representing the next
     * completed task, or {@code null} if none are present.
     *
     * @return the Future representing the next completed task, or
     *         {@code null} if none are present
     */
    java.util.concurrent.future.Future<V> poll();

    /**
     * Retrieves and removes the Future representing the next
     * completed task, waiting if necessary up to the specified wait
     * time if none are yet present.
     *
     * @param timeout how long to wait before giving up, in units of
     *        {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return the Future representing the next completed task or
     *         {@code null} if the specified waiting time elapses
     *         before one is present
     * @throws InterruptedException if interrupted while waiting
     */
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
}
