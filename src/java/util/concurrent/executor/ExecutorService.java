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
import java.util.List;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.exception.ExecutionException;
import java.util.concurrent.exception.RejectedExecutionException;
import java.util.concurrent.exception.TimeoutException;
import java.util.concurrent.future.Future;

/**
 * 提供用于管理终止的方法的{@link Executor}和可以生成用于跟踪一个或多个异步任务的进度的{@link java.util.concurrent.future.Future}的方法。
 *
 * <p>可以关闭{@code ExecutorService}，这将导致它拒绝新任务。
 * 关闭{@code ExecutorService}提供了两种不同的方法。
 *      {@link #shutdown}方法将允许以前提交的任务在终止之前执行，
 *      {@link #shutdownNow}方法将防止等待任务启动并尝试停止当前正在执行的任务。
 * 终止时，执行器没有正在执行的任务，没有等待执行的任务，也不能提交新的任务。
 * 应该关闭未使用的{@code ExecutorService}，以允许回收其资源。
 *
 * <p>方法{@code submit}通过创建和返回一个{@link java.util.concurrent.future.Future}来扩展基本方法{@link Executor#execute(Runnable)}，
 * 这个{@link java.util.concurrent.future.Future}可以用来取消执行和或等待完成。
 *
 * {@code invokeAny}和{@code invokeAll}执行最常用的批量执行形式，执行一个任务集合，然后等待至少一个或全部完成。
 * (Class {@link ExecutorCompletionService}可用于编写这些方法的定制变体。)
 *
 * <p>{@link Executors}类为这个包中提供的executor服务提供工厂方法。
 *
 * <h3>Usage Examples</h3>
 *
 * 下面是一个网络服务的示意图，其中线程池服务中的线程传入请求。
 * 它使用预先配置的{@link * exec# newFixedThreadPool} factory方法:
 *
 * 下面的方法分两个阶段关闭{@code ExecutorService}，
 * 首先通过调用{@code shutdown}来拒绝传入的任务，然后在必要时调用{@code shutdown now}来取消任何滞留的任务:
 *  <pre> {@code
 * void shutdownAndAwaitTermination(ExecutorService pool) {
 *   pool.shutdown(); // Disable new tasks from being submitted
 *   try {
 *     // Wait a while for existing tasks to terminate
 *     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
 *       pool.shutdownNow(); // Cancel currently executing tasks
 *       // Wait a while for tasks to respond to being cancelled
 *       if (!pool.awaitTermination(60, TimeUnit.SECONDS))
 *           System.err.println("Pool did not terminate");
 *     }
 *   } catch (InterruptedException ie) {
 *     // (Re-)Cancel if current thread also interrupted
 *     pool.shutdownNow();
 *     // Preserve interrupt status
 *     Thread.currentThread().interrupt();
 *   }
 * }}</pre>
 *
 * <p>内存一致性的影响:线程在提交{@code Runnable}或{@code Callable}任务给{@code ExecutorService}
 * <a href="package-summary "之前的动作。htmlMemoryVisibility"><i> happens -before<i><a>该任务所采取的任何动作，依次为<i> happens -before<i>结果通过{@code Future.get()}获取。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface ExecutorService extends Executor {

    /**
     * 启动一个有序关闭，在此过程中执行先前提交的任务，但不接受任何新任务。
     * 如果已经关闭，调用将没有其他效果。
     *
     * <p>此方法不等待以前提交的任务完成执行。
     * 使用{@link #awaitTermination awaitTermination}来完成。
     *
     * @throws SecurityException 如果存在一个安全管理器，并且关闭这个ExecutorService可能会操作调用者不允许修改的线程，
     *          因为它不包含{@link java.lang。RuntimePermission}{@code ("modifyThread")}，
     *          或安全管理器的{@code checkAccess}方法拒绝访问。
     */
    void shutdown();

    /**
     * 尝试停止所有正在执行的任务，停止等待任务的处理，并返回等待执行的任务列表。
     *
     * <p>此方法不会等待正在积极执行的任务终止。
     *      使用{@link #awaitTermination awaitTermination}来做这个。
     *
     * <p>除了尽最大努力停止处理正在执行的任务外，没有任何保证。
     * 例如，典型的*实现将通过{@link Thread#interrupt}取消，因此任何未能响应中断的任务都可能永远不会终止。
     *
     * @return 从未开始执行的任务列表
     * @throws SecurityException
     * 如果存在一个安全管理器，那么关闭这个ExecutorService可能会操作调用者不允许修改的线程，
     * 因为它不包含{@link java.lang.service.Runtime#Permission}{@code ("modifyThread")}，
     * 或者安全管理器的{@code checkAccess}方法拒绝访问。
     */
    List<Runnable> shutdownNow();

    /**
     * 如果此执行程序已关闭，则返回{@code true}。
     *
     * 如果这个执行器已经关闭，则返回{@code true}
     */
    boolean isShutdown();

    /**
     * 如果所有任务都在关闭后完成，则返回{@code true}。
     * 注意，{@code isTerminated}从来不是{@code true}，
     * 除非先调用{@code shutdown}或{@code shutdownNow}。
     *
     * @return {@code true}如果所有任务都在关闭后完成
     */
    boolean isTerminated();

    /**
     * 直到所有任务在一个关机请求后完成执行，或超时发生，或当前线程被中断，以先发生的情况为准。
     *
     * @param timeout 等待的最长时间
     * @param unit timeout参数的时间单位
     * @return {@code true} 如果遗嘱执行人终止遗嘱
     *         {@code false} 如果超时在终止之前发生
     * @throws InterruptedException 如果在等待时被打断
     */
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 提交一个返回值的任务以供执行，并返回一个Future，表示该任务的未决结果。
     * Future的{@code get}方法将在成功完成后返回任务的结果。
     * <p>
     * 如果您想立即阻止等待任务，您可以使用表单{@code result = exec.submit(aCallable).get();}的结构。
     *
     * <p>注意:{@link Executor}类包含一组方法，可以转换其他一些类似关闭的对象，
     * 例如{@link java.security.PrivilegedAction} to {@link Callable}表单，以便提交。
     *
     * @param task 要提交的任务
     * @param <T> 任务结果的类型
     * @return 表示任务等待完成的未来
     * @throws java.util.concurrent.exception.RejectedExecutionException 任务无法调度执行
     * @throws NullPointerException 如果任务为空
     */
    <T> java.util.concurrent.future.Future<T> submit(Callable<T> task);

    /**
     * 提交一个可运行任务以供执行，并返回一个表示该任务的Future。
     * 将来的{@code get}方法将成功完成后返回给定的结果。
     *
     * @param task 要提交的任务
     * @param result 返回的结果
     * @param <T> 结果的类型
     * @return 表示任务未完成的将来
     * @throws java.util.concurrent.exception.RejectedExecutionException 任务无法调度执行
     * @throws NullPointerException 如果任务为空
     */
    <T> java.util.concurrent.future.Future<T> submit(Runnable task, T result);

    /**
     * 提交一个可运行任务以供执行，并返回一个表示该任务的Future。
     * 将来的{@code get}方法将在成功完成时返回{@code null}。
     *
     * @param task 要提交的任务
     * @return 表示任务等待完成的未来
     * @throws java.util.concurrent.exception.RejectedExecutionException 任务无法调度执行
     * @throws NullPointerException 如果任务为空
     */
    java.util.concurrent.future.Future<?> submit(Runnable task);

    /**
     * 执行给定的任务，当所有任务都完成时，返回包含其状态和结果的期货列表。
     * 对于返回的列表中的每个元素，{@link java.util.concurrent.future.Future#isDone}都是{@code true}。
     * 注意，一个完成的任务可以正常终止，也可以通过抛出异常终止。
     * 如果在执行此操作时修改了给定的集合，则此方法的结果是未定义的。
     *
     * @param tasks 任务的集合
     * @param <T> 任务返回值的类型
     * @return 表示任务的期货列表，与迭代器为给定任务列表生成的顺序相同，每个任务都已完成
     * @throws InterruptedException 如果在等待时中断，则未完成的任务将被取消
     * @throws NullPointerException 如果tasks或它的任何元素是{@code null}
     * @throws java.util.concurrent.exception.RejectedExecutionException 如果有任务无法调度执行
     */
    <T> List<java.util.concurrent.future.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

    /**
     * 执行给定的任务，当所有任务都完成或超时时(以最先发生的为准)，返回持有其状态和结果的期货列表。
     * 对于返回的列表中的每个元素，{@link java.util.concurrent.future.Future#isDone}都是{@code true}。
     * 返回时，未完成的任务将被取消。
     * 注意，一个完成的任务可以正常终止，也可以通过抛出异常终止。
     * 如果在执行此操作时修改了给定的集合，则此方法的结果是未定义的。
     *
     * @param tasks 任务的集合
     * @param timeout 等待的最长时间
     * @param unit timeout参数的时间单位
     * @param <T> 任务返回值的类型
     * @return 代表任务的期货列表，与迭代器为给定任务列表生成的顺序相同。
     *      如果操作没有超时，则每个任务都将完成。
     *      如果它确实超时了，那么其中一些任务将不会完成。
     * @throws InterruptedException 如果在等待时中断，则未完成的任务将被取消
     * @throws NullPointerException 如果tasks、它的任何元素或单元是{@code null}
     * @throws java.util.concurrent.exception.RejectedExecutionException 如果有任务无法调度执行
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 执行给定的任务，如果有成功完成的任务(即不抛出异常)，则返回成功完成的任务的结果。
     * 在正常或异常返回时，未完成的任务将被取消。
     * 如果在执行此操作时修改了给定的集合，则此方法的结果是未定义的。
     *
     * @param tasks 任务的集合
     * @param <T> 任务返回值的类型
     * @return 其中一个任务返回的结果
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if tasks or any element task
     *         subject to execution is {@code null}
     * @throws IllegalArgumentException if tasks is empty
     * @throws java.util.concurrent.exception.ExecutionException if no task successfully completes
     * @throws java.util.concurrent.exception.RejectedExecutionException if tasks cannot be scheduled
     *         for execution
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, java.util.concurrent.exception.ExecutionException;

    /**
     * 执行给定的任务，返回一个已经成功完成的任务的结果，而不抛出异常，如果在给定超时超时之前有任何操作。
     * 在正常或异常情况下，未完成的任务将被取消。
     * 如果在操作过程中修改了给定的集合，则此方法的结果是未定义的。
     *
     * @param tasks 任务的集合
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @param <T> the type of the values returned from the tasks
     * @return the result returned by one of the tasks
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if tasks, or unit, or any element
     *         task subject to execution is {@code null}
     * @throws java.util.concurrent.exception.TimeoutException if the given timeout elapses before
     *         any task successfully completes
     * @throws java.util.concurrent.exception.ExecutionException 如果没有任务成功完成
     * @throws RejectedExecutionException 任务无法调度执行
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
