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

import java.util.concurrent.TimeUnit;


/**
 * 一个{@link java.util.concurrent.blocking.queue.BlockingQueue} 在这种情况下，生产者可以等待消费者接收元素。
 * {@code TransferQueue}可能在消息传递应用程序中很有用，在这些应用程序中，生产者有时(使用方法{@link #transfer})等待调用{@code take}或{@code poll}的消费者接收元素，
 * 而在其他时候(通过方法{@code put})不等待接收元素进入队列。
 * {@code tryTransfer}的{@linkplain #tryTransfer(Object) Non-blocking}
 * 和{@linkplain #tryTransfer(Object,long, TimeUnit) time-out}版本也可用。
 * {@code TransferQueue}也可以通过{@link #hasWaitingConsumer}来查询是否有线程在等待项，这与{@code peek}操作相反。
 *
 *
 * <p>像其他阻塞队列一样，{@code TransferQueue}可能是容量有限的。
 * 如果是这样，尝试的传输操作可能首先阻塞等待可用空间，然后阻塞等待消费者接收。
 * 注意，在一个容量为零的队列中，例如{@link SynchronousQueue}， {@code put}和{@code transfer}实际上是同义的。
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.7
 * @author Doug Lea
 * @param <E> 此集合中保存的元素的类型
 */
public interface TransferQueue<E> extends BlockingQueue<E> {
    /**
     * 如果可能的话，将元素立即转移给等待的消费者。
     *
     * <p>更准确地说，如果存在一个消费者正在等待接收指定的元素，
     * 则立即传输指定的元素(在{@link #take}或timed {@link #poll(long,TimeUnit) poll}中)，
     * 否则返回{@code false}而不将元素排队。
     *
     * @param e 要转移的元素
     * @return {@code true} 如果元素被转移，否则{@code false}
     * @throws ClassCastException 如果指定元素的类阻止将其添加到此队列中
     * @throws NullPointerException 如果指定的元素为空
     * @throws IllegalArgumentException 如果指定元素的某些属性阻止它被添加到此队列
     */
    boolean tryTransfer(E e);

    /**
     * Transfers the element to a consumer, waiting if necessary to do so.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else waits until the element is received by a consumer.
     *
     * @param e the element to transfer
     * @throws InterruptedException if interrupted while waiting,
     *         in which case the element is not left enqueued
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    void transfer(E e) throws InterruptedException;

    /**
     * Transfers the element to a consumer if it is possible to do so
     * before the timeout elapses.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else waits until the element is received by a consumer,
     * returning {@code false} if the specified wait time elapses
     * before the element can be transferred.
     *
     * @param e the element to transfer
     * @param timeout how long to wait before giving up, in units of
     *        {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return {@code true} if successful, or {@code false} if
     *         the specified waiting time elapses before completion,
     *         in which case the element is not left enqueued
     * @throws InterruptedException if interrupted while waiting,
     *         in which case the element is not left enqueued
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    boolean tryTransfer(E e, long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * Returns {@code true} if there is at least one consumer waiting
     * to receive an element via {@link #take} or
     * timed {@link #poll(long,TimeUnit) poll}.
     * The return value represents a momentary state of affairs.
     *
     * @return {@code true} if there is at least one waiting consumer
     */
    boolean hasWaitingConsumer();

    /**
     * Returns an estimate of the number of consumers waiting to
     * receive elements via {@link #take} or timed
     * {@link #poll(long,TimeUnit) poll}.  The return value is an
     * approximation of a momentary state of affairs, that may be
     * inaccurate if consumers have completed or given up waiting.
     * The value may be useful for monitoring and heuristics, but
     * not for synchronization control.  Implementations of this
     * method are likely to be noticeably slower than those for
     * {@link #hasWaitingConsumer}.
     *
     * @return the number of consumers waiting to receive elements
     */
    int getWaitingConsumerCount();
}
