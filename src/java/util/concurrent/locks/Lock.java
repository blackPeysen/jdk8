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
import java.util.concurrent.TimeUnit;

/**
 * {@code Lock} implementations provide more extensive locking
 * operations than can be obtained using {@code synchronized} methods
 * and statements.  They allow more flexible structuring, may have
 * quite different properties, and may support multiple associated
 * {@link Condition} objects.
 *
 * <p>A lock is a tool for controlling access to a shared resource by
 * multiple threads. Commonly, a lock provides exclusive access to a
 * shared resource: only one thread at a time can acquire the lock and
 * all access to the shared resource requires that the lock be
 * acquired first. However, some locks may allow concurrent access to
 * a shared resource, such as the read lock of a {@link ReadWriteLock}.
 *
 * <p>The use of {@code synchronized} methods or statements provides
 * access to the implicit monitor lock associated with every object, but
 * forces all lock acquisition and release to occur in a block-structured way:
 * when multiple locks are acquired they must be released in the opposite
 * order, and all locks must be released in the same lexical scope in which
 * they were acquired.
 *
 * <p>While the scoping mechanism for {@code synchronized} methods
 * and statements makes it much easier to program with monitor locks,
 * and helps avoid many common programming errors involving locks,
 * there are occasions where you need to work with locks in a more
 * flexible way. For example, some algorithms for traversing
 * concurrently accessed data structures require the use of
 * &quot;hand-over-hand&quot; or &quot;chain locking&quot;: you
 * acquire the lock of node A, then node B, then release A and acquire
 * C, then release B and acquire D and so on.  Implementations of the
 * {@code Lock} interface enable the use of such techniques by
 * allowing a lock to be acquired and released in different scopes,
 * and allowing multiple locks to be acquired and released in any
 * order.
 *
 * <p>With this increased flexibility comes additional
 * responsibility. The absence of block-structured locking removes the
 * automatic release of locks that occurs with {@code synchronized}
 * methods and statements. In most cases, the following idiom
 * should be used:
 *
 *  <pre> {@code
 * Lock l = ...;
 * l.lock();
 * try {
 *   // access the resource protected by this lock
 * } finally {
 *   l.unlock();
 * }}</pre>
 *
 * When locking and unlocking occur in different scopes, care must be
 * taken to ensure that all code that is executed while the lock is
 * held is protected by try-finally or try-catch to ensure that the
 * lock is released when necessary.
 *
 * <p>{@code Lock} implementations provide additional functionality
 * over the use of {@code synchronized} methods and statements by
 * providing a non-blocking attempt to acquire a lock ({@link
 * #tryLock()}), an attempt to acquire the lock that can be
 * interrupted ({@link #lockInterruptibly}, and an attempt to acquire
 * the lock that can timeout ({@link #tryLock(long, TimeUnit)}).
 *
 * <p>A {@code Lock} class can also provide behavior and semantics
 * that is quite different from that of the implicit monitor lock,
 * such as guaranteed ordering, non-reentrant usage, or deadlock
 * detection. If an implementation provides such specialized semantics
 * then the implementation must document those semantics.
 *
 * <p>Note that {@code Lock} instances are just normal objects and can
 * themselves be used as the target in a {@code synchronized} statement.
 * Acquiring the
 * monitor lock of a {@code Lock} instance has no specified relationship
 * with invoking any of the {@link #lock} methods of that instance.
 * It is recommended that to avoid confusion you never use {@code Lock}
 * instances in this way, except within their own implementation.
 *
 * <p>Except where noted, passing a {@code null} value for any
 * parameter will result in a {@link NullPointerException} being
 * thrown.
 *
 * <h3>Memory Synchronization</h3>
 *
 * <p>All {@code Lock} implementations <em>must</em> enforce the same
 * memory synchronization semantics as provided by the built-in monitor
 * lock, as described in
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * The Java Language Specification (17.4 Memory Model)</a>:
 * <ul>
 * <li>A successful {@code lock} operation has the same memory
 * synchronization effects as a successful <em>Lock</em> action.
 * <li>A successful {@code unlock} operation has the same
 * memory synchronization effects as a successful <em>Unlock</em> action.
 * </ul>
 *
 * Unsuccessful locking and unlocking operations, and reentrant
 * locking/unlocking operations, do not require any memory
 * synchronization effects.
 *
 * <h3>Implementation Considerations</h3>
 *
 * <p>The three forms of lock acquisition (interruptible,
 * non-interruptible, and timed) may differ in their performance
 * characteristics, ordering guarantees, or other implementation
 * qualities.  Further, the ability to interrupt the <em>ongoing</em>
 * acquisition of a lock may not be available in a given {@code Lock}
 * class.  Consequently, an implementation is not required to define
 * exactly the same guarantees or semantics for all three forms of
 * lock acquisition, nor is it required to support interruption of an
 * ongoing lock acquisition.  An implementation is required to clearly
 * document the semantics and guarantees provided by each of the
 * locking methods. It must also obey the interruption semantics as
 * defined in this interface, to the extent that interruption of lock
 * acquisition is supported: which is either totally, or only on
 * method entry.
 *
 * <p>As interruption generally implies cancellation, and checks for
 * interruption are often infrequent, an implementation can favor responding
 * to an interrupt over normal method return. This is true even if it can be
 * shown that the interrupt occurred after another action may have unblocked
 * the thread. An implementation should document this behavior.
 *
 * @see ReentrantLock
 * @see Condition
 * @see ReadWriteLock
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Lock {

    /**
     * 获得锁。
     *
     * <p>如果该锁不可用，那么当前线程将出于线程调度的目的变为禁用，并处于休眠状态，直到获得锁为止。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>一个{@code Lock}实现可能会检测到锁的错误使用，
     * 例如导致死锁的调用，在这种情况下，可能会抛出(未选中的)异常。
     * {@code Lock}实现必须记录环境和异常类型。
     */
    void lock();

    /**
     * 获取锁，，可以响应线程中断除非当前线程是{@linkplain Thread #interrupt}。
     *
     * <p>如果锁可用，则获取锁并立即返回。
     *
     * <p>如果锁不可用，那么当前线程就会变成禁用线程调度的目的，并处于休眠状态，直到发生以下两种情况之一:
     *      <li>锁被当前线程获取;
     *      <li>其他线程{@linkplain Thread #interrupt interrupts}当前线程，支持中断锁获取。
     *
     * <p>如果当前线程:
     *      <li>在进入此方法时已设置其中断状态;
     *      <li>{@linkplain Thread#interrupt}在获取锁时中断，支持锁获取中断，然后抛出{@link InterruptedException}，并清除当前线程的InterruptedException状态。
     *
     * <p><b>实现注意事项</b>
     * <p>在某些实现中中断锁获取的能力可能是不可能的，如果可能的话，可能是一个昂贵的操作。
     *      程序员应该意识到情况可能是这样的。在这种情况下，实现应该记录。
     * <p>一个实现可能倾向于响应一个中断而不是正常的方法返回。
     *
     * @throws InterruptedException 如果当前线程在获取锁时被中断(支持中断获取锁)
     */
    void lockInterruptibly() throws InterruptedException;

    /**
     * 仅当锁在调用时处于空闲状态时才获取锁。
     *
     * <p>如果锁可用，则获取锁，并立即返回值为{@code true}的。
     *    如果锁不可用，那么这个方法将立即返回值{@code false}。
     *
     * 这种用法确保锁在被获取时被解锁，而在未被获取时不尝试解锁。
     *
     * @return {@code true} if the lock was acquired and
     *         {@code false} otherwise
     */
    boolean tryLock();

    /**
     * 如果在给定的等待时间内线程是空闲的，并且当前线程没有被{@linkplain Thread #interrupt}中断，则获取该锁。
     *
     * <p>如果锁可用，这个方法立即返回值为{@code true}的。
     *    如果锁不可用，那么为了线程调度的目的，当前线程将被禁用，并处于休眠状态，直到发生以下三种情况之一:
     *      <li>锁被当前线程获取
     *      <li>其他线程{@linkplain Thread #interrupt interrupts}当前线程当前线程，支持锁获取中断;
     *      <li>指定的等待时间已经过了
     *
     * <p>如果获取了锁，则返回值{@code true}。
     *
     * <p>如果当前线程:
     *      <li>在进入此方法时已设置其中断状态; or
     *      <li>是否{@linkplain Thread#interrupt}在获取*锁时中断，支持锁获取中断，
     *          然后抛出{@link InterruptedException}，并清除当前线程的InterruptedException状态。
     *
     * <p>如果指定的等待时间过期，则返回值{@code false}
     *    如果时间小于或等于0，则该方法根本不会等待。
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>在某些实现中中断锁获取的能力可能是不可能的，如果可能的话可能是一个昂贵的操作。
     *    程序员应该意识到情况可能是这样的。在这种情况下，实现应该记录。
     *
     * <p>实现可以更倾向于响应中断而不是正常的方法返回，或者报告超时。
     *
     * <p>一个{@code Lock}实现可能会检测到锁的错误使用，例如会导致死锁的调用，
     *      并可能在这种情况下抛出(未选中的)异常。
     *      环境和异常类型必须由{@code Lock}实现记录。
     *
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the {@code time} argument
     * @return {@code true} if the lock was acquired and {@code false}
     *         if the waiting time elapsed before the lock was acquired
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while acquiring the lock (and interruption of lock
     *         acquisition is supported)
     */
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    /**
     * 释放锁。
     *
     * <p><b>实现注意事项</b>
     *
     * <p>一个{@code Lock}实现通常会对哪个线程可以释放锁施加限制(通常只有锁的持有者可以释放锁)，
     *   如果违反该限制，可能会抛出(未选中)异常。
     *   任何限制和异常类型必须由{@code Lock}实现记录。
     */
    void unlock();

    /**
     * 返回一个新的{@link条件}实例，该实例绑定到这个{@code Lock}实例。
     *
     * <p>在等待条件之前，锁必须由当前线程持有。
     *    调用{@link Condition#await()}将自动释放锁在等待之前，并在等待返回之前重新获取锁。
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>{@link Condition}实例的确切操作依赖于{@code Lock}实现，并且必须由该实现记录。
     *
     * @return A new {@link Condition} instance for this {@code Lock} instance
     * @throws UnsupportedOperationException if this {@code Lock}
     *         implementation does not support conditions
     */
    Condition newCondition();
}
