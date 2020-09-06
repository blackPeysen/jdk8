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

package java.lang;
import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 这个类提供线程局部变量。
 * 这些变量不同于它们的正常对应变量，因为每个访问一个变量的线程(通过其{@code get}或{@code set}方法)都有自己的、独立初始化的变量副本。
 * {@code ThreadLocal}实例通常是希望将状态与线程关联的类中的私有静态字段(例如，一个用户ID或事务ID).
 *
 * <p>例如，下面的类生成每个线程本地的唯一标识符.
 * 线程的id在第一次调用{@code ThreadId.get()}时被分配，并且在随后的调用中保持不变.
 * <pre>
 * import java.util.concurrent.atomic.AtomicInteger;
 *
 * public class ThreadId {
 *     // Atomic integer containing the next thread ID to be assigned
 *     private static final AtomicInteger nextId = new AtomicInteger(0);
 *
 *     // Thread local variable containing each thread's ID
 *     private static final ThreadLocal&lt;Integer&gt; threadId =
 *         new ThreadLocal&lt;Integer&gt;() {
 *             &#64;Override protected Integer initialValue() {
 *                 return nextId.getAndIncrement();
 *         }
 *     };
 *
 *     // Returns the current thread's unique ID, assigning it if necessary
 *     public static int get() {
 *         return threadId.get();
 *     }
 * }
 * </pre>
 * <p>每个线程持有一个隐式的线程本地变量副本的引用，只要线程是活着的并且{@code ThreadLocal}实例是可访问的;
 *      当一个线程离开后，它的线程本地实例的所有副本都会受到垃圾收集的影响(除非存在对这些副本的其他引用).
 *
 * @author  Josh Bloch and Doug Lea
 * @since   1.2
 */
public class ThreadLocal<T> {
    /**
     * 线程局部变量依赖于每个线程的线性探测散列映射附加到每个线程 (Thread.threadLocals and inheritableThreadLocals).
     * ThreadLocal对象充当键，通过threadLocalHashCode搜索。
     * 这是一个自定义哈希码(仅在ThreadLocalMaps中有用)，它在一般情况下消除了冲突，即相同线程使用连续构造的线程局部变量，而在不太常见的情况下保持良好的行为。
     */
    private final int threadLocalHashCode = nextHashCode();

    /**
     * 下一个要给出的哈希码。
     * 自动更新。从0开始
     */
    private static AtomicInteger nextHashCode = new AtomicInteger();

    /**
     * 连续生成的哈希码之间的区别——将隐式顺序线程本地id转换为接近最优扩散乘法哈希值，用于两个幂次大小的表.
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * 返回下一个哈希码.
     */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * 为这个线程局部变量返回当前线程的“初始值”。
     * 该方法将在线程第一次使用{@link #get}方法访问变量时被调用，
     * 除非线程之前调用了{@link #set}方法，在这种情况下，不会为该线程调用{@code initialValue}方法。
     * 通常，每个线程最多调用该方法一次，但在后续调用{@link #remove}和{@link #get}时，可能会再次调用该方法。
     *
     * <p>这个实现只返回{@code null};
     *      如果程序员希望线程局部变量有一个初始值，而不是{@code null}，{@code ThreadLocal}必须子类化，并且重写这个方法。
     *      通常，将使用匿名内部类.
     *
     * @return 这个线程的初始值
     */
    protected T initialValue() {
        return null;
    }

    /**
     * 创建一个线程局部变量。
     * 变量的初始值通过调用{@code Supplier}上的{@code get}方法确定.
     *
     * @param <S> 线程本地值的类型
     * @param supplier 用于确定初始值的供应商
     * @return 一个新的线程局部变量
     * @throws NullPointerException 如果指定的供应商为空
     * @since 1.8
     */
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }

    /**
     * 创建一个线程局部变量.
     * @see #withInitial(java.util.function.Supplier)
     */
    public ThreadLocal() {
    }

    /**
     * 返回这个线程局部变量的当前线程副本中的值。
     * 如果该变量没有当前线程的值，则首先通过调用{@link #initialValue}方法将其初始化为返回的值.
     *
     * @return the current thread's value of this thread-local
     */
    public T get() {
        // 获取当前线程
        Thread t = Thread.currentThread();
        // 根据线程信息获取对应集合
        ThreadLocalMap map = getMap(t);

        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    /**
     * set()的变体，用于建立初始值。
     * 如果用户覆盖了set()方法，则用代替set()方法
     *
     * @return 初始值
     */
    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }

    /**
     * 将当前线程对这个线程局部变量的副本设置为指定值。
     * 大多数子类不需要重写这个方法，只需要依赖{@link #initialValue}方法来设置线程局部变量的值.
     *
     * @param value 要存储在当前线程的this thread-local副本中的值.
     */
    public void set(T value) {
        // 获取当前线程
        Thread t = Thread.currentThread();

        /**
         * 获取当前线程实例上的的实例属性:ThreadLocalMap对象
         *      每一个线程实例上都对应有一个自身的ThreadLocalMap对象
         */
        ThreadLocalMap map = getMap(t);

        if (map != null)
            /**
             * 调用ThreadLocalMap对象的set()方法：
             *      key：是当前ThreadLocal对象
             *      value：是当前要设置的value值。
             */
            map.set(this, value);
        else
            /**
             * 用当前线程T 和 要设置的value创建一个新的ThreadLocal对象
             */
            createMap(t, value);
    }

    /**
     * 删除当前线程的这个线程本地变量的值.
     * 如果这个线程局部变量随后被当前线程{@linkplain #get read}所读取，它的值将通过调用它的{@link #initialValue}方法被重新初始化，
     * 除非它的值在过渡期间被当前线程为{@linkplain #set set}。
     * 这可能导致在当前线程中多次调用{@code initialValue}方法.
     *
     * @since 1.5
     */
     public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)
             m.remove(this);
     }

    /**
     * 获取与ThreadLocal关联的映射。
     * 在InheritableThreadLocal中被重写.
     *
     * @param  t 当前线程
     * @return the map
     */
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    /**
     * 创建与ThreadLocal关联的映射。在InheritableThreadLocal中被重写.
     *
     * @param t 当前线程
     * @param firstValue 映射的初始项的值
     */
    void createMap(Thread t, T firstValue) {
        /**
         * 先是调用ThreadLocalMap的构造函数，生成一个新的Map对象
         * 然后把该对象的引用赋值给当前线程T
         */
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    /**
     * 工厂方法来创建继承线程局部变量的映射。
     * 设计为只从线程构造函数调用.
     *
     * @param  parentMap 与父线程相关联的映射
     * @return 包含父类的可继承绑定的映射
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * 方法childValue显然是在子类InheritableThreadLocal中定义的，但它是在内部定义的，
     * 目的是提供createInheritedMap工厂方法，而不需要在InheritableThreadLocal中子类化map类。
     * 该技术优于在方法中嵌入instanceof测试的替代方法
     */
    T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * ThreadLocal的一个扩展，它从指定的{@code Supplier}获得初始值.
     */
    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }

    /**
     * ThreadLocalMap是一个定制的哈希映射，只适用于维护线程本地值。没有操作被导出到ThreadLocal类之外。
     * 这个类是包私有的，允许在类线程中声明字段。
     * 为了帮助处理非常大且长期存在的使用，哈希表条目对键使用WeakReferences。
     * 但是，由于引用队列不被使用，因此只有当表开始耗尽空间时，才能保证删除过时的条目。
     *      可能引发内存溢出，所以在使用完ThradLocal对象后，调用remove()，删除线程局部变量的引用。
     *
     * 表开始耗尽空间.
     */
    static class ThreadLocalMap {

        /**
         * 这个哈希映射中的条目扩展了WeakReference，使用它的主ref字段作为键(始终是ThreadLocal对象)。
         * 注意，空键(即entry.get() == null)意味着不再引用该键，因此可以从表中删除条目。
         * 这样的条目在下面的代码中被称为“陈旧条目”.
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** 与这个ThreadLocal关联的值. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * 初始容量必须是2的幂.
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * 根据需要调整表的大小。
         * 表长度必须总是2的幂.
         */
        private Entry[] table;

        /**
         * 表中的条目数.
         */
        private int size = 0;

        /**
         * 要调整大小的下一个大小值.
         * 默认是0
         */
        private int threshold;

        /**
         * 设置调整大小阈值，以保持在最坏的2/3的负载率.
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        /**
         * i对len取模.
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * 对len取模减量.
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        /**
         * 构造一个最初包含(firstKey, firstValue)的新映射。
         * ThreadLocalMaps是惰性构造的，所以我们只在至少有一个条目要放入其中时才创建一个.
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            /**
             * 根据ThreadLocal 的threadLocalHashCode 与 INITIAL_CAPACITY 获取到下标i
             */
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);

            // 将新的Entry赋值给数组下标
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        /**
         * 构造一个新的映射，包括来自给定父映射的所有可继承线程局部变量。
         * 仅由createInheritedMap调用.
         *
         * @param parentMap 与父线程相关联的映射.
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * 获取与key关联的条目。这个方法本身只处理快速路径:直接命中现有的键。
         * 否则，它继电器得到错过。这样做的目的是使直接命中的性能最大化，部分是通过使这种方法容易地不可分割.
         *
         * @param  key 线程本地对象
         * @return 与key关联的条目，如果没有，则为null
         */
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.get() == key)
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }

        /**
         * 当键在其直接哈希槽中找不到时使用的getEntry方法的版本.
         *
         * @param  key 线程本地对象
         * @param  i 键的散列代码的表索引
         * @param  e 表[i]中的条目
         * @return 与key关联的条目，如果没有，则为null
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null)
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * 设置与key关联的值.
         *
         * @param key 线程本地对象
         * @param value 要设置的值
         */
        private void set(ThreadLocal<?> key, Object value) {

            /**
             * 我们不像使用get()那样使用快速路径，因为使用set()创建新条目和使用it替换现有条目一样常见，在这种情况下，快速路径经常会失败
             */
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);

            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

        /**
         * 删除key条目.
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        /**
         * Replace a stale entry encountered during a set operation
         * with an entry for the specified key.  The value passed in
         * the value parameter is stored in the entry, whether or not
         * an entry already exists for the specified key.
         *
         * As a side effect, this method expunges all stale entries in the
         * "run" containing the stale entry.  (A run is a sequence of entries
         * between two null slots.)
         *
         * @param  key the key
         * @param  value the value to be associated with key
         * @param  staleSlot index of the first stale entry encountered while
         *         searching for key.
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value, int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            // Back up to check for prior stale entry in current run.
            // We clean out whole runs at a time to avoid continual
            // incremental rehashing due to garbage collector freeing
            // up refs in bunches (i.e., whenever the collector runs).
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = prevIndex(i, len))
                if (e.get() == null)
                    slotToExpunge = i;

            // Find either the key or trailing null slot of run, whichever
            // occurs first
            for (int i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                // If we find key, then we need to swap it
                // with the stale entry to maintain hash table order.
                // The newly stale slot, or any other stale slot
                // encountered above it, can then be sent to expungeStaleEntry
                // to remove or rehash all of the other entries in run.
                if (k == key) {
                    e.value = value;

                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // Start expunge at preceding stale entry if it exists
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // If we didn't find stale entry on backward scan, the
                // first stale entry seen while scanning for key is the
                // first still present in the run.
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // If key not found, put new entry in stale slot
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // If there are any other stale entries in run, expunge them
            if (slotToExpunge != staleSlot)
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }

        /**
         * 通过重新散列staleSlot和下一个空槽之间可能发生冲突的条目来删除陈旧的条目。
         * 这还会删除在尾随空值之前遇到的任何其他陈旧条目。见Knuth，第6.4节
         *
         * @param staleSlot 已知具有空键的槽的索引
         * @return staleSlot之后的下一个空槽的索引(staleSlot和这个槽之间的所有数据都将被检查以清除).
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // expunge entry at staleSlot
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            size--;

            // Rehash until we encounter null
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         * Heuristically scan some cells looking for stale entries.
         * This is invoked when either a new element is added, or
         * another stale one has been expunged. It performs a
         * logarithmic number of scans, as a balance between no
         * scanning (fast but retains garbage) and a number of scans
         * proportional to number of elements, that would find all
         * garbage but would cause some insertions to take O(n) time.
         *
         * @param i a position known NOT to hold a stale entry. The
         * scan starts at the element after i.
         *
         * @param n scan control: {@code log2(n)} cells are scanned,
         * unless a stale entry is found, in which case
         * {@code log2(table.length)-1} additional cells are scanned.
         * When called from insertions, this parameter is the number
         * of elements, but when from replaceStaleEntry, it is the
         * table length. (Note: all this could be changed to be either
         * more or less aggressive by weighting n instead of just
         * using straight log n. But this version is simple, fast, and
         * seems to work well.)
         *
         * @return true if any stale entries have been removed.
         */
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                i = nextIndex(i, len);
                Entry e = tab[i];
                if (e != null && e.get() == null) {
                    n = len;
                    removed = true;
                    i = expungeStaleEntry(i);
                }
            } while ( (n >>>= 1) != 0);
            return removed;
        }

        /**
         * 重新打包和/或调整表的大小。
         * 首先扫描整个表，删除过时的条目。如果这样做还不够，缩小表的大小，将表的大小加倍。
         */
        private void rehash() {
            expungeStaleEntries();

            // 使用较低的阈值加倍，以避免迟滞
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * 把table的容量增加一倍
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        // 帮助GC
                        e.value = null;
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         * 删除表中所有过时的条目.
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    expungeStaleEntry(j);
            }
        }
    }
}
