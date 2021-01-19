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

package java.util;

import java.util.concurrent.blocking.queue.ArrayBlockingQueue;
import java.util.concurrent.blocking.queue.BlockingQueue;
import java.util.concurrent.blocking.queue.LinkedBlockingQueue;
import java.util.concurrent.blocking.queue.PriorityBlockingQueue;

/**
 * 用于在处理之前保存元素的集合。
 * 除了basic {@link java.util.Collection}操作，队列提供额外的插入、提取和检查操作。
 * 这些方法都以两种形式存在:
 *      一种方法在操作失败时抛出异常，
 *      另一种方法返回特殊的值({@code null}或{@code false}，取决于操作)。
 * 后一种插入操作形式是专门为容量受限的{@code Queue}实现而设计的;在大多数实现中，插入操作不能失败。
 *
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>Summary of Queue methods</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>Throws exception</em></td>
 *    <td ALIGN=CENTER><em>Returns special value</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>Insert</b></td>
 *    <td>{@link Queue#add add(e)}</td>
 *    <td>{@link Queue#offer offer(e)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>Remove</b></td>
 *    <td>{@link Queue#remove remove()}</td>
 *    <td>{@link Queue#poll poll()}</td>
 *  </tr>
 *  <tr>
 *    <td><b>Examine</b></td>
 *    <td>{@link Queue#element element()}</td>
 *    <td>{@link Queue#peek peek()}</td>
 *  </tr>
 * </table>
 *
 * <p>队列通常(但不一定)以FIFO(先进先出)方式对元素排序。
 * 例外情况包括:
 *      优先级队列，它根据提供的comparator或元素的自然顺序对元素排序，
 *      LIFO队列(或堆栈)，它对元素LIFO(后进先出)排序。
 * 无论使用什么顺序，队列的head是元素，调用{@link #remove()}或{@link #poll()}可以删除该元素。
 * 在FIFO队列中，所有新元素都插入到队列的尾处。其他类型的队列可能使用不同的放置规则。
 * 每个{@code队列}实现必须指定其排序属性。
 *
 * <p>offer()方法在可能的情况下插入一个元素，否则返回{@code false}。
 * 这与{@link java.util.Collection#add Collection.add}方法不同，该方法只能通过抛出未检查的异常来失败添加元素。
 * {@code offer}方法被设计为在正常情况下使用，而不是在异常情况下使用，例如在固定容量(或“有界”)队列中。
 *
 * <p>{@link #remove()}和{@link #poll()}方法删除和返回队列的头。
 * 从队列中删除的元素是队列的排序策略的一个函数，它与poll()实现不同。
 * 当队列为空时，{@code remove()}和{@code poll()}方法的行为不同:
 *      {@code remove()}方法抛出一个异常，而{@code poll()}方法返回{@code null}。
 *
 * <p>{@link #element()}和{@link #peek()}方法返回队列的头部，但不移除该元素
 *
 * <p>{@code Queue}接口没有定义在并发编程中常见的阻塞队列方法。
 * 在{@link BlockingQueue}中定义了这些方法，它们等待元素出现或空间可用。
 * BlockingQueue}接口，它扩展此接口。
 *
 *
 * <p>{@code Queue} implementations generally do not allow insertion
 * of {@code null} elements, although some implementations, such as
 * {@link LinkedList}, do not prohibit insertion of {@code null}.
 * Even in the implementations that permit it, {@code null} should
 * not be inserted into a {@code Queue}, as {@code null} is also
 * used as a special return value by the {@code poll} method to
 * indicate that the queue contains no elements.
 *
 * <p>{@code Queue} implementations generally do not define
 * element-based versions of methods {@code equals} and
 * {@code hashCode} but instead inherit the identity based versions
 * from class {@code Object}, because element-based equality is not
 * always well-defined for queues with the same elements but different
 * ordering properties.
 *
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @see java.util.Collection
 * @see LinkedList
 * @see PriorityQueue
 * @see LinkedBlockingQueue
 * @see BlockingQueue
 * @see ArrayBlockingQueue
 * @see LinkedBlockingQueue
 * @see PriorityBlockingQueue
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public interface Queue<E> extends Collection<E> {
    /**
     * 如果可以在不违反容量限制的情况下立即将指定的元素插入到此队列中，
     *      如果成功则返回{@code true}，
     *      如果当前没有可用空间则抛出{@code IllegalStateException}。
     *
     * @param e 要添加的元素
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws IllegalStateException if the element cannot be added at this
     *         time due to capacity restrictions
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null and
     *         this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this queue
     */
    boolean add(E e);

    /**
     * 如果可能的话，立即将指定的元素插入此队列，而不会违反容量限制。
     * 当使用容量受限的队列时，此方法通常比{@link #add}更可取，后者可能会因抛出异常而无法仅插入元素。
     *
     * @param e the element to add
     * @return {@code true} if the element was added to this queue, else
     *         {@code false}
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null and
     *         this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this queue
     */
    boolean offer(E e);

    /**
     * 检索并删除此队列的头。
     * 这个方法与{@link #poll poll}的不同之处在于，它只在队列为空时抛出异常。
     *
     * @return 这队的头˙
     * @throws NoSuchElementException if this queue is empty
     */
    E remove();

    /**
     * 检索并删除此队列的头，如果此队列为空，则返回{@code null}。
     *
     * @return 此队列的头，如果此队列为空，则为{@code null}
     */
    E poll();

    /**
     * 检索但不删除此队列的头。
     * 此方法与{@link #peek peek}唯一的区别在于，如果此队列为空，它将抛出一个异常。
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    E element();

    /**
     * 检索但不删除此队列的头，或在此队列为空时返回{@code null}。
     *
     * @return 此队列的头，如果此队列为空，则为{@code null}
     */
    E peek();
}
