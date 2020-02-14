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
import sun.misc.Unsafe;

/**
 * 一个可以自动更新的{@code boolean}值。
 * 参见{@link java.util.concurrent.atomic}包规范描述原子变量的属性。
 * {@code AtomicBoolean}用于原子更新标志等应用程序中，不能代替{@link java.lang.Boolean}。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicBoolean implements java.io.Serializable {
    private static final long serialVersionUID = 4654671469794556979L;
    // 设置为使用Unsafe.compareAndSwapInt进行更新
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicBoolean.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;

    /**
     * 使用给定的初始值创建一个新的{@code AtomicBoolean}。
     *
     * @param initialValue 初始值
     */
    public AtomicBoolean(boolean initialValue) {
        value = initialValue ? 1 : 0;
    }

    /**
     * 用初值{@code false}创建一个新的{@code AtomicBoolean}。
     */
    public AtomicBoolean() {
    }

    /**
     * 返回当前值
     *
     * @return the current value
     */
    public final boolean get() {
        return value != 0;
    }

    /**
     * 如果当前值{@code ==}是期望值，则自动将该值设置为给定的更新值。
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} 如果成功。False return表示实际值不等于期望值。
     */
    public final boolean compareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }

    /**
     * 如果当前值{@code ==}是期望值，则自动将该值设置为给定的更新值
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful
     */
    public boolean weakCompareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }

    /**
     * 无条件地设置为给定的值。
     *
     * @param newValue the new value
     */
    public final void set(boolean newValue) {
        value = newValue ? 1 : 0;
    }

    /**
     * 最终设置为给定的值
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final void lazySet(boolean newValue) {
        int v = newValue ? 1 : 0;
        unsafe.putOrderedInt(this, valueOffset, v);
    }

    /**
     * 原子地设置为给定的值并返回前一个值
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final boolean getAndSet(boolean newValue) {
        boolean prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, newValue));
        return prev;
    }

    /**
     * 返回当前值的字符串表示形式
     * @return the String representation of the current value
     */
    public String toString() {
        return Boolean.toString(get());
    }

}
