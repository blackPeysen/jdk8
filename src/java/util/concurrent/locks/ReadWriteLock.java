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

package java.util.concurrent.locks;

/**
 * 一个{@code ReadWriteLock}维护一对关联的{@link Lock 锁}，一个用于只读操作，一个用于写操作。
 * 只要没有写线程，多个读线程可以同时持有{@link #readLock read lock}。
 * {@link #writeLock 写锁}是独占的。
 *
 * <p>所有{@code ReadWriteLock}实现必须保证{@code writeLock}操作的内存同步效果
 * (在{@link Lock}接口中指定)对关联的{@code readLock}也适用。
 * 也就是说，一个成功获取读锁的线程将看到在以前的写锁版本上所做的所有更新。
 *
 * <p>与互斥锁相比，读写锁在访问共享数据时允许更大的并发级别。
 * 利用了这样一个事实，即一次只能有一个线程（一个writer线程）可以修改共享数据，
 * 但在许多情况下，任何数量的线程都可以同时读取数据（因此阅读器线程）。
 *
 * 从理论上讲，与使用互斥锁相比，使用读写锁允许的并发性增加将导致性能提高。
 * 实际上，并发性的增加只能在多处理器上完全实现，并且只有在共享数据的访问模式合适的情况下才能实现。
 *
 * <p>读写锁是否会比使用互斥锁提高性能，取决于与修改相比，读取数据的频率，读写操作的持续时间以及争用数据-即将尝试同时读取或写入数据的线程数。
 * 例如，一个最初填充有数据然后很少修改，而后又被频繁搜索（例如某种目录）的集合是读写锁的理想选择。
 * 但是，如果更新变得频繁，那么数据将大部分时间专门用于锁定，并且并发几乎没有增加。
 * 此外，如果读取操作太短读写锁实现的开销（本质上*比互斥锁更复杂）会控制执行成本，特别是因为许多读写锁实现仍在进行序列化所有线程都通过一小段代码。
 * 最终，只有通过分析和测量才能确定使用读写锁是否适合您的应用程序。
 *
 *
 * <p>尽管读写锁的基本操作很简单，但是实现必须做出许多策略决策，而这些决策可能会影响给定应用程序中读写锁的有效性。
 * 这些政策的示例包括：
 * <ul>
 * <li>当读取器和写入器都在等待时，在写入器释放写锁定时，确定是授予读取锁定还是写入锁定。
 * 编写者的偏好很常见，因为期望写的内容简短且不频繁。
 * 读者喜好不太普遍，因为如果读者经常如期且预期寿命长，可能会导致较长的写延迟。
 * 公平或“有序”的实现也是可能的。
 *
 * <li>确定在读取器处于活动状态且写入器正在等待时请求读取锁定的读取器是否被授予读取锁定。
 * 对阅读者的偏好可以无限期地延迟编写者，而对作家的偏好可以降低并发的可能性。
 *
 * <li>确定锁是否可重入：具有写锁的线程可以重新获取它吗？持有写锁的同时可以获取读锁吗？读锁本身是否可重入？
 *
 * <li>是否可以在不允许中间写程序的情况下将写锁降级为读锁？可以将读取锁定升级为写入锁定吗，而不是优先于其他正在等待的读取器或写入器？
 *
 * </ul>
 * 在评估给定实现对应用程序的适用性时，应考虑所有这些因素。
 *
 * @see ReentrantReadWriteLock
 * @see Lock
 * @see ReentrantLock
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface ReadWriteLock {
    /**
     * 返回用于读取的锁。
     *
     * @return 用于阅读的锁
     */
    Lock readLock();

    /**
     * 返回用于写入的锁。
     *
     * @return 用于写的锁
     */
    Lock writeLock();
}
