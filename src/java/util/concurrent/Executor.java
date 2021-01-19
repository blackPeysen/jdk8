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
 * 一个执行已提交{@link Runnable}任务的对象。
 * 这个接口提供了一种将任务提交与每个任务如何运行的机制解耦的方法，包括线程使用、调度等细节。
 * 通常使用{@code Executor}而不是显式地创建线程。
 * 例如，对于一组任务的每个调用{@code new Thread(new(RunnableTask())).start()}，您可以使用:
 *      Executor executor = <em>anExecutor</em>;
 *      executor.execute(new RunnableTask1());
 *      executor.execute(new RunnableTask2());
 *
 * 但是，{@code Executor}接口并不严格要求执行是异步的。
 * 在最简单的情况下，executor可以在调用者的线程中立即运行提交的任务:
 *
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 *
 * 更典型的情况是，任务在调用者的线程之外的其他线程中执行。下面的执行器为每个任务生成一个新线程。
 *
 *  <pre> {@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }}</pre>
 *
 * 许多{@code Executor}实现对如何以及何时调度任务施加了某种限制。
 * 下面的执行程序将任务的提交序列化到第二个执行程序，演示了复合执行程序。
 *
 *  <pre> {@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
 *   final Executor executor;
 *   Runnable active;
 *
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *
 *   public synchronized void execute(final Runnable r) {
 *     tasks.offer(new Runnable() {
 *       public void run() {
 *         try {
 *           r.run();
 *         } finally {
 *           scheduleNext();
 *         }
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }}</pre>
 *
 * 这个包中提供的{@code Executor}实现实现了{@link ExecutorService}，这是一个更广泛的接口。
 * {@link ThreadPoolExecutor}类提供了一个可扩展的线程池实现。
 * {@link Executors}类为这些执行器提供了方便的工厂方法。
 *
 * <p>内存一致性效果:在一个线程中，
 * 在提交一个{@code Runnable}对象给{@code Executor}之前的操作发生在之前，它的执行可能在另一个线程中开始。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Executor {

    /**
     * 在将来的某个时候执行给定的命令。
     * 命令可以在新线程、池化线程或调用线程中执行，由{@code Executor}实现决定。
     *
     * @param command 可运行的任务
     * @throws RejectedExecutionException 如果此任务不能接受执行
     * @throws NullPointerException 如果命令为空
     */
    void execute(Runnable command);
}
