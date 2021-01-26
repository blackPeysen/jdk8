/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/**
 * 将atomic 进行分类：
 *  1、基本数据类型
 *      {@link java.util.concurrent.atomic.AtomicBoolean}：以原子更新的方式更新boolean
 *      {@link java.util.concurrent.atomic.AtomicInteger}：以原子更新的方式更新Integer
 *      {@link java.util.concurrent.atomic.AtomicLong}：以原子更新的方式更新Long
 *
 * 2、基本数据类型数组
 *      {@link java.util.concurrent.atomic.AtomicIntegerArray}：原子更新整型数组中的元素
 *      {@link java.util.concurrent.atomic.AtomicLongArray}：原子更新长整型数组中的元素
 *      {@link java.util.concurrent.atomic.AtomicReferenceArray}：原子更新引用类型数组中的元素
 *
 *  3、原子更新引用类型
 *      {@link java.util.concurrent.atomic.AtomicReference}：以原子更新的方式更新引用类型
 *      {@link java.util.concurrent.atomic.AtomicReferenceFieldUpdater}：原子更新引用类型里的字段；
 *      {@link java.util.concurrent.atomic.AtomicMarkableReference}：原子更新带有标记位的引用类型
 *
 * 4、原子更新字段类型
 *      {@link java.util.concurrent.atomic.AtomicIntegerFieldUpdater}：原子更新整型字段类；
 *      {@link java.util.concurrent.atomic.AtomicLongFieldUpdater}：原子更新长整型字段类；
 *      {@link java.util.concurrent.atomic.AtomicStampedReference}：原子更新引用类型，解决ABA问题
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
 * <p>类的实例
 * {@link java.util.concurrent.atomic.AtomicBoolean},
 * {@link java.util.concurrent.atomic.AtomicInteger},
 * {@link java.util.concurrent.atomic.AtomicLong}, and
 * {@link java.util.concurrent.atomic.AtomicReference}
 * 每一个都提供对对应类型的单个变量的访问和更新。
 * 每个类还为该类型提供适当的实用程序方法。
 * 例如，类{@code AtomicLong}和{@code AtomicInteger}提供了原子递增方法。
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
 *   <li> {@code get}具有读取{@code volatile}变量的存储效果。
 *
 *   <li> {@code set}具有写入（分配）{@code volatile}变量的记忆效应。
 *
 *   <li> {@code lazySet}具有写入（分配）{@code volatile}变量的内存效果，除了它允许使用后续（但不是以前）的内存操作进行重排序，
 *   而这些内存操作本身并不对普通的非-施加重排序约束。
 *   {@code volatile}写在其他用法上下文中，{@code lazySet}可能适用于为避免垃圾回收而将永不再访问的引用。
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
 * <p>除了表示单个值的类之外，此包还包含Updater类，这些类可用于在任何选定类的任何选定{@code volatile} 字段上获取{@code compareAndSet}操作。
 *
 * {@link java.util.concurrent.atomic.AtomicReferenceFieldUpdater},
 * {@link java.util.concurrent.atomic.AtomicIntegerFieldUpdater}, and
 * {@link java.util.concurrent.atomic.AtomicLongFieldUpdater}
 * 是基于反射的实用程序，提供对相关字段类型的访问。
 * 它们主要用于原子数据结构中，在原子数据结构中，同一节点的几个{@code volatile}字段(例如，树节点的链接)独立地接受原子更新。
 * 这些类在如何以及何时使用原子更新方面提供了更大的灵活性，但代价是更笨拙的基于反射的设置、更不方便的使用和更弱的保证。
 *
 * <p>{@link java.util.concurrent.atomic.AtomicIntegerArray},
 * {@link java.util.concurrent.atomic.AtomicLongArray}, and
 * {@link java.util.concurrent.atomic.AtomicReferenceArray} 类
 * 进一步将原子操作支持扩展到这些类型的数组。
 * 这些类还为它们的数组元素提供了{@code volatile}访问语义，这在普通数组中是不受支持的。
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
 * <p>{@link java.util.concurrent.atomic.AtomicMarkableReference} 类将单个布尔值与引用关联。
 * 例如，该位可能在数据结构内使用，表示被引用的对象在逻辑上已被删除。
 *
 * {@link java.util.concurrent.atomic.AtomicStampedReference} 类将整数值与引用关联。
 * 例如，可以使用来表示与系列更新相对应的版本号。
 *
 * <p>原子类主要设计为构建块，用于实现非阻塞数据结构和相关的基础结构类。
 * {@code compareAndSet}方法不是锁定的通用替代。它仅在对象的关键更新被限制在单个变量中时适用。
 *
 * <p>原子类不是* {@code java.lang.Integer}和相关类的通用替代品。
 * 它们不会定义以下方法，例如{@code equals}，{@code hashCode}和{@code compareTo}。 （由于期望原子变量被*突变，因此它们对于哈希表键是不好的选择。）
 * 此外，类仅针对那些在预期应用程序中通常有用的类型提供。
 * 例如，没有用于的原子类表示{@code byte}。
 * 如果您不希望这样做，可以使用{@code AtomicInteger}来保存{@code byte}值，并进行适当的转换。
 *
 *
 * 您也可以使用
 * {@link java.lang.Float#floatToRawIntBits} and
 * {@link java.lang.Float#intBitsToFloat} conversions, and doubles using
 * {@link java.lang.Double#doubleToRawLongBits} and
 * {@link java.lang.Double#longBitsToDouble} conversions.
 *
 * @since 1.5
 */
package java.util.concurrent.atomic;
