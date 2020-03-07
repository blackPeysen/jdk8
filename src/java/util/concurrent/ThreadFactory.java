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
 * 根据需要创建新线程的对象。
 * 使用线程工厂删除对{@link Thread # thread (Runnable) new thread}的调用的硬连接，
 * 允许应用程序使用特殊的线程子类、优先级等。
 *
 * <p>
 * 这个接口最简单的实现就是:
 *  <pre> {@code
 * class SimpleThreadFactory implements ThreadFactory {
 *   public Thread newThread(Runnable r) {
 *     return new Thread(r);
 *   }
 * }}</pre>
 *
 * {@link exec# defaultThreadFactory}方法提供了一个更有用的简单实现，
 *      它将创建的线程上下文设置为已知值，然后返回它。
 * @since 1.5
 * @author Doug Lea
 */
public interface ThreadFactory {

    /**
     *构造一个新的{@code线程}。
     *  实现还可以初始化优先级、名称、守护进程状态、{@code ThreadGroup}等。
     *
     * @param r 一个可由新线程实例执行的runnable
     * @return 构造的线程，或者{@code null}，如果创建线程的请求被拒绝
     */
    Thread newThread(Runnable r);
}
