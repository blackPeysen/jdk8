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

package java.util.concurrent.locks;

/**
 * 一种同步器，可以由一个线程独占。
 * 这个类为创建锁和相关的同步器提供了基础，这些同步器可能包含所有权的概念。
 * {@code AbstractOwnableSynchronizer}类本身并不管理或使用这些信息。
 * 但是，子类和工具可能使用适当维护的值来帮助控制和监视访问并提供诊断。
 *
 * @since 1.6
 * @author Doug Lea
 */
public abstract class AbstractOwnableSynchronizer
    implements java.io.Serializable {

    /** 即使所有字段都是瞬态的，也要使用串行ID。 */
    private static final long serialVersionUID = 3737899427754241961L;

    /**
     * 为子类使用的空构造函数。
     */
    protected AbstractOwnableSynchronizer() { }

    /**
     * 独占模式同步的当前所有者。
     */
    private transient Thread exclusiveOwnerThread;

    /**
     * 设置当前拥有独占访问权的线程。
     * {@code null}参数表示没有线程拥有访问权限。此方法不强制任何同步或{@code volatile}字段访问。
     * @param thread the owner thread
     */
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }

    /**
     * 返回{@code setExclusiveOwnerThread}或{@code null}设置的最后一个线程集。
     * @return the owner thread
     */
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
