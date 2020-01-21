/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * List接口的可调整大小的数组实现。
 * 实现所有可选的列表操作，并允许所有元素，包括null。
 * 除了实现List 接口之外，此类还提供了一些方法来操纵内部用于存储列表的数组的大小。
 * 此类与Vector大致等效，但它是不同步的。
 *
 * size ，isEmpty ，get， set， iterator，listIterator 操作以固定的时间运行。
 * add 操作在摊销固定时间中运行，即添加n个元素需要O（n）时间。
 * 所有其他操作线性运行（大致而言）。与LinkedList实现的常量因子相比，的常量因子较低。
 *
 * 每个ArrayList实例都有一个容量。
 * 容量用于存储列表中元素的数组的大小。
 * 它始终至少等于列表大小。
 * 随着元素被添加到ArrayList中，其容量会自动增长。
 * 除了添加元素具有固定摊销的时间成本外，没有指定增长策略的细节。
 *
 * 在使用ensureCapacity 操作添加大量元素之前，应用程序可以增加ArrayList 实例的容量。
 * 这可以减少增量重新分配的数量。
 *
 * 请注意，此实现未同步。
 * 如果多个线程同时访问ArrayList实例，并且至少一个线程在结构上修改了该列表，则必须在外部进行同步。
 * 结构修改是添加或删除一个或多个元素的任何操作，或显式调整支持数组的大小；仅设置元素的值不是结构修改。
 * 通常通过在某些对象上同步来完成自然地封装了列表。
 *
 * 如果不存在这样的对象，则应该使用{{@link Collections＃synchronizedList Collections.synchronizedList} 方法来“包装”列表。
 * 最好在创建时执行此操作，以防止意外不同步访问列表：
 *     list= = Collections.synchronizedList（new ArrayList（...））;
 *
 * <p><a name="fail-fast">
 * 此类的{@link #iterator（）迭代器}和
 * {@link #listIterator（int）listIterator}方法返回的迭代器是 fail-fast：
 * （如果列表在结构上在创建迭代器之后的任何时候都可以通过修改器自身的
 * {{@link ListIterator＃remove（）remove}或
 * {@link ListIterator＃add（Object）add}方法进行修改后，
 * 迭代器将抛出一个 {@link ConcurrentModificationException}。
 * 因此，面对并发修改，迭代器会迅速而干净地失败，而不是在将来的不确定时间内冒着任意，不确定的行为的风险。
 *
 * 注意，不能保证迭代器的快速失败行为，因为通常来说，在存在不同步的并发修改的情况下，不可能做出任何严格的保证。
 * 快速失败的迭代器尽最大努力抛出{@code ConcurrentModificationException}。
 * 因此，编写依赖于此异常的程序的正确性是错误的：迭代器的快速失败行为仅应用于检测错误
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     List
 * @see     LinkedList
 * @see     Vector
 * @since   1.2
 */

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 默认初始容量
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 用于空实例的共享空数组实例
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 共享的空数组实例，用于默认大小的空实例。
     * 我们将其与EMPTY_ELEMENTDATA区别开来，以了解添加第一个元素时需要充气多少。
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 存储ArrayList元素的数组缓冲区。
     * ArrayList的容量是此数组缓冲区的长度。
     * 添加第一个元素时，任何具有elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA 的空ArrayList都将扩展为DEFAULT_CAPACITY。
     */
    transient Object[] elementData; // 非私有以简化嵌套类访问

    /**
     * ArrayList的大小（它包含的元素数）
     *
     * @serial
     */
    private int size;

    /**
     * 构造具有指定初始容量的空列表
     *
     * @param  initialCapacity  列表的初始容量
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }

    /**
     * 构造一个初始容量为10的空列表
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * 构造一个包含指定集合的元素的列表，其顺序由集合的迭代器返回
     *
     * @param c 将其元素放入此列表的集合
     * @throws NullPointerException 如果指定的集合为null
     */
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray可能（不正确）不返回Object []（请参见6260652）
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // 替换为空数组
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * 将该ArrayList 实例的容量调整为列表的当前大小。
     * 应用程序可以使用此操作来最大程度地减少ArrayList实例的存储。
     */
    public void trimToSize() {
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
              ? EMPTY_ELEMENTDATA
              : Arrays.copyOf(elementData, size);
        }
    }

    /**
     * 如有必要，增加此 ArrayList实例的容量，以确保它至少可以容纳最小容量参数指定的元素数量。
     *
     * @param   minCapacity   所需的最小容量
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            // 任何大小（如果不是默认元素表）
            ? 0
            // 大于默认值的默认空表。它已经应该是默认大小
            : DEFAULT_CAPACITY;

        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // 有溢出意识的代码
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * 要分配的最大数组大小。
     * 一些虚拟机在数组中保留一些标题字。
     * 尝试分配更大的阵列可能会导致OutOfMemoryError：请求的阵列大小超出VM限制
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 增加容量以确保它至少可以容纳最小容量参数指定的元素数
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // 有溢出意识的代码
        int oldCapacity = elementData.length;
        //根据原有数据的长度进行容量增大
        int newCapacity = oldCapacity + (oldCapacity >> 1);

        //判断增大后的长度与minCapacity比较
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;

        //判断增大后的长度与最大长度比较
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);

        // minCapacity通常接近大小，所以这是一个胜利:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    public static void main(String[] args) {
        int oldCapacity = 10;
        System.out.println(oldCapacity + (oldCapacity >> 1));
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * 返回此列表中的元素数
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * 如果此列表不包含任何元素，则返回true。
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 如果此列表包含指定的元素，则返回true 。
     * 更正式地讲，当且仅当此列表包含至少一个元素e 使得（o == null？e == null： o.equals（e）。
     *
     * @param o 要检查其在此列表中是否存在的元素
     * @return 如果此列表包含指定的元素返回true
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * 返回指定元素在此列表中首次出现的索引，如果此列表不包含该元素，则返回-1。
     * 更正式地，返回最低索引i 以便（o == null？get（i）== null：o.equals（get（i））），或-1（如果没有这样的索引）。
     */
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            //遍历elementData，判断是否存在该元素，如果相等，则返回下标i
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        //如果不存在，则返回-1
        return -1;
    }

    /**
     * 返回指定元素在此列表中最后一次出现的索引；如果此列表不包含该元素，则返回-1。
     * 更正式地，返回最高索引i 使得（o == null？get（i）== null：o.equals（get（i））），或-1（如果没有这样的索引）。
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            //反向遍历elementData，判断是否存在该元素，如果相等，则返回最高索引i
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        //如果不存在，则返回-1
        return -1;
    }

    /**
     * 返回此ArrayList 实例的浅表副本。 元素本身不会被复制。
     *
     * @return 此ArrayList实例的副本
     */
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是可克隆的
            throw new InternalError(e);
        }
    }

    /**
     * 以正确的顺序（从第一个到最后一个元素）返回包含此列表中所有元素的数组。
     *
     * 返回的数组将是“安全的”，因为此列表不维护对它的引用。
     * 换句话说，此方法必须分配一个新数组）。因此，调用者可以自由修改返回的数组。
     *
     * 此方法充当基于数组的API和基于集合的API之间的桥梁。
     *
     * @return 以适当顺序包含此列表中所有元素的数组
     */
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    /**
     * 以适当的顺序返回一个数组，该数组包含此列表中的所有元素（从第一个元素到最后一个元素）；
     * 返回的数组的运行时类型是指定数组的类型。
     * 如果列表适合指定的数组，则在其中返回。
     * 否则，将使用指定数组的运行时类型和此列表的大小分配新数组。
     *
     * 如果列表适合指定的数组并有剩余空间*即，数组中的元素多于列表），则紧接集合结束后的数组中的元素设置为null。
     * （如果调用者知道列表不包含任何空元素，这对于确定列表仅的长度很有用。）
     *
     * @param a 列表元素要存储到的数组（如果足够大）；否则，将为此分配一个具有相同运行时类型的新数组
     * @return 包含列表元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此列表中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            // 创建一个新的a的运行时类型数组，但是我的内容是
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    // 位置访问操作

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * 返回此列表中指定位置的元素
     *
     * @param  index 要返回的元素的索引
     * @return 此列表中指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        rangeCheck(index);

        return elementData(index);
    }

    /**
     * 用指定的元素替换此列表中指定位置的元素
     *
     * @param index 替换元素的索引
     * @param element 要存储在指定位置的元素
     * @return 先前位于指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        rangeCheck(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    /**
     * 将指定的元素追加到此列表的末尾
     *
     * @param e 要添加到此列表的元素
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        // 增加modCount
        ensureCapacityInternal(size + 1);
        elementData[size++] = e;
        return true;
    }

    /**
     * 将指定的元素插入此列表中的指定位置。
     * 将当前在该位置的元素（如果有的话）和任何后续元素向右移动（向其索引添加一个）。
     *
     * @param index 指定元素要插入的索引
     * @param element 要插入的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        //将index之后的元素向后移动一位
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * 删除此列表中指定位置的元素。 将所有后续元素向左移动（从它们的索引中减去一个）。
     *
     * @param index 要删除的元素的索引
     * @return 从列表中删除的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        // 明确让GC开展工作
        elementData[--size] = null;

        return oldValue;
    }

    /**
     * 从此列表中删除第一次出现的指定元素，如果存在，则将其删除。
     * 如果列表不包含元素，则不变。
     * 更正式地，删除具有最低索引 i 的元素，以使<tt>（o == null？get（i）== null：o.equals（get（i）））（如果存在这样的元素）。
     * 如果此列表包含指定的元素，则返回true （或者等效地，如果此列表由于调用而更改），则返回true。
     *
     * @param o 要从此列表中删除的元素（如果存在）
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /*
     * 专用remove方法，跳过边界检查并且不返回删除的值。
     */
    private void fastRemove(int index) {
        modCount++;
        //定位需要移动的位数
        int numMoved = size - index - 1;
        if (numMoved > 0)
            //将元素往前移动一位
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

    /**
     * 从此列表中删除所有元素。该调用返回后，列表将为空
     */
    public void clear() {
        modCount++;

        // 明确让GC开展工作
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }

    /**
     * 将指定集合中的所有元素追加到此列表的末尾，以指定集合的​​Iterator返回它们的顺序。
     * 如果在操作过程中修改了指定的集合，则该操作的行为是不确定的。
     * （这意味着如果指定的集合是此列表，并且此列表是非空的，则此调用的行为是未定义的。）
     *
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因通话而更改则返回true
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     * 从指定位置开始，将指定集合中的所有元素插入此列表。
     * 将当前位于该位置的元素（如果有）和任何后续元素右移（增加其索引）。
     * 新元素将按指定集合的​​迭代器返回的顺序显示在列表中。
     *
     * @param index 从指定的集合中插入第一个元素的索引
     * @param c 包含要添加到此列表的元素的集合
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        //对数组进行扩容
        ensureCapacityInternal(size + numNew);  // Increments modCount

        //确定需要移动的位数
        int numMoved = size - index;
        if (numMoved > 0)
            //将index之后的元素移动到（index + numNew）处
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);

        //将a中的元素移动到elementData中的index处
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     * 从此列表中删除索引在{@code fromIndex}（含）和{@code toIndex}（互斥）之间的所有元素。
     * 将任何后续元素向左移动（减少其索引）。
     * 此调用通过{@code（toIndex-fromIndex）}元素来缩短列表。 （如果{@code toIndex == fromIndex}，则此操作无效。）
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *         {@code toIndex} is out of range
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

        // clear to let GC do its work
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    /**
     * 检查给定的索引是否在范围内。如果不是，则抛出适当的运行时异常。
     * 此方法不检查索引是否为负数：始终在访问数组之前立即使用它，如果索引为负数，则抛出ArrayIndexOutOfBoundsException。
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * add和addAll使用的rangeCheck版本
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 构造一个IndexOutOfBoundsException详细消息。
     * 在错误处理代码的许多可能重构中，此“概述”在服务器和客户端VM上均表现最佳。
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
     * 从此列表中删除指定的集合中包含的所有元素
     *
     * @param c 包含要从此列表中删除的元素的集合
     * @return {@code true} 该列表是否因通话而改变
     * @throws ClassCastException 如果此列表的元素的类与指定的集合不兼容
     *  (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException 如果此列表包含null元素，并且指定的集合不允许使用null元素
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

    /**
     * 仅保留此列表中指定集合中包含的元素。
     * 换句话说，从列表中删除所有不包含在指定集合中的元素
     *
     * @param c 包含要保留在此列表中的元素的集合
     * @return {@code true} 该列表是否因通话而改变
     * @throws ClassCastException 如果此列表的元素的类与指定的集合不兼容
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null element and the
     *         specified collection does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>),
     *         or if the specified collection is null
     * @see Collection#contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // 保留与AbstractCollection的行为兼容性
            // 即使c.contains（）抛出异常。
            if (r != size) {
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            if (w != size) {
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }

    /**
     * 将ArrayList 实例的状态保存到流中（即对其进行序列化）
     *
     * @serialData 支持ArrayList 实例的数组的长度（int），然后以适当的顺序跟随其所有元素（每个Object）。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // 写出元素计数以及任何隐藏的内容
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // 写出大小作为与clone（）行为兼容的容量
        s.writeInt(size);

        // 按照正确的顺序写出所有元素
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * 从流中重构ArrayList实例（即反序列化）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // 阅读大小以及任何隐藏的内容
        s.defaultReadObject();

        // 读入容量
        s.readInt(); // ignored

        if (size > 0) {
            // 像clone（）一样，根据大小而不是容量分配数组
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // 按正确的顺序读入所有元素
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    /**
     * 从列表中的指定位置开始，以适当的顺序返回此列表中元素的列表迭代器。
     * 指定的索引表示首次调用{@link ListIterator＃next next}将返回的第一个元素。
     * 初次调用{@link ListIterator＃previous previous}将返回具有指定索引减一的元素。
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

    /**
     * 返回此列表中的元素的列表迭代器（按适当的顺序）。
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * 以适当的顺序返回此列表中元素的迭代器.
     *
     * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @return 以适当顺序在此列表中的元素上进行迭代的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * 优化版本的AbstractList.Itr
     */
    private class Itr implements Iterator<E> {
        int cursor;       // 下一个要返回的元素的索引
        int lastRet = -1; // 返回的最后一个元素的索引； -1（如果没有）
        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * AbstractList.ListItr的优化版本
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations.
     *
     * <p>This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for {@link #indexOf(Object)} and
     * {@link #lastIndexOf(Object)}, and all of the algorithms in the
     * {@link Collections} class can be applied to a subList.
     *
     * <p>The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                               parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }

        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                                               offset + this.size, this.modCount);
        }
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this list
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    /** Index-based split-by-two, lazily initialized Spliterator */
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        /*
         * If ArrayLists were immutable, or structurally immutable (no
         * adds, removes, etc), we could implement their spliterators
         * with Arrays.spliterator. Instead we detect as much
         * interference during traversal as practical without
         * sacrificing much performance. We rely primarily on
         * modCounts. These are not guaranteed to detect concurrency
         * violations, and are sometimes overly conservative about
         * within-thread interference, but detect enough problems to
         * be worthwhile in practice. To carry this out, we (1) lazily
         * initialize fence and expectedModCount until the latest
         * point that we need to commit to the state we are checking
         * against; thus improving precision.  (This doesn't apply to
         * SubLists, that create spliterators with current non-lazy
         * values).  (2) We perform only a single
         * ConcurrentModificationException check at the end of forEach
         * (the most performance-sensitive method). When using forEach
         * (as opposed to iterators), we can normally only detect
         * interference after actions, not before. Further
         * CME-triggering checks apply to all other possible
         * violations of assumptions for example null or too-small
         * elementData array given its size(), that could only have
         * occurred due to interference.  This allows the inner loop
         * of forEach to run without any further checks, and
         * simplifies lambda-resolution. While this does entail a
         * number of checks, note that in the common case of
         * list.stream().forEach(a), no checks or other computation
         * occur anywhere other than inside forEach itself.  The other
         * less-often-used methods cannot take advantage of most of
         * these streamlinings.
         */

        private final ArrayList<E> list;
        private int index; // current index, modified on advance/split
        private int fence; // -1 until used; then one past last index
        private int expectedModCount; // initialized when fence set

        /** Create new spliterator covering the given  range */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                new ArrayListSpliterator<E>(list, lo, index = mid,
                                            expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}
