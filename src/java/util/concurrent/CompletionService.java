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
 * 将新异步任务的产生与已完成任务结果的使用分离开来的服务。
 * 生产者{@code #submit}的任务执行。消费者{@code take}完成的任务，并按照完成的顺序处理它们的结果。
 * 一个{@code CompletionService}例如可以用来管理异步IO,在任务执行读取提交在一个程序或系统的一部分,
 * 然后采取行动在一个不同的程序,当读取完整的一部分,可能比他们要求以不同的顺序。
 *
 * <p>通常，{@code CompletionService}依赖于单独的{@link Executor}来实际执行任务，
 * 在这种情况下，{@code CompletionService}只管理一个内部的完成队列。
 * {@link ExecutorCompletionService}类提供了这种方法的实现。
 *
 * <p>内存一致性的影响:在提交任务给{@code CompletionService} <a href="package-summary "之前，
 * 线程中的动作htmlMemoryVisibility">happens -before<a> action由该任务执行，
 * 在相应的{@code take()}成功返回后，依次执行happens -beforeaction。
 */
public interface CompletionService<V> {
    /**
     * 提交一个返回值的任务以供执行，并返回表示该任务未决结果的Future。完成后，可以执行或轮询此任务。
     *
     * @param task 要提交的任务
     * @return 表示任务等待完成的未来
     * @throws RejectedExecutionException 任务无法调度执行
     * @throws NullPointerException 如果任务为空
     */
    Future<V> submit(Callable<V> task);

    /**
     * 提交一个可运行的任务执行，并返回表示该任务的Future。完成后，可以执行或轮询此任务。
     *
     * @param task 要提交的任务
     * @param result 返回一个Future，表示任务正在等待完成，其{@code get()}方法将在任务完成时返回给定的结果值
     * @throws RejectedExecutionException 任务无法调度执行
     * @throws NullPointerException 如果任务为空
     */
    Future<V> submit(Runnable task, V result);

    /**
     * 检索并删除表示下一个已完成任务的Future，如果没有的话就等待。
     *
     * @return 未来表示下一个完成的任务
     * @throws InterruptedException 如果在等待时被打断
     */
    Future<V> take() throws InterruptedException;

    /**
     * 检索并删除表示下一个已完成任务的Future，如果没有，则为{@code null}。
     *
     * @return 表示下一个已完成任务的Future，如果没有，则为{@code null}
     */
    Future<V> poll();

    /**
     * 检索并删除表示下一个已完成任务的Future，如果有必要就等待，如果还没有出现，则等待到指定的等待时间。
     *
     * @param timeout 在放弃之前等待多久，以{@code unit}为单位
     * @param unit 一个{@code TimeUnit}决定如何解释{@code timeout}参数
     * @return 未来表示下一个完成的任务，或者如果指定的等待时间在一个任务出现之前就已经过去了{@code null}
     * @throws InterruptedException 如果在等待时被打断
     */
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
}
