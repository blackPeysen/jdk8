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
import java.util.concurrent.blocking.queue.BlockingQueue;
import java.util.concurrent.blocking.queue.LinkedBlockingQueue;
import java.util.concurrent.future.FutureTask;
import java.util.concurrent.future.RunnableFuture;

/**
 * A {@link java.util.concurrent.executor.CompletionService} that uses a supplied {@link java.util.concurrent.executor.Executor}
 * to execute tasks.  This class arranges that submitted tasks are,
 * upon completion, placed on a queue accessible using {@code take}.
 * The class is lightweight enough to be suitable for transient use
 * when processing groups of tasks.
 *
 * <p>
 *
 * <b>Usage Examples.</b>
 *
 * Suppose you have a set of solvers for a certain problem, each
 * returning a value of some type {@code Result}, and would like to
 * run them concurrently, processing the results of each of them that
 * return a non-null value, in some method {@code use(Result r)}. You
 * could write this as:
 *
 * <pre> {@code
 * void solve(Executor e,
 *            Collection<Callable<Result>> solvers)
 *     throws InterruptedException, ExecutionException {
 *     CompletionService<Result> ecs
 *         = new ExecutorCompletionService<Result>(e);
 *     for (Callable<Result> s : solvers)
 *         ecs.submit(s);
 *     int n = solvers.size();
 *     for (int i = 0; i < n; ++i) {
 *         Result r = ecs.take().get();
 *         if (r != null)
 *             use(r);
 *     }
 * }}</pre>
 *
 * Suppose instead that you would like to use the first non-null result
 * of the set of tasks, ignoring any that encounter exceptions,
 * and cancelling all other tasks when the first one is ready:
 *
 * <pre> {@code
 * void solve(Executor e,
 *            Collection<Callable<Result>> solvers)
 *     throws InterruptedException {
 *     CompletionService<Result> ecs
 *         = new ExecutorCompletionService<Result>(e);
 *     int n = solvers.size();
 *     List<Future<Result>> futures
 *         = new ArrayList<Future<Result>>(n);
 *     Result result = null;
 *     try {
 *         for (Callable<Result> s : solvers)
 *             futures.add(ecs.submit(s));
 *         for (int i = 0; i < n; ++i) {
 *             try {
 *                 Result r = ecs.take().get();
 *                 if (r != null) {
 *                     result = r;
 *                     break;
 *                 }
 *             } catch (ExecutionException ignore) {}
 *         }
 *     }
 *     finally {
 *         for (Future<Result> f : futures)
 *             f.cancel(true);
 *     }
 *
 *     if (result != null)
 *         use(result);
 * }}</pre>
 */
public class ExecutorCompletionService<V> implements CompletionService<V> {
    private final java.util.concurrent.executor.Executor executor;
    private final java.util.concurrent.executor.AbstractExecutorService aes;
    private final java.util.concurrent.blocking.queue.BlockingQueue<java.util.concurrent.future.Future<V>> completionQueue;

    /**
     * 完成时进入队列的FutureTask扩展
     */
    private class QueueingFuture extends java.util.concurrent.future.FutureTask<Void> {
        QueueingFuture(java.util.concurrent.future.RunnableFuture<V> task) {
            super(task, null);
            this.task = task;
        }
        protected void done() { completionQueue.add(task); }
        private final java.util.concurrent.future.Future<V> task;
    }

    private java.util.concurrent.future.RunnableFuture<V> newTaskFor(Callable<V> task) {
        if (aes == null)
            return new java.util.concurrent.future.FutureTask<V>(task);
        else
            return aes.newTaskFor(task);
    }

    private java.util.concurrent.future.RunnableFuture<V> newTaskFor(Runnable task, V result) {
        if (aes == null)
            return new FutureTask<V>(task, result);
        else
            return aes.newTaskFor(task, result);
    }

    /**
     * 使用提供的基本任务执行器和创建ExecutorCompletionService {@link java.util.concurrent.blocking.queue.LinkedBlockingQueue}作为完成队列。
     *
     * @param executor the executor to use
     * @throws NullPointerException if executor is {@code null}
     */
    public ExecutorCompletionService(java.util.concurrent.executor.Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof java.util.concurrent.executor.AbstractExecutorService) ?
            (java.util.concurrent.executor.AbstractExecutorService) executor : null;
        this.completionQueue = new LinkedBlockingQueue<java.util.concurrent.future.Future<V>>();
    }

    /**
     * 使用提供的执行器执行基本任务，并将提供的队列作为其完成队列，创建ExecutorCompletionService。
     *
     * @param executor the executor to use
     * @param completionQueue 作为完成队列使用的队列通常是这个服务专用的队列。
     *                        这个队列被视为不受限制的——failed attempt {@code 队列。对于已完成任务的add}操作将导致无法检索它们。
     * @throws NullPointerException 如果executor或completionQueue是{@code null}
     */
    public ExecutorCompletionService(Executor executor,
                                     BlockingQueue<java.util.concurrent.future.Future<V>> completionQueue) {
        if (executor == null || completionQueue == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof java.util.concurrent.executor.AbstractExecutorService) ?
            (AbstractExecutorService) executor : null;
        this.completionQueue = completionQueue;
    }

    public java.util.concurrent.future.Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        java.util.concurrent.future.RunnableFuture<V> f = newTaskFor(task);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    public java.util.concurrent.future.Future<V> submit(Runnable task, V result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task, result);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    public java.util.concurrent.future.Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    public java.util.concurrent.future.Future<V> poll() {
        return completionQueue.poll();
    }

    public java.util.concurrent.future.Future<V> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

}
