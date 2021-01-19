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

/**
 * {@code AtomicStampedReference}维护一个对象引用和一个可以自动更新的整数“stamp”。
 *
 * <p>实现注意:这个实现通过创建表示“装箱的”[reference, integer]对的内部对象来维护带戳记的引用。
 *      解决了AtomicReference可能出现的ABA问题
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> 此引用引用的对象类型
 */
public class AtomicStampedReference<V> {

    private static class Pair<T> {
        final T reference;  // 对象引用
        final int stamp;    // 时间戳
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    private volatile Pair<V> pair;

    /**
     * 使用给定的初始值创建一个新的{@code AtomicStampedReference}。
     *
     * @param initialRef 最初的引用
     * @param initialStamp 最初的时间戳
     */
    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }

    /**
     * 返回引用的当前值。
     *
     * @return the current value of the reference
     */
    public V getReference() {
        return pair.reference;
    }

    /**
     * 返回戳记的当前值
     *
     * @return the current value of the stamp
     */
    public int getStamp() {
        return pair.stamp;
    }

    /**
     * 返回引用和戳记的当前值。
     * 典型用法是{@code int[1] holder;ref = v.get(持有人);}。
     *
     * @param stampHolder 至少一个大小的数组。在返回时，{@code stampholder[0]}将保存该戳记的值。
     * @return the current value of the reference
     */
    public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }

    /**
     * 如果当前引用是{@code ==}到预期引用，并且当前戳等于预期戳，则自动将引用和stamp的值设置为给定的更新值。
     *
     * <p><a href="package-summary.html#weakCompareAndSet">May fail
     * spuriously and does not provide ordering guarantees</a>, so is
     * only rarely an appropriate alternative to {@code compareAndSet}.
     *
     * @param expectedReference the expected value of the reference
     * @param newReference the new value for the reference
     * @param expectedStamp the expected value of the stamp
     * @param newStamp the new value for the stamp
     * @return {@code true} if successful
     */
    public boolean weakCompareAndSet(V   expectedReference,
                                     V   newReference,
                                     int expectedStamp,
                                     int newStamp) {
        return compareAndSet(expectedReference, newReference,
                             expectedStamp, newStamp);
    }

    /**
     * 如果当前引用是{@code ==}到预期引用，并且当前戳等于预期戳，则自动将引用和stamp的值设置为给定的更新值。
     *
     * @param expectedReference the expected value of the reference
     * @param newReference the new value for the reference
     * @param expectedStamp the expected value of the stamp
     * @param newStamp the new value for the stamp
     * @return {@code true} if successful
     */
    public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedStamp == current.stamp &&
            ((newReference == current.reference &&
              newStamp == current.stamp) ||
             casPair(current, Pair.of(newReference, newStamp)));
    }

    /**
     * 无条件地设置引用和戳记的值
     *
     * @param newReference the new value for the reference
     * @param newStamp the new value for the stamp
     */
    public void set(V newReference, int newStamp) {
        Pair<V> current = pair;
        if (newReference != current.reference || newStamp != current.stamp)
            this.pair = Pair.of(newReference, newStamp);
    }

    /**
     * 如果当前引用是{@code ==}，则自动将戳记的值设置为给定的更新值，
     * 如果当前引用是{@code ==}，则设置为预期的引用。
     * 该操作的任何给定调用都可能会错误地失败(返回{@code false})，
     * 但是当当前值持有期望值并且没有其他线程也试图设置该值时，重复调用将最终成功。
     *
     * @param expectedReference the expected value of the reference
     * @param newStamp the new value for the stamp
     * @return {@code true} if successful
     */
    public boolean attemptStamp(V expectedReference, int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            (newStamp == current.stamp ||
             casPair(current, Pair.of(expectedReference, newStamp)));
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE = sun.misc.Unsafe.getUnsafe();
    private static final long pairOffset =
        objectFieldOffset(UNSAFE, "pair", AtomicStampedReference.class);

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
    }

    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // Convert Exception to corresponding Error
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }
}
