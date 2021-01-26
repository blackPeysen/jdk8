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

package java.util.concurrent.atomic;
import java.util.function.IntUnaryOperator;
import java.util.function.IntBinaryOperator;
import sun.misc.Unsafe;

/**
 * 原子性更新int数组
 * 一个{@code int}数组，其中的元素可以自动更新。
 * 请参阅{@link java.util.concurrent。描述原子变量属性的包规范。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicIntegerArray implements java.io.Serializable {
    private static final long serialVersionUID = 2862133569453604235L;

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final int base = unsafe.arrayBaseOffset(int[].class);
    private static final int shift;
    private final int[] array;

    static {
        int scale = unsafe.arrayIndexScale(int[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);

        return byteOffset(i);
    }

    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }

    /**
     * 创建一个给定长度的新AtomicIntegerArray，所有元素最初为零。
     *
     * @param length the length of the array
     */
    public AtomicIntegerArray(int length) {
        array = new int[length];
    }

    /**
     * 创建一个新AtomicIntegerArray，其长度与给定数组相同，并且从该数组复制所有元素。
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicIntegerArray(int[] array) {
        // Visibility guaranteed by final field guarantees
        this.array = array.clone();
    }

    /**
     * 返回数组的长度。
     *
     * @return the length of the array
     */
    public final int length() {
        return array.length;
    }

    /**
     * 获取位置{@code i}的当前值
     *
     * @param i the index
     * @return the current value
     */
    public final int get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private int getRaw(long offset) {
        return unsafe.getIntVolatile(array, offset);
    }

    /**
     * 将位置{@code i}的元素设置为给定值
     *
     * @param i the index
     * @param newValue the new value
     */
    public final void set(int i, int newValue) {
        unsafe.putIntVolatile(array, checkedByteOffset(i), newValue);
    }

    /**
     * 最终将位置{@code i}的元素设置为给定值
     *
     * @param i the index
     * @param newValue the new value
     * @since 1.6
     */
    public final void lazySet(int i, int newValue) {
        unsafe.putOrderedInt(array, checkedByteOffset(i), newValue);
    }

    /**
     * 以原子方式将位置{@code i}的元素设置为给定值并返回旧值.
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    public final int getAndSet(int i, int newValue) {
        return unsafe.getAndSetInt(array, checkedByteOffset(i), newValue);
    }

    /**
     * 如果当前值{@code ==}是期望值，则以原子方式将位置{@code i}的元素设置为给定的更新值。
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return {@code true}如果成功。错误的返回值表示实际值不等于期望值。
     */
    public final boolean compareAndSet(int i, int expect, int update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, int expect, int update) {
        return unsafe.compareAndSwapInt(array, offset, expect, update);
    }

    /**
     * Atomically sets the element at position {@code i} to the given
     * updated value if the current value {@code ==} the expected value.
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     */
    public final boolean weakCompareAndSet(int i, int expect, int update) {
        return compareAndSet(i, expect, update);
    }

    /**
     * 以原子方式将索引{@code i}处的元素加1。
     *
     * @param i the index
     * @return the previous value
     */
    public final int getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    /**
     * 以原子方式将索引{@code i}处的元素减1。
     *
     * @param i the index
     * @return the previous value
     */
    public final int getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    /**
     * 以原子方式将给定值添加到索引为{@code i}的元素。
     *
     * @param i the index
     * @param delta the value to add
     * @return the previous value
     */
    public final int getAndAdd(int i, int delta) {
        return unsafe.getAndAddInt(array, checkedByteOffset(i), delta);
    }

    /**
     * 以原子方式将索引{@code i}处的元素加1。
     *
     * @param i the index
     * @return the updated value
     */
    public final int incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    /**
     * 以原子方式将索引{@code i}处的元素减1。
     *
     * @param i the index
     * @return the updated value
     */
    public final int decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    /**
     * 以原子方式将给定值添加到索引为{@code i}的元素。
     *
     * @param i the index
     * @param delta the value to add
     * @return the updated value
     */
    public final int addAndGet(int i, int delta) {
        return getAndAdd(i, delta) + delta;
    }


    /**
     * 使用应用给定函数的结果以原子方式更新索引{@code i}处的元素，并返回先前的值。
     * 函数应无副作用，因为当尝试更新由于线程间争用而失败时，函数可能会重新应用。
     *
     * @param i the index
     * @param updateFunction 无副作用的功能
     * @return the previous value
     * @since 1.8
     */
    public final int getAndUpdate(int i, IntUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    /**
     * 使用应用给定函数的结果以原子方式更新索引{@code i}处的元素，并返回更新后的值。
     * 函数应无副作用，因为当尝试更新由于线程间争用而失败时，函数可能会重新应用。
     *
     * @param i 索引
     * @param updateFunction 无副作用的功能
     * @return 更新值
     * @since 1.8
     */
    public final int updateAndGet(int i, IntUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    /**
     * 用*将给定函数应用于当前值和给定值，并返回先前值的结果*以原子方式更新索引{@code i}处的元素。
     * 该功能应无副作用，因为当尝试更新由于线程间争用而失败时，它可能会重新应用。
     * 应用带有索引{@code i}的当前值作为其第一个*参数，并使用给定的update作为第二个参数。
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction 两个参数的无副作用功能
     * @return the previous value
     * @since 1.8
     */
    public final int getAndAccumulate(int i, int x,
                                      IntBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    /**
     * 使用将给定函数应用于当前值和*给定值的结果，以原子方式更新索引{@code i}处的元素，并返回更新后的值。
     * 该功能应无副作用，因为当尝试更新由于线程间争用而失败时，它可能会重新应用。
     * 应用带有索引{@code i}的当前值作为其第一个*参数，并使用给定的update作为第二个参数。
     *
     * @param i the index
     * @param x the update value
     * @param accumulatorFunction 两个参数的无副作用功能
     * @return the updated value
     * @since 1.8
     */
    public final int accumulateAndGet(int i, int x,
                                      IntBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    /**
     * 返回数组当前值的字符串表示形式。
     * @return the String representation of the current values of array
     */
    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }

}
