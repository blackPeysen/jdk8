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
 * {@code Future}表示异步计算的结果。
 *      提供的方法用于检查计算是否完成，等待其完成，并检索计算的结果。
 *
 * 只有当计算完成时，才可以使用方法{@code get}检索结果，如果需要，则阻塞，直到计算完成为止。
 * 取消由{@code cancel}方法执行。
 * 提供其他方法确定任务是否正常完成或已取消。
 * 一旦运算完成，就不能取消运算。
 * 如果你想使用{@code Future}的目的是可取消，但不提供一个可用的结果，
 *      你可以声明形式{@code Future<? 作为底层任务的结果>}和返回{@code null}。
 *
 * <p>
 * <b>Sample Usage</b> (Note that the following classes are all
 * made-up.)
 * <pre> {@code
 * interface ArchiveSearcher { String search(String target); }
 * class App {
 *   ExecutorService executor = ...
 *   ArchiveSearcher searcher = ...
 *   void showSearch(final String target)
 *       throws InterruptedException {
 *     Future<String> future
 *       = executor.submit(new Callable<String>() {
 *         public String call() {
 *             return searcher.search(target);
 *         }});
 *     displayOtherThings(); // do other things while searching
 *     try {
 *       displayText(future.get()); // use future
 *     } catch (ExecutionException ex) { cleanup(); return; }
 *   }
 * }}</pre>
 *
 * {@link FutureTask}类是{@code Future}的一个实现，它实现了{@code Runnable}，因此可以由{@code Executor}执行。
 * 例如，上面带有{@code submit}的构造可以被替换为:
 *  <pre> {@code
 * FutureTask<String> future =
 *   new FutureTask<String>(new Callable<String>() {
 *     public String call() {
 *       return searcher.search(target);
 *   }});
 * executor.execute(future);}</pre>
 *
 * <p>内存一致性效果:异步计算所采取的操作<a href="package-summary"。
 * 在另一个线程中，发生在跟随相应的{@code Future.get()}的操作之前。
 *
 * @see FutureTask
 * @see Executor
 * @since 1.5
 * @author Doug Lea
 * @param <V> 这个Future的{@code get}方法返回的结果类型
 */
public interface Future<V> {

    /**
     * 试图取消此任务的执行。
     * 如果任务已经完成、已经取消或由于其他原因无法取消，则此尝试将失败。
     * 如果成功，并且在调用{@code cancel}时该任务还没有启动，则该任务永远不会运行。
     * 如果任务已经启动，则{@code mayInterruptIfRunning}参数确定执行该任务的线程是否应该在尝试停止任务时被中断。
     *
     * <p>在这个方法返回之后，后续对{@link #isDone}的调用将总是返回{@code true}。
     * 如果这个方法返回了{@code true}，那么后续对{@link #isCancelled}的调用将总是返回{@code true}。
     *
     * @param mayInterruptIfRunning {@code true}如果执行该任务的线程应该被中断;
     *                              否则，允许正在进行的任务完成
     * @return {@code false} 如果任务无法取消，通常是因为它已经正常完成;{@code true}
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * 如果该任务在正常完成之前被取消，则返回{@code true}。
     *
     * @return {@code true} 如果该任务在完成之前被取消
     */
    boolean isCancelled();

    /**
     * 如果任务完成，返回{@code true}。
     *
     * 完成可能是由于正常的终止、异常或取消——在所有这些情况下，这个方法将返回{@code true}。
     *
     * @return {@code true} 如果此任务完成
     */
    boolean isDone();

    /**
     * 如果需要，等待计算完成，然后检索其结果。
     *
     * @return 计算的结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程在等待时被中断
     */
    V get() throws InterruptedException, ExecutionException;

    /**
     * 如果需要，最多等待给定的时间来完成计算，然后检索其结果(如果可用)。
     *
     * @param timeout 等待的最长时间
     * @param unit timeout参数的时间单位
     * @return 计算的结果
     * @throws CancellationException 如果计算被取消
     * @throws ExecutionException 如果计算抛出异常
     * @throws InterruptedException 如果当前线程在等待时被中断
     * @throws TimeoutException 如果等待超时
     */
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
