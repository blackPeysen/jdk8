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
 * 与使用{@code synchronized}方法和语句所获得的锁定相比，{@code Lock}实现提供了更广泛的锁定操作。
 * 它们允许更灵活的结构，可能具有完全不同的属性，并可能支持多个关联的{@link Condition}对象。
 *
 * <p>锁是用于控制多个线程对共享资源的访问的工具。
 * 通常，锁提供对共享资源的独占访问：一次只能有一个线程可以获取该锁，并且对共享资源的所有访问都需要首先获取该锁。
 * 但是，某些锁可能允许并发访问共享资源，例如{@link ReadWriteLock}的读取锁。
 *
 * <p>使用{@code Synchronized}方法或语句可以访问与每个对象关联的隐式监视器锁，但是强制所有锁的获取和释放以块结构的方式发生：
 * 当获取多个锁时，它们必须是以相反的顺序释放，并且所有锁必须在获取它们的同一词法范围内释放。
 *
 * <p>尽管{@code sync}方法和语句的作用域机制使使用监视器锁的编程变得容易得多，并且避免了很多常见的涉及锁的编程错误，但是有时您需要使用更多的锁来进行操作灵活的方式。
 * 例如，用于遍历并发访问的数据结构的某些算法需要使用“交接”或“链锁”：您取得节点A的锁，然后取得节点B的锁，然后释放A并取得C ，然后释放B并获取D，依此类推。
 * {@code Lock}接口的实现通过允许在不同范围内获取和释放锁，并允许以任意顺序获取和释放多个锁，从而启用了此类技术。
 *
 * <p>灵活性的提高带来了额外的责任。缺少块结构锁定将消除{{code sync}方法和语句中发生的锁定的自动释放。
 * 在大多数情况下，应使用以下习惯用语：
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
 * 当锁定和解锁发生在不同的范围内时，必须小心以确保通过try-finally或try-catch保护在保持锁定的同时执行的所有代码，以确保在必要时释放锁定。
 *
 * <p>{@code Lock}实现通过使用提供非阻塞性尝试来获取锁（{@link #tryLock（）}），从而提供了附加功能通过使用{@code sync}方法和语句，提供了附加功能。
 * 可以被中断的锁（{@link #lockInterruptibly}，并尝试获取可能超时的锁（{@link #tryLock（long，TimeUnit）}）。
 *
 * <p>{@code Lock}类还可以提供与隐式监视器锁完全不同的行为和语义，例如保证顺序，不可重用或死锁检测。
 * 如果实现提供了这种特殊的语义，那么实现必须记录这些语义。
 *
 * <p>请注意，{@code Lock}实例只是普通对象，它们本身可以在{@code sync}语句中用作目标。
 * 获取{@code Lock}实例的监视器锁定与该实例的任何{@link #lock}方法都没有指定的关系。
 * 为避免混淆，建议您不要以这种方式使用{@code Lock}实例，除非在它们自己的实现中使用。
 *
 * <p>除非另有说明，否则为任何参数传递{@code null}值将导致抛出{@link NullPointerException}。
 *
 * <h3>Memory Synchronization</h3>
 *
 * <p>所有{@code Lock}实现都必须执行与内置监视器锁所提供的相同的内存同步语义，
 * 如<a href =“ https://docs.oracle中所述。 com / javase / specs / jls / se7 / html / jls-17.html＃jls-17.4“> * Java语言规范（17.4内存模型）</a>：
 * <ul>
 * <li>A successful {@code lock} operation has the same memory
 * synchronization effects as a successful <em>Lock</em> action.
 * <li>A successful {@code unlock} operation has the same
 * memory synchronization effects as a successful <em>Unlock</em> action.
 * </ul>
 *
 * 不成功的锁定和解锁操作以及可重入的*锁定/解锁操作，不需要任何内存同步效果。
 *
 * <h3>实施注意事项</h3>
 *
 * <p>锁获取的三种形式（可中断，不中断和定时）可能在性能，特性，订购保证或其他实现质量上有所不同。
 * 此外，在给定的{@code Lock} 类中，中断inginging获取锁的功能可能不可用。
 * 因此，不需要实现为所有三种形式的锁获取定义完全相同的保证或语义，也不需要支持中断正在进行的锁获取。
 * 需要一个实现来清楚地记录每个锁定方法提供的语义和保证。
 * 在支持锁获取中断的程度上，它还必须遵守此接口中定义的中断语义：或者完全或仅在方法输入时才这样做。
 *
 * <p>由于中断通常意味着取消，并且很少进行中断检查，因此与正常方法返回相比，实现可能更喜欢对中断做出响应。
 * 即使可以表明在另一个动作之后发生的中断可能已经取消阻塞了线程，也是如此。实现应记录此行为。
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
     * 获取锁，可以响应线程中断除非当前线程是{@linkplain Thread #interrupt}。
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
     * @return {@code true}（如果已获得锁），{@code false}（否则）
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
     * @param time 等待锁的最长时间
     * @param unit {@code time}参数的时间单位
     * @return 如果获取了锁，则为{@code true}，如果获取了锁，则为{@code false} ，如果经过了等待时间
     *
     * @throws InterruptedException 如果当前线程在获取锁时被中断（并且锁被中断支持获取）
     */
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    /**
     * 释放锁。
     *
     * <p>实现注意事项
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
