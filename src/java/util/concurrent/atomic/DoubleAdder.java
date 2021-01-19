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
import java.io.Serializable;

/**
 * 一个或多个变量一起维持初始的{@code double}和。
 * 当更新(方法{@link #add})在线程间被争用时，变量集可以动态增长以减少争用。
 * 方法{@link #sum}(或者等效的{@link #doubleValue})返回维护该sum的变量的当前总和。
 * 不能保证线程内或线程间的累积顺序。
 * 因此，如果需要数值稳定性，这类可能不适用，特别是当组合的值有很大的不同数量级时。
 *
 * <p>当多个线程更新用于某些目的(如经常更新但读取次数较少的摘要统计信息)的通用值时，这个类通常比其他选项更可取。
 *
 * <p>这个类扩展了{@link Number}，
 * 而不是定义了{@code equals}、{@code hashCode}和{@code compareTo}这样的方法，
 * 因为实例需要被改变，所以作为集合键没有用处。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class DoubleAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /*
     * Note that we must use "long" for underlying representations,
     * because there is no compareAndSet for double, due to the fact
     * that the bitwise equals used in any CAS implementation is not
     * the same as double-precision equals.  However, we use CAS only
     * to detect and alleviate contention, for which bitwise equals
     * works best anyway. In principle, the long/double conversions
     * used here should be essentially free on most platforms since
     * they just re-interpret bits.
     */

    /**
     * 创建一个初始和为零的新加法器。
     */
    public DoubleAdder() {
    }

    /**
     * Adds the given value.
     *
     * @param x the value to add
     */
    public void add(double x) {
        Cell[] as; long b, v; int m; Cell a;
        if ((as = cells) != null ||
            !casBase(b = base,
                     Double.doubleToRawLongBits
                     (Double.longBitsToDouble(b) + x))) {
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[getProbe() & m]) == null ||
                !(uncontended = a.cas(v = a.value,
                                      Double.doubleToRawLongBits
                                      (Double.longBitsToDouble(v) + x))))
                doubleAccumulate(x, null, uncontended);
        }
    }

    /**
     * Returns the current sum.  The returned value is <em>NOT</em> an
     * atomic snapshot; invocation in the absence of concurrent
     * updates returns an accurate result, but concurrent updates that
     * occur while the sum is being calculated might not be
     * incorporated.  Also, because floating-point arithmetic is not
     * strictly associative, the returned result need not be identical
     * to the value that would be obtained in a sequential series of
     * updates to a single variable.
     *
     * @return the sum
     */
    public double sum() {
        Cell[] as = cells; Cell a;
        double sum = Double.longBitsToDouble(base);
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += Double.longBitsToDouble(a.value);
            }
        }
        return sum;
    }

    /**
     * Resets variables maintaining the sum to zero.  This method may
     * be a useful alternative to creating a new adder, but is only
     * effective if there are no concurrent updates.  Because this
     * method is intrinsically racy, it should only be used when it is
     * known that no threads are concurrently updating.
     */
    public void reset() {
        Cell[] as = cells; Cell a;
        base = 0L; // relies on fact that double 0 must have same rep as long
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    a.value = 0L;
            }
        }
    }

    /**
     * Equivalent in effect to {@link #sum} followed by {@link
     * #reset}. This method may apply for example during quiescent
     * points between multithreaded computations.  If there are
     * updates concurrent with this method, the returned value is
     * <em>not</em> guaranteed to be the final value occurring before
     * the reset.
     *
     * @return the sum
     */
    public double sumThenReset() {
        Cell[] as = cells; Cell a;
        double sum = Double.longBitsToDouble(base);
        base = 0L;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null) {
                    long v = a.value;
                    a.value = 0L;
                    sum += Double.longBitsToDouble(v);
                }
            }
        }
        return sum;
    }

    /**
     * Returns the String representation of the {@link #sum}.
     * @return the String representation of the {@link #sum}
     */
    public String toString() {
        return Double.toString(sum());
    }

    /**
     * Equivalent to {@link #sum}.
     *
     * @return the sum
     */
    public double doubleValue() {
        return sum();
    }

    /**
     * Returns the {@link #sum} as a {@code long} after a
     * narrowing primitive conversion.
     */
    public long longValue() {
        return (long)sum();
    }

    /**
     * Returns the {@link #sum} as an {@code int} after a
     * narrowing primitive conversion.
     */
    public int intValue() {
        return (int)sum();
    }

    /**
     * Returns the {@link #sum} as a {@code float}
     * after a narrowing primitive conversion.
     */
    public float floatValue() {
        return (float)sum();
    }

    /**
     * Serialization proxy, used to avoid reference to the non-public
     * Striped64 superclass in serialized forms.
     * @serial include
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        /**
         * The current value returned by sum().
         * @serial
         */
        private final double value;

        SerializationProxy(DoubleAdder a) {
            value = a.sum();
        }

        /**
         * Returns a {@code DoubleAdder} object with initial state
         * held by this proxy.
         *
         * @return a {@code DoubleAdder} object with initial state
         * held by this proxy.
         */
        private Object readResolve() {
            DoubleAdder a = new DoubleAdder();
            a.base = Double.doubleToRawLongBits(value);
            return a;
        }
    }

    /**
     * Returns a
     * <a href="../../../../serialized-form.html#java.util.concurrent.atomic.DoubleAdder.SerializationProxy">
     * SerializationProxy</a>
     * representing the state of this instance.
     *
     * @return a {@link SerializationProxy}
     * representing the state of this instance
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    /**
     * @param s the stream
     * @throws java.io.InvalidObjectException always
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}
