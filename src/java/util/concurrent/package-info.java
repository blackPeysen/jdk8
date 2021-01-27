/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/**
 * 实用程序类通常在并发编程中很有用。
 * 该软件包包括一些小的标准化可扩展框架，以及一些提供有用功能并且很繁琐或难以实现的类。
 * 这是主要组件的简要说明。另请参见{@link java.util.concurrent.locks}和{@link java.util.concurrent.atomic}软件包。
 *
 * <h2>Executors</h2>
 *
 * <b>接口类.</b>
 *
 * {@link java.util.concurrent.executor.Executor}是一个简单的标准化接口，用于定义自定义的类线程子系统，包括线程池，异步I/O和轻量级任务框架。
 * 取决于正在使用的具体Executor类，任务可以在新创建的线程，现有任务执行线程或{@link java.util.concurrent.executor.Executor＃execute  execute}线程中执行，
 * 并且可以顺序执行或同时执行。
 *
 * {@link java.util.concurrent.executor.ExecutorService}提供了一个更完整的异步任务执行框架。
 *      ExecutorService管理任务的排队和调度，并允许受控关闭。
 *
 * {@link java.util.concurrent.executor.ScheduledExecutorService}子接口和关联的接口增加了对延迟和定期任务执行的支持。
 *      ExecutorServices提供了一些方法，这些方法可以安排异步执行任何表示为{@link java.util.concurrent.Callable}的函数，可以模拟结果{@link java.lang.Runnable}。
 *
 * {@link java.util.concurrent.future.Future}返回函数的结果，允许确定执行是否完成，并提供取消执行的方法。
 *
 * {@link java.util.concurrent.future.RunnableFuture}是一个{@code Future}，它具有一个{@code run}方法，该方法在执行时会设置其结果。
 *
 * <p>
 *
 * <b>实现类.</b>
 *
 * 类{@link java.util.concurrent.executor.ThreadPoolExecutor}和{@link java.util.concurrent.executor.ScheduledThreadPoolExecutor} 提供可调的，灵活的线程池。
 *
 * {@link java.util.concurrent.executor.Executors}类提供了*最常见的Executor类型和配置的工厂方法，以及一些使用它们的实用程序方法。
 *      其他基于{@code Executors}的实用程序包括具体类{@link java.util.concurrent.future.FutureTask} 提供了Future的通用可扩展实现，
 *      以及 {@link java.util.concurrent.executor.ExecutorCompletionService }，可以帮助协调异步任务组的处理。
 *
 * <p>类{@link java.util.concurrent.fork.ForkJoinPool}提供了一个执行器，主要用于处理{@link java.util.concurrent.fork.ForkJoinTask}及其子类的实例。
 *      这些类采用了窃取工作的调度程序，该任务的调度程序达到了吞吐量高的吞吐量，这些任务符合经常在计算密集型并行处理中保持的限制。
 *
 * <h2>Queues</h2>
 *
 * {@link java.util.concurrent.blocking.queue.ConcurrentLinkedQueue}类提供了高效的可伸缩线程安全的非阻塞FIFO队列。
 * {@link java.util.concurrent.blocking.deque.ConcurrentLinkedDeque}类是类似的，但还支持{@link java.util.Deque}接口。
 *
 * <p>{@code java.util.concurrent}中的五个实现支持扩展的{@link java.util.concurrent.blocking.queue.BlockingQueue} 接口，
 *      该接口定义了put和take的阻塞版本：
 *          {@link java.util.concurrent.blocking.queue.LinkedBlockingQueue},
 *          {@link java.util.concurrent.blocking.queue.ArrayBlockingQueue},
 *          {@link java.util.concurrent.blocking.queue.SynchronousQueue},
 *          {@link java.util.concurrent.blocking.queue.PriorityBlockingQueue}, and
 *          {@link java.util.concurrent.blocking.queue.DelayQueue}.
 * 不同的类涵盖了最常见的用法上下文*用于生产者-消费者，消息传递，并行任务和相关的并行设计。
 *
 * <p>扩展接口{@link java.util.concurrent.blocking.queue.TransferQueue}，和实现{@link java.util.concurrent.blocking.queue.LinkedTransferQueue}
 *      引入了同步{@code transfer}方法（以及相关的功能），其中生产者可以选择阻止其消费者。
 *
 * <p>{@link java.util.concurrent.blocking.deque.BlockingDeque}接口扩展了{@code BlockingQueue}以支持FIFO和LIFO（基于堆栈）操作。
 *      类{@link java.util.concurrent.blocking.deque.LinkedBlockingDeque} 提供了一个实现。
 *
 * <h2>Timing</h2>
 *
 * {@link java.util.concurrent.TimeUnit}类提供*多个粒度（包括纳秒），用于指定和控制基于超时的操作。
 * 包中的大多数类除不确定的等待时间外，还包含基于超时的操作。
 * 在所有使用超时的情况下，超时都指定方法表示超时之前应等待的最短时间。
 * 实施会“尽最大努力” 在超时发生后尽快检测到超时。
 * 但是，在检测到超时与该超时之后再次实际执行的线程之间可能会经过不确定的时间。
 * 所有接受timeout 参数的方法都将小于或等于零的值视为根本不等待。
 * 要“永远”等待，您可以使用{@code Long.MAX_VALUE}的值。
 *
 *
 * <h2>同步器</h2>
 *
 * 五类辅助通用的专用同步惯用语。
 *
 * <li>{@link java.util.concurrent.Semaphore}是经典的并发工具。
 *
 * <li>{@link java.util.concurrent.CountDownLatch}是一个非常简单但非常常见的实用程序，用于阻塞直到给定数量的信号，事件或条件成立。
 *
 * <li>{@link java.util.concurrent.CyclicBarrier}是可重置的多路同步点，在某些并行编程样式中很有用。
 *
 * <li>{@link java.util.concurrent.Phaser}提供了更灵活的屏障形式，可用于控制多个线程之间的分阶段计算。
 *
 * <li>{@link java.util.concurrent.syncTool.Exchanger}允许两个线程在集合点交换对象，并且在几种管道设计中很有用。
 *
 * <h2>并发集合</h2>
 *
 * 除队列外，此包还提供了Collection实现，旨在用于多线程上下文：
 *      {@link java.util.concurrent.collection.ConcurrentHashMap},
 *      {@link java.util.concurrent.collection.ConcurrentSkipListMap},
 *      {@link java.util.concurrent.collection.ConcurrentSkipListSet},
 *      {@link java.util.concurrent.collection.CopyOnWriteArrayList}, and
 *      {@link java.util.concurrent.collection.CopyOnWriteArraySet}.
 * 当期望有多个线程访问给定集合时，{@code ConcurrentHashMap}通常比同步的{{code HashMap}更可取，
 * 而{@code ConcurrentSkipListMap}通常比同步的{@code TreeMap}更可取。
 * 当预期的读取和遍历次数大大超过对列表的更新次数时，{@code CopyOnWriteArrayList}比同步的{@code ArrayList}更可取。
 *
 * <p>与该包中的某些类一起使用的“并发”前缀是一个速记，表示与类似的“同步”类有所不同。
 * 例如，{@code java.util.Hashtable}和{@code Collections.synchronizedMap（new HashMap（））}已同步。
 * 但是{@link * java.util.concurrent.collection.ConcurrentHashMap}是“并发的”。
 * 并发集合是线程安全的，但不受单个排除锁的约束。
 * 在ConcurrentHashMap的特殊情况下，它安全地允许任意数量的*并发读取以及可调数量的并发写入。
 * 当您需要阻止通过单个锁对集合的所有访问时，“同步”类可能会很有用，以较差的可伸缩性为代价。
 * 在预期多个线程访问一个公共集合的其他情况下，通常最好使用“并发”版本。
 * 并且当不共享集合或仅当*持有其他锁时才可访问*时，不同步的集合是更可取的。
 *
 * <p id="Weakly">大多数并发的Collection实现（包括大多数队列）也与通常的{@code java.util} 约定不同，
 *      它们的{@linkplain java.util.Iterator Iterators} *和{@linkplain java.util.Spliterator Spliterators}提供了弱一致性，而不是快速失败遍历：
 * <ul>
 * <li>他们可能会与其他操作同时进行
 * <li>他们永远不会抛出{@link java.util.ConcurrentModificationException  ConcurrentModificationException}
 * <li>它们可以保证遍历原先存在的元素一次，并且可以（但不保证）反映出构建后的任何修改。
 * </ul>
 *
 * <h2 id="MemoryVisibility">内存一致性属性</h2>
 *
 * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.4.5">
 * Java语言规范的第17章,定义了与内存操作（如共享变量的读和写）之间的happens-before关系。
 * 仅当写操作在读操作之前发生时，才保证一个线程写的结果对另一线程的读取是可见的。
 * {@code Synchronized}和{@code volatile}构造，以及{@code Thread.start（）}和{@code Thread.join（）}方法可以形成happens-before关系。
 * 特别是：
 *
 * <ul>
 *   <li>线程中的每个动作于在该线程中的每个动作在程序顺序中排在后面。
 *
 *   <li>监视器的解锁（{@代码同步块或方法退出）在同一监视器的每个后续锁定（{@代码同步}块或方法条目）之前。
 *   而且，由于happens-before关系是可传递的，因此在监控该线程的任何线程之后，在解锁happen-before 所有动作之前，线程的所有动作。
 *
 *   <li>对{@code volatile}字段的写操作在每个之后的同一字段中都要在发生之前进行。
 *   {@code volatile}字段的写入和读取具有与进入和退出监视器类似的内存一致性影响，但并不需要互斥锁定。
 *
 *   <li>在启动的线程中的任何操作发生之前的线程上调用{@code start}。
 *
 *   <li>线程发生在其他任何线程之前的所有操作从该线程上的{@code join}成功返回。
 *
 * </ul>
 *
 *
 * {@code java.util.concurrent}中所有类的方法及其子包将这些保证扩展到更高级别的同步。特别是：
 *
 *   <li>在将对象放入任何并发集合中的线程之前的线程中的操作，然后从另一个线程的集合中将该元素访问或从集合中删除该操作。
 *
 *   <li>在happen-before 开始执行之前，在向{@code Executor}提交{@code Runnable} 之前在线程中执行的操作。
 *          对于提交给{@code ExecutorService}的{@code Callables}同样。
 *
 *   <li>Actions taken by the asynchronous computation represented by a
 *   {@code Future} <i>happen-before</i> actions subsequent to the
 *   retrieval of the result via {@code Future.get()} in another thread.
 *
 *   <li>Actions prior to "releasing" synchronizer methods such as
 *   {@code Lock.unlock}, {@code Semaphore.release}, and
 *   {@code CountDownLatch.countDown} <i>happen-before</i> actions
 *   subsequent to a successful "acquiring" method such as
 *   {@code Lock.lock}, {@code Semaphore.acquire},
 *   {@code Condition.await}, and {@code CountDownLatch.await} on the
 *   same synchronizer object in another thread.
 *
 *   <li>For each pair of threads that successfully exchange objects via
 *   an {@code Exchanger}, actions prior to the {@code exchange()}
 *   in each thread <i>happen-before</i> those subsequent to the
 *   corresponding {@code exchange()} in another thread.
 *
 *   <li>Actions prior to calling {@code CyclicBarrier.await} and
 *   {@code Phaser.awaitAdvance} (as well as its variants)
 *   <i>happen-before</i> actions performed by the barrier action, and
 *   actions performed by the barrier action <i>happen-before</i> actions
 *   subsequent to a successful return from the corresponding {@code await}
 *   in other threads.
 *
 * </ul>
 *
 * @since 1.5
 */
package java.util.concurrent;
