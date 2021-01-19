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
 * 接口和类为锁定和等待条件提供了一个框架，这与内置同步和监视器不同。
 * 该框架在使用锁和条件方面提供了更大的灵活性，但代价是语法更加笨拙。
 *
 * <p>The {@link java.util.concurrent.locks.Lock} interface supports
 * locking disciplines that differ in semantics (reentrant, fair, etc),
 * and that can be used in non-block-structured contexts including
 * hand-over-hand and lock reordering algorithms.  The main implementation
 * is {@link java.util.concurrent.locks.ReentrantLock}.
 *
 * <p>The {@link java.util.concurrent.locks.ReadWriteLock} interface
 * similarly defines locks that may be shared among readers but are
 * exclusive to writers.  Only a single implementation, {@link
 * java.util.concurrent.locks.ReentrantReadWriteLock}, is provided, since
 * it covers most standard usage contexts.  But programmers may create
 * their own implementations to cover nonstandard requirements.
 *
 * <p>The {@link java.util.concurrent.locks.Condition} interface
 * describes condition variables that may be associated with Locks.
 * These are similar in usage to the implicit monitors accessed using
 * {@code Object.wait}, but offer extended capabilities.
 * In particular, multiple {@code Condition} objects may be associated
 * with a single {@code Lock}.  To avoid compatibility issues, the
 * names of {@code Condition} methods are different from the
 * corresponding {@code Object} versions.
 *
 * <p>The {@link java.util.concurrent.locks.AbstractQueuedSynchronizer}
 * class serves as a useful superclass for defining locks and other
 * synchronizers that rely on queuing blocked threads.  The {@link
 * java.util.concurrent.locks.AbstractQueuedLongSynchronizer} class
 * provides the same functionality but extends support to 64 bits of
 * synchronization state.  Both extend class {@link
 * java.util.concurrent.locks.AbstractOwnableSynchronizer}, a simple
 * class that helps record the thread currently holding exclusive
 * synchronization.  The {@link java.util.concurrent.locks.LockSupport}
 * class provides lower-level blocking and unblocking support that is
 * useful for those developers implementing their own customized lock
 * classes.
 *
 * @since 1.5
 */
package java.util.concurrent.locks;
