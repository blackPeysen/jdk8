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
 * 拒绝策略：不能由{@link ThreadPoolExecutor}执行的任务的处理程序。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface RejectedExecutionHandler {

    /**
     * 当{@link ThreadPoolExecutor#execute execute}不能接受任务时，{@link ThreadPoolExecutor}可以调用该方法。
     * 当没有更多的线程或队列槽可用时(因为它们的界限将被超出)，或者在执行程序关闭时，可能会发生这种情况。
     *
     * <p>在没有其他替代方法的情况下，该方法可能抛出未检查的{@link RejectedExecutionException}，
     * 该异常将传播给{@code execute}的调用者。
     *
     * @param r 请求执行的可运行任务
     * @param executor 试图执行此任务的执行程序
     * @throws RejectedExecutionException 如果没有补救办法
     */
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
