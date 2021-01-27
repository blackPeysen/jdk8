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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.exception.CancellationException;
import java.util.concurrent.exception.ExecutionException;
import java.util.concurrent.exception.RejectedExecutionException;
import java.util.concurrent.exception.TimeoutException;
import java.util.concurrent.future.FutureTask;
import java.util.concurrent.future.RunnableFuture;

/**
 * 提供{@link ExecutorService} 执行方法的默认实现。
 * 这个类使用
 *  {@code newTaskFor}返回的{@link java.util.concurrent.future.RunnableFuture}实现
 *  {@code submit}、
 *  {@code invokeAny}和{@code invokeAll}方法，
 *  {@code newTaskFor}在这个包中提供的{@link java.util.concurrent.future.FutureTask}类的默认值。
 *
 * 例如，{@code submit(Runnable)}的实现创建了一个关联的{@code RunnableFuture}，该{@code RunnableFuture}被执行并返回。
 * 子类可以覆盖{@code newTaskFor}方法来返回{@code RunnableFuture}实现，而不是{@code FutureTask}。
 *
 * @since 1.5
 * @author Doug Lea
 */
public abstract class AbstractExecutorService implements ExecutorService {

    /**
     * 为给定的runnable和默认值返回{@code RunnableFuture}。
     *
     * @param runnable 正在包装的可运行任务
     * @param value 返回的future的默认值
     * @param <T> 给定值的类型
     * @return {@code RunnableFuture}，它在运行时将运行底层的runnable，作为{@code Future}，
     *          它将产生给定的值作为其结果，并提供取消底层任务
     * @since 1.6
     */
    protected <T> java.util.concurrent.future.RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new java.util.concurrent.future.FutureTask<T>(runnable, value);
    }

    /**
     * 为给定的可调用任务返回{@code RunnableFuture}。
     *
     * @param callable 被包装的可调用任务
     * @param <T> 可调用对象的结果类型
     * @return {@code RunnableFuture}，当运行时，将调用底层可调用对象，作为{@code Future}，
     *          将产生可调用对象的结果作为其结果，并提供底层任务的取消
     * @since 1.6
     */
    protected <T> java.util.concurrent.future.RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(callable);
    }

    /**
     * @throws java.util.concurrent.exception.RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public java.util.concurrent.future.Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        java.util.concurrent.future.RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    /**
     * @throws java.util.concurrent.exception.RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <T> java.util.concurrent.future.Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        java.util.concurrent.future.RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <T> java.util.concurrent.future.Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        java.util.concurrent.future.RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    /**
     * invokeAny的主要机制
     */
    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks,
                              boolean timed, long nanos)
        throws InterruptedException, java.util.concurrent.exception.ExecutionException,
            java.util.concurrent.exception.TimeoutException {
        if (tasks == null)
            throw new NullPointerException();
        int ntasks = tasks.size();
        if (ntasks == 0)
            throw new IllegalArgumentException();
        ArrayList<java.util.concurrent.future.Future<T>> futures = new ArrayList<java.util.concurrent.future.Future<T>>(ntasks);
        ExecutorCompletionService<T> ecs =
            new ExecutorCompletionService<T>(this);

        // For efficiency, especially in executors with limited
        // parallelism, check to see if previously submitted tasks are
        // done before submitting more of them. This interleaving
        // plus the exception mechanics account for messiness of main
        // loop.

        try {
            // Record exceptions so that if we fail to obtain any
            // result, we can throw the last exception we got.
            java.util.concurrent.exception.ExecutionException ee = null;
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Iterator<? extends Callable<T>> it = tasks.iterator();

            // Start one task for sure; the rest incrementally
            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;

            for (;;) {
                java.util.concurrent.future.Future<T> f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    }
                    else if (active == 0)
                        break;
                    else if (timed) {
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                        if (f == null)
                            throw new java.util.concurrent.exception.TimeoutException();
                        nanos = deadline - System.nanoTime();
                    }
                    else
                        f = ecs.take();
                }
                if (f != null) {
                    --active;
                    try {
                        return f.get();
                    } catch (java.util.concurrent.exception.ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new java.util.concurrent.exception.ExecutionException(rex);
                    }
                }
            }

            if (ee == null)
                ee = new java.util.concurrent.exception.ExecutionException();
            throw ee;

        } finally {
            for (int i = 0, size = futures.size(); i < size; i++)
                futures.get(i).cancel(true);
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, java.util.concurrent.exception.ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (java.util.concurrent.exception.TimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                           long timeout, TimeUnit unit)
        throws InterruptedException, java.util.concurrent.exception.ExecutionException,
            java.util.concurrent.exception.TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    public <T> List<java.util.concurrent.future.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException {
        if (tasks == null)
            throw new NullPointerException();
        ArrayList<java.util.concurrent.future.Future<T>> futures = new ArrayList<java.util.concurrent.future.Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f);
                execute(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++) {
                java.util.concurrent.future.Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (java.util.concurrent.exception.CancellationException ignore) {
                    } catch (java.util.concurrent.exception.ExecutionException ignore) {
                    }
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(true);
        }
    }

    public <T> List<java.util.concurrent.future.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                         long timeout, TimeUnit unit)
        throws InterruptedException {
        if (tasks == null)
            throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        ArrayList<java.util.concurrent.future.Future<T>> futures = new ArrayList<java.util.concurrent.future.Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> t : tasks)
                futures.add(newTaskFor(t));

            final long deadline = System.nanoTime() + nanos;
            final int size = futures.size();

            // Interleave time checks and calls to execute in case
            // executor doesn't have any/much parallelism.
            for (int i = 0; i < size; i++) {
                execute((Runnable)futures.get(i));
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L)
                    return futures;
            }

            for (int i = 0; i < size; i++) {
                java.util.concurrent.future.Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    if (nanos <= 0L)
                        return futures;
                    try {
                        f.get(nanos, TimeUnit.NANOSECONDS);
                    } catch (CancellationException ignore) {
                    } catch (ExecutionException ignore) {
                    } catch (TimeoutException toe) {
                        return futures;
                    }
                    nanos = deadline - System.nanoTime();
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(true);
        }
    }

}
