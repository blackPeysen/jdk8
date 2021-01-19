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

/**
 * 一个支持单变量无锁线程安全编程的类的小工具包。
 * 本质上，这个包中的类将{@code volatile}值、字段和数组元素的概念扩展到了那些同样提供了一个原子条件更新操作的表单:
 *
 *  <pre> {@code boolean compareAndSet(expectedValue, updateValue);}</pre>
 *
 * <p>这个方法(在不同的类中不同的参数类型)如果当前持有{@code expectedValue}，则自动地将变量设置为{@code updateValue}，成功时报告{@code true}。
 * 这个包中的类还包含获取和无条件设置值的方法，以及下面描述的较弱条件原子更新操作{@code weakCompareAndSet}。
 *
 * <p>这些方法的规范使实现能够使用在当代处理器上可用的高效的机器级原子指令。
 * 然而，在某些平台上，支持可能需要某种形式的内部锁定。
 * 因此，不能严格保证这些方法是非阻塞的——线程在执行操作之前可能会暂时阻塞。
 *
 * <p>Instances of classes
 * {@link java.util.concurrent.atomic.AtomicBoolean},
 * {@link java.util.concurrent.atomic.AtomicInteger},
 * {@link java.util.concurrent.atomic.AtomicLong}, and
 * {@link java.util.concurrent.atomic.AtomicReference}
 * 每一个都提供对对应类型的单个变量的访问和更新。
 * 每个类还为该类型提供适当的实用程序方法。
 * 例如，类{@code AtomicLong}和{@code AtomicInteger}提供了原子递增方法。
 * 一个应用程序是生成序列号，例如:
 *
 *  <pre> {@code
 * class Sequencer {
 *   private final AtomicLong sequenceNumber
 *     = new AtomicLong(0);
 *   public long next() {
 *     return sequenceNumber.getAndIncrement();
 *   }
 * }}</pre>
 *
 * <p>定义新的实用函数很简单，像{@code getAndIncrement}这样的实用函数可以自动地将函数应用到一个值上。
 *
 * <p>The memory effects for accesses and updates of atomics generally
 * follow the rules for volatiles, as stated in
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4">
 * The Java Language Specification (17.4 Memory Model)</a>:
 *
 * <ul>
 *
 *   <li> {@code get} has the memory effects of reading a
 * {@code volatile} variable.
 *
 *   <li> {@code set} has the memory effects of writing (assigning) a
 * {@code volatile} variable.
 *
 *   <li> {@code lazySet} has the memory effects of writing (assigning)
 *   a {@code volatile} variable except that it permits reorderings with
 *   subsequent (but not previous) memory actions that do not themselves
 *   impose reordering constraints with ordinary non-{@code volatile}
 *   writes.  Among other usage contexts, {@code lazySet} may apply when
 *   nulling out, for the sake of garbage collection, a reference that is
 *   never accessed again.
 *
 *   <li>{@code weakCompareAndSet} atomically reads and conditionally
 *   writes a variable but does <em>not</em>
 *   create any happens-before orderings, so provides no guarantees
 *   with respect to previous or subsequent reads and writes of any
 *   variables other than the target of the {@code weakCompareAndSet}.
 *
 *   <li> {@code compareAndSet}
 *   and all other read-and-update operations such as {@code getAndIncrement}
 *   have the memory effects of both reading and
 *   writing {@code volatile} variables.
 * </ul>
 *
 * <p>In addition to classes representing single values, this package
 * contains <em>Updater</em> classes that can be used to obtain
 * {@code compareAndSet} operations on any selected {@code volatile}
 * field of any selected class.
 *
 * {@link java.util.concurrent.atomic.AtomicReferenceFieldUpdater},
 * {@link java.util.concurrent.atomic.AtomicIntegerFieldUpdater}, and
 * {@link java.util.concurrent.atomic.AtomicLongFieldUpdater}
 * 是基于反射的实用程序，提供对相关字段类型的访问。
 * 它们主要用于原子数据结构中，在原子数据结构中，同一节点的几个{@code volatile}字段(例如，树节点的链接)独立地接受原子更新。
 * 这些类在如何以及何时使用原子更新方面提供了更大的灵活性，但代价是更笨拙的基于反射的设置、更不方便的使用和更弱的保证。
 *
 * <p>The
 * {@link java.util.concurrent.atomic.AtomicIntegerArray},
 * {@link java.util.concurrent.atomic.AtomicLongArray}, and
 * {@link java.util.concurrent.atomic.AtomicReferenceArray} 类
 * 进一步将原子操作支持扩展到这些类型的数组。这
 * 些类还为它们的数组元素提供了{@code volatile}访问语义，这在普通数组中是不受支持的。
 *
 * <p id="weakCompareAndSet">The atomic classes also support method
 * {@code weakCompareAndSet}, which has limited applicability.  On some
 * platforms, the weak version may be more efficient than {@code
 * compareAndSet} in the normal case, but differs in that any given
 * invocation of the {@code weakCompareAndSet} method may return {@code
 * false} <em>spuriously</em> (that is, for no apparent reason).  A
 * {@code false} return means only that the operation may be retried if
 * desired, relying on the guarantee that repeated invocation when the
 * variable holds {@code expectedValue} and no other thread is also
 * attempting to set the variable will eventually succeed.  (Such
 * spurious failures may for example be due to memory contention effects
 * that are unrelated to whether the expected and current values are
 * equal.)  Additionally {@code weakCompareAndSet} does not provide
 * ordering guarantees that are usually needed for synchronization
 * control.  However, the method may be useful for updating counters and
 * statistics when such updates are unrelated to the other
 * happens-before orderings of a program.  When a thread sees an update
 * to an atomic variable caused by a {@code weakCompareAndSet}, it does
 * not necessarily see updates to any <em>other</em> variables that
 * occurred before the {@code weakCompareAndSet}.  This may be
 * acceptable when, for example, updating performance statistics, but
 * rarely otherwise.
 *
 * <p>The {@link java.util.concurrent.atomic.AtomicMarkableReference}
 * class associates a single boolean with a reference.  For example, this
 * bit might be used inside a data structure to mean that the object
 * being referenced has logically been deleted.
 *
 * The {@link java.util.concurrent.atomic.AtomicStampedReference}
 * class associates an integer value with a reference.  This may be
 * used for example, to represent version numbers corresponding to
 * series of updates.
 *
 * <p>Atomic classes are designed primarily as building blocks for
 * implementing non-blocking data structures and related infrastructure
 * classes.  The {@code compareAndSet} method is not a general
 * replacement for locking.  It applies only when critical updates for an
 * object are confined to a <em>single</em> variable.
 *
 * <p>Atomic classes are not general purpose replacements for
 * {@code java.lang.Integer} and related classes.  They do <em>not</em>
 * define methods such as {@code equals}, {@code hashCode} and
 * {@code compareTo}.  (Because atomic variables are expected to be
 * mutated, they are poor choices for hash table keys.)  Additionally,
 * classes are provided only for those types that are commonly useful in
 * intended applications.  For example, there is no atomic class for
 * representing {@code byte}.  In those infrequent cases where you would
 * like to do so, you can use an {@code AtomicInteger} to hold
 * {@code byte} values, and cast appropriately.
 *
 * You can also hold floats using
 * {@link java.lang.Float#floatToRawIntBits} and
 * {@link java.lang.Float#intBitsToFloat} conversions, and doubles using
 * {@link java.lang.Double#doubleToRawLongBits} and
 * {@link java.lang.Double#longBitsToDouble} conversions.
 *
 * @since 1.5
 */
package java.util.concurrent.atomic;
