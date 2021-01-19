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

package java.util.concurrent.blocking.queue;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * 一个{@link java.util.Queue}，它另外支持以下操作：
 *  在检索元素时等待队列变为非空，并在存储元素时等待队列中的空间变为可用。
 *
 * <p>{@code BlockingQueue}方法有四种形式，它们以不同的方式处理无法立即满足的操作，但将来可能会满足：
 *      第一个抛出异常，
 *      第二个返回一个特殊值（{@code null}或{@code false}，取决于操作），
 *      第三个无限期阻塞当前线程，直到操作成功为止，
 *      第四个在放弃之前，仅在给定的最大时间限制内阻止。
 *
 * 下表总结了这些方法：
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <caption>Summary of BlockingQueue methods</caption>
 *  <tr>
 *    <td></td>
 *    <td ALIGN=CENTER><em>Throws exception</em></td>
 *    <td ALIGN=CENTER><em>Special value</em></td>
 *    <td ALIGN=CENTER><em>Blocks</em></td>
 *    <td ALIGN=CENTER><em>Times out</em></td>
 *  </tr>
 *  <tr>
 *    <td><b>Insert</b></td>
 *    <td>{@link #add add(e)}</td>
 *    <td>{@link #offer offer(e)}</td>
 *    <td>{@link #put put(e)}</td>
 *    <td>{@link #offer(Object, long, TimeUnit) offer(e, time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>Remove</b></td>
 *    <td>{@link #remove remove()}</td>
 *    <td>{@link #poll poll()}</td>
 *    <td>{@link #take take()}</td>
 *    <td>{@link #poll(long, TimeUnit) poll(time, unit)}</td>
 *  </tr>
 *  <tr>
 *    <td><b>Examine</b></td>
 *    <td>{@link #element element()}</td>
 *    <td>{@link #peek peek()}</td>
 *    <td><em>not applicable</em></td>
 *    <td><em>not applicable</em></td>
 *  </tr>
 * </table>
 *
 * {@code BlockingQueue}不接受{@code null}元素。
 *      实现会在尝试中向{@code add}，{@code put}或{@code offer}提供{@code null}时抛出{@code NullPointerException}。
 *      {@code null}用作标记值，以指示{@code poll}操作失败
 *
 * {@code BlockingQueue}可能有容量限制。
 *      在任何给定的时间，它都可能具有{@code 剩余容量}，超过该数量就不能{@code put}添加其他元素而不会阻塞。
 *      没有任何内部容量约束的{@code BlockingQueue}总是报告{@code Integer.MAX_VALUE}的剩余容量。
 *
 * {@code BlockingQueue}实现旨在主要用于生产者-消费者队列，但另外支持{@link java.util.Collection}接口。
 *      因此，例如，可以使用{@code remove（x）}从队列中删除任意元素。
 *      但是，此类操作通常非常有效地执行，并且仅用于偶尔使用，例如在取消排队的消息时。
 *
 * <p>{@code BlockingQueue} implementations are thread-safe.  All
 * queuing methods achieve their effects atomically using internal
 * locks or other forms of concurrency control. However, the
 * <em>bulk</em> Collection operations {@code addAll},
 * {@code containsAll}, {@code retainAll} and {@code removeAll} are
 * <em>not</em> necessarily performed atomically unless specified
 * otherwise in an implementation. So it is possible, for example, for
 * {@code addAll(c)} to fail (throwing an exception) after adding
 * only some of the elements in {@code c}.
 *
 * <p>A {@code BlockingQueue} does <em>not</em> intrinsically support
 * any kind of &quot;close&quot; or &quot;shutdown&quot; operation to
 * indicate that no more items will be added.  The needs and usage of
 * such features tend to be implementation-dependent. For example, a
 * common tactic is for producers to insert special
 * <em>end-of-stream</em> or <em>poison</em> objects, that are
 * interpreted accordingly when taken by consumers.
 *
 * <p>
 * Usage example, based on a typical producer-consumer scenario.
 * Note that a {@code BlockingQueue} can safely be used with multiple
 * producers and multiple consumers.
 *  <pre> {@code
 * class Producer implements Runnable {
 *   private final BlockingQueue queue;
 *   Producer(BlockingQueue q) { queue = q; }
 *   public void run() {
 *     try {
 *       while (true) { queue.put(produce()); }
 *     } catch (InterruptedException ex) { ... handle ...}
 *   }
 *   Object produce() { ... }
 * }
 *
 * class Consumer implements Runnable {
 *   private final BlockingQueue queue;
 *   Consumer(BlockingQueue q) { queue = q; }
 *   public void run() {
 *     try {
 *       while (true) { consume(queue.take()); }
 *     } catch (InterruptedException ex) { ... handle ...}
 *   }
 *   void consume(Object x) { ... }
 * }
 *
 * class Setup {
 *   void main() {
 *     BlockingQueue q = new SomeQueueImplementation();
 *     Producer p = new Producer(q);
 *     Consumer c1 = new Consumer(q);
 *     Consumer c2 = new Consumer(q);
 *     new Thread(p).start();
 *     new Thread(c1).start();
 *     new Thread(c2).start();
 *   }
 * }}</pre>
 *
 * <p>Memory consistency effects: As with other concurrent
 * collections, actions in a thread prior to placing an object into a
 * {@code BlockingQueue}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions subsequent to the access or removal of that element from
 * the {@code BlockingQueue} in another thread.
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public interface BlockingQueue<E> extends Queue<E> {
    /**
     * 如果可以这样做，则立即将指定的元素插入此队列，而不会违反容量限制；
     *      如果成功，则返回{@code true}，如果当前没有可用空间，则抛出 {@code IllegalStateException}。
     *      使用容量受限的队列时，通常最好使用{@link #offer（Object）offer}。
     *
     * 与offer()相比：当队列满了，add()插入会报错，而offer()只会返回false
     *
     * @param e 要添加的元素
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws IllegalStateException 如果由于容量限制此时无法添加元素
     * @throws ClassCastException 如果指定元素的类阻止将其添加到此队列中
     * @throws NullPointerException 如果指定的元素为null
     * @throws IllegalArgumentException 如果指定的元素的某些属性阻止将其添加到此队列中
     */
    boolean add(E e);

    /**
     * 如果可以这样做，则立即将指定的元素插入此队列，而不会违反容量限制；
     *      如果成功，则返回{@code true}，
     *      如果当前没有可用空间，则返回{@code false}。
     * 当使用容量受限的队列时，此方法通常比{@link #add}更好，后者只能通过抛出异常来插入元素。
     *
     * @param e 要添加的元素
     * @return {@code true}（如果元素已添加到此队列），否则{@code false}
     * @throws ClassCastException 如果指定元素的类阻止将其添加到此队列中
     * @throws NullPointerException 如果指定的元素为null
     * @throws IllegalArgumentException 如果指定的元素的某些属性阻止将其添加到此队列中
     */
    boolean offer(E e);

    /**
     * 将指定的元素插入此队列，如有必要，请等待以使空间可用
     *
     * @param e 要添加的元素
     * @throws InterruptedException 如果在等待时被打断
     * @throws ClassCastException 如果指定元素的类阻止将其添加到此队列中
     * @throws NullPointerException 如果指定的元素为null
     * @throws IllegalArgumentException 如果指定的元素的某些属性阻止将其添加到此队列中
     */
    void put(E e) throws InterruptedException;

    /**
     * 将指定的元素插入此队列，如有必要，最多等待指定的等待时间以使空间可用。
     *
     * @param e 要添加的元素
     * @param timeout 放弃之前需要等待的时间，以{@code unit}为单位
     * @param unit {@code TimeUnit}确定如何解释{@code timeout}参数
     *
     * @return {@code true} if successful, or {@code false} if
     *         the specified waiting time elapses before space is available
     * @throws InterruptedException if interrupted while waiting
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 检索并除去此队列的头，如有必要，请等待直到元素可用
     *
     * @return 此队列的头
     * @throws InterruptedException if interrupted while waiting
     */
    E take() throws InterruptedException;

    /**
     * 检索并删除此队列的头，如果有必要使元素可用，则等待指定的等待时间。
     *
     * @param timeout 放弃之前需要等待的时间，以* {@code unit}为单位
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return the head of this queue, or {@code null} if the
     *         specified waiting time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    E poll(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 返回此队列可以理想地（在没有内存或资源限制的情况下）无阻塞接受的其他元素的数量；
     * 如果没有内部限制，则返回{@code Integer.MAX_VALUE}。
     *
     *
     * 请注意，您不能总是通过检查{@code remainingCapacity} 来判断尝试插入元素是否成功，因为可能是另一线程插入或删除元素的情况。
     *
     * @return the remaining capacity
     */
    int remainingCapacity();

    /**
     * 从此队列中删除指定元素的单个实例，如果存在，则将其删除。
     * 更正式地说，如果此队列包含一个或多个此类元素，则删除诸如{@code o.equals（e）}的元素{@code e}。
     * 如果此队列包含指定的元素，则返回{@code true}（或者等效地，如果此队列由于调用而更改）。
     *
     * @param o 要从此队列中删除的元素（如果存在）
     * @return {@code true} 该队列是否由于调用而改变
     * @throws ClassCastException 如果指定元素的类与此队列不兼容
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     */
    boolean remove(Object o);

    /**
     * 如果此队列包含指定的元素，则返回{@code true}。
     * 更正式地，当且仅当此队列包含至少一个元素{@code e}这样{@code o.equals（e）}时，才返回{@code true}。
     *
     * @param o 要检查是否包含在此队列中的对象
     * @return {@code true} 如果此队列包含指定的元素
     * @throws ClassCastException 如果指定元素的类与此队列不兼容
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null
     *         (<a href="../Collection.html#optional-restrictions">optional</a>)
     */
    public boolean contains(Object o);

    /**
     * 从此队列中删除所有可用的元素，并将它们添加到给定的集合中。
     * 此操作可能比重复轮询此队列更有效。
     * 尝试将元素添加到集合{@code c}时遇到失败，可能会在引发关联异常时导致元素不在两个集合中任何一个或两个集合中。
     * 尝试排空队列本身会导致{@code IllegalArgumentException}。
     * 此外，如果在操作进行过程中修改了指定的集合，则此操作的行为是不确定的。
     *
     * @param c 将元素转移到的集合
     * @return 传输的元素数
     * @throws UnsupportedOperationException 如果指定的集合不支持元素的添加
     * @throws ClassCastException if the class of an element of this queue
     *         prevents it from being added to the specified collection
     * @throws NullPointerException if the specified collection is null
     * @throws IllegalArgumentException if the specified collection is this
     *         queue, or some property of an element of this queue prevents
     *         it from being added to the specified collection
     */
    int drainTo(Collection<? super E> c);

    /**
     * 从此队列中最多移除给定数量的可用元素，并将它们添加到给定的集合中。
     * 尝试将元素添加到集合{@code c}时遇到失败，可能会在引发关联异常时导致元素不在两个集合中任何一个或两个集合中。
     * 尝试排空队列本身会导致* {@code IllegalArgumentException}。
     * 此外，如果在操作进行过程中修改了指定的集合，则此操作的行为是不确定的。
     *
     * @param c 将元素转移到的集合
     * @param maxElements 转移的最大元素数
     * @return 传输的元素数
     * @throws UnsupportedOperationException 如果指定的集合不支持元素的添加
     * @throws ClassCastException if the class of an element of this queue
     *         prevents it from being added to the specified collection
     * @throws NullPointerException if the specified collection is null
     * @throws IllegalArgumentException if the specified collection is this
     *         queue, or some property of an element of this queue prevents
     *         it from being added to the specified collection
     */
    int drainTo(Collection<? super E> c, int maxElements);
}
