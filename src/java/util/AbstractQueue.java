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

/**
 * 这个类提供了一些{@link队列}操作的框架实现。
 * 当基本实现不允许null元素时，这个类中的实现是合适的。
 * 方法{@link #add add}、{@link #remove remove}和{@link #element}
 * 分别基于{@link #offer offer}、{@link  #poll poll}和{@link #peek peek}，
 * 但抛出异常而不是通过false或null return。
 *
 * <p>扩展该类的队列实现必须最低限度地定义一个方法{@link Queue#offer}，
 * 该方法不允许插入null元素，以及方法{@link Queue#peek}、{@link Queue#poll}、
 * {@link Collection#size}和{@link Collection#iterator}。
 * 通常，还会覆盖其他方法。
 * 如果不能满足这些需求，可以考虑用代替子类化{@link AbstractCollection}。
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this collection
 */
public abstract class AbstractQueue<E>
    extends AbstractCollection<E>
    implements Queue<E> {

    /**
     * 子类使用的构造函数
     */
    protected AbstractQueue() {
    }

    /**
     * 如果可能的话，立即将指定的元素插入此队列而不违反容量限制；
     * 如果成功，则返回true；
     * 如果当前没有空间，则抛出IllegalStateException可用。
     *
     * 如果 offer成功，则此实现返回true，否则抛出IllegalStateException。
     *
     * @param e the element to add
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws IllegalStateException if the element cannot be added at this
     *         time due to capacity restrictions
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null and
     *         this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this queue
     */
    public boolean add(E e) {
        if (offer(e))
            return true;
        else
            throw new IllegalStateException("Queue full");
    }

    /**
     * 检索并删除此队列的头。
     * 这个方法与{@link #poll poll}的不同之处在于，它只在队列为空时抛出异常。
     *
     * <p>除非队列为空，否则此实现将返回poll的结果。
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    public E remove() {
        E x = poll();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    /**
     * 检索但不删除此队列的头。
     * 此方法与{@link #peek peek}唯一的不同之处在于，它在该队列为空时抛出异常。
     *
     * <p>除非队列为空，否则此实现将返回peek的结果。
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    public E element() {
        E x = peek();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    /**
     * 从该队列中删除所有元素。
     * 该调用返回后，队列将为空。
     *
     * <p>这个实现反复调用{@link #poll poll}，直到它返回null。
     */
    public void clear() {
        while (poll() != null)
            ;
    }

    /**
     * 将指定集合中的所有元素添加到此队列。
     * 试图将所有队列添加到自身会导致IllegalArgumentException。
     * 此外，如果在操作进行期间修改了指定的集合，则此操作的行为是未定义的。
     *
     * <p>这个实现遍历指定的集合，并将迭代器返回的每个元素依次添加到这个队列中。
     * 当试图添加一个元素(特别是包含一个null元素)时遇到的运行时异常可能只会导致在抛出关联的异常时成功添加了一些元素。
     *
     * @param c collection containing elements to be added to this queue
     * @return <tt>true</tt> if this queue changed as a result of the call
     * @throws ClassCastException if the class of an element of the specified
     *         collection prevents it from being added to this queue
     * @throws NullPointerException if the specified collection contains a
     *         null element and this queue does not permit null elements,
     *         or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     *         specified collection prevents it from being added to this
     *         queue, or if the specified collection is this queue
     * @throws IllegalStateException if not all the elements can be added at
     *         this time due to insertion restrictions
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

}
