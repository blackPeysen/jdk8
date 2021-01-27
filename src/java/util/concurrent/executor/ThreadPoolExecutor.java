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

package java.util.concurrent.executor;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.*;
import java.util.concurrent.blocking.queue.ArrayBlockingQueue;
import java.util.concurrent.blocking.queue.BlockingQueue;
import java.util.concurrent.blocking.queue.LinkedBlockingQueue;
import java.util.concurrent.blocking.queue.SynchronousQueue;
import java.util.concurrent.exception.RejectedExecutionException;
import java.util.concurrent.future.FutureTask;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

/**
 * 一个{@link ExecutorService}，它使用可能是多个池线程中的一个来执行每个提交的任务，这些线程通常是使用{@link Executors}工厂方法配置的。
 *
 * <p>线程池解决了两个不同的问题：
 *      由于减少了每个任务的调用开销，它们通常在执行大量*异步任务时提供改进的性能，
 *      并且它们提供了一种绑定和管理资源（包括线程）的方法，这些资源包括执行任务的集合。
 *      每个{@code ThreadPoolExecutor}还维护一些基本统计信息，例如已完成任务的数量。
 *
 * <p>为了在广泛的上下文中有用，此类提供了许多可调整的参数和可扩展性挂钩。
 * 但是，建议程序员使用更方便的{@link Executors}工厂方法：
 *      {@link Executors＃newCachedThreadPool}（无边界线程池，并带有自动线程回收），
 *      {@link Executors＃newFixedThreadPool}（固定大小的线程池）
 *      {@link  Executors＃newSingleThreadExecutor}（单个后台线程），
 * 它们为最常见的使用情况预配置设置方案。
 * 否则，在手动配置和调整此类时，请使用以下指南：
 *
 * <dl>
 *
 * <dt>核心和最大池大小</dt>
 *
 * <dd>{@code ThreadPoolExecutor}将根据corePoolSize（请参阅{@link #getCorePoolSize}）和
 *      maximumPoolSize（请参阅{@link #getMaximumPoolSize）设置的范围自动调整池大小（请参阅{@link #getPoolSize}） }）。
 *
 *  当通过方法{@link #execute（Runnable）}提交新任务时，并且正在运行的线程少于corePoolSize线程，将创建一个新线程来处理该请求，即使其他工作线程处于空闲状态。
 *  如果正在运行的corePoolSize多于一个，但小于maximumPoolSize个线程，则仅当队列已满时才创建新线程。
 *  通过将corePoolSize和maximumPoolSize 设置为相同，可以创建固定大小的线程池。
 *  通过将maximumPoolSize设置为本质上不受限制的值，例如{@code Integer.MAX_VALUE}，您可以允许池容纳任意数量的并发任务。
 *  最典型地，核心和最大池的大小仅在构造时设置，但也可以使用{@link #setCorePoolSize}和{@link #setMaximumPoolSize}动态更改。
 *
 * <dt>按需施工</dt>
 *
 * <dd>默认情况下，甚至最初都会创建核心线程并仅在新任务到达时启动它们，
 *      但是可以使用方法{@link #prestartCoreThread}或{@link #prestartAllCoreThreads}动态地覆盖它。如果*使用非空队列构造池，则可能要预启动线程。
 * </dd>
 *
 * <dt>创建新线程</dt>
 *
 * <dd>使用{@link ThreadFactory}创建新线程。
 *  如果未另行指定，则使用{@link Executors＃defaultThreadFactory}，以创建所有线程，它们都位于相同的{@link ThreadGroup}中，
 *  并且具有相同的{@code NORM_PRIORITY}优先级和非守护进程状态。
 *  通过提供一个不同的ThreadFactory，您可以更改线程的名称，线程组，优先级，守护程序状态等。
 *  如果{@code ThreadFactory}在被询问时无法创建线程，则从{@code newThread}返回null，执行者将继续，但可能无法执行任何任务。
 *  线程应该具有“modifyThread” {@code RuntimePermission}。
 *  如果使用该池的工作线程或其他线程不具有此权限，则服务可能会降级：配置更改可能不会及时生效，并且关闭池可能保持处于可以终止的状态。没完成。
 * </dd>
 *
 * <dt>保活时间</dt>
 *
 * <dd>如果当前池中的线程数量超过corePoolSize线程，则如果多余的线程空闲时间超过keepAliveTime，则将终止它们（请参阅{@link #getKeepAliveTime（TimeUnit）}）。
 *  当未积极使用池时，这提供了一种减少资源消耗的方法。如果池在以后变得更活跃，将构造新线程。
 *  还可以使用方法{@link #setKeepAliveTime（long，TimeUnit）}动态更改此参数。
 *  使用{@code Long.MAX_VALUE}的值{@link TimeUnit＃NANOSECONDS}有效地使空闲线程永远不会在关闭之前终止。
 *  默认情况下，仅当corePoolSize线程多时，保持活动策略适用。
 *  但是，只要keepAliveTime值非零，方法{@link #allowCoreThreadTimeOut（boolean）}也可以用于将该超时策略应用于核心线程。
 * </dd>
 *
 * <dt>排队</dt>
 *
 * <dd>任何{@link java.util.concurrent.blocking.queue.BlockingQueue}均可用于传输和保留提交的任务。此队列的使用与池大小交互：
 *      <li> 如果正在运行的线程少于corePoolSize线程，则执行器总是更喜欢添加新线程而不是排队。</li>
 *      <li> 如果正在运行corePoolSize或更多线程，则执行器总是更喜欢对请求进行排队，而不是添加新的线程。</li>
 *      <li> 如果无法将请求排队，则将创建一个新线程，除非超过了maximumPoolSize，在这种情况下，该任务将被拒绝。</li>
 *
 * 有三种一般的排队策略：
 * <li> 直接交接:
 *      一个工作队列的不错的默认选择是{@link SynchronousQueue}，它可以将任务移交给线程，而不必另外保留它们。
 *      在这里，如果没有立即可用的线程来运行任务，则尝试将任务排队将失败，因此将创建一个新线程。
 *      处理可能具有内部依赖性的请求集时，此策略避免了锁定。
 *      直接越区切换通常需要无限制的maximumPoolSizes来避免拒绝新提交的任务。
 *      反过来，这允许当命令继续平均到达速度比其可处理速度快时无限增长线程的可能性。
 * </li>
 *
 * <li>无限队列:
 *      当所有corePoolSize线程都忙时，使用无界队列（例如，没有预定义容量的{@link LinkedBlockingQueue}）将导致新任务在队列中等待。
 *      因此，最多只能创建corePoolSize线程。 （因此，maximumPoolSize 的值没有任何作用。）
 *      当每个任务完全独立于其他任务时，这可能是适当的，因此任务不会彼此影响执行；例如，在网页服务器中。
 *      虽然这种排队方式对于消除短暂的请求突发很有用，但它承认当命令继续以平均速度比其可处理的速度继续到达时，工作队列无限制增长的可能性。
 * </li>
 *
 * <li>有界队列:
 *      当与有限的maximumPoolSizes一起使用时，有界队列（例如{@link ArrayBlockingQueue}）有助于防止资源耗尽，但是调整和控制起来会更加困难。
 *      队列大小和最大池大小可以相互折衷：使用大队列和小池可以最大程度地减少CPU使用率，OS资源和上下文切换开销，但可能导致人为降低吞吐量。
 *      如果任务频繁阻塞（例如如果它们受I / O约束），则系统可能能够为线程安排比您原本所允许的更多的时间。
 *      使用小队列通常需要更大的池大小，这会使CPU繁忙，但是可能会遇到不可接受的调度开销，这也会降低吞吐量。
 * </li>
 *
 * </dd>
 *
 * <dt>被拒绝的任务</dt>
 *
 * <dd>当执行器已关闭时，以及在执行器对最大线程数和工作队列使用有限范围时，在方法{@link #execute（Runnable）}中提交的新任务将被拒绝。容量，并且饱和。
 *      在任何一种情况下，{@code execute}方法都会调用其{@link RejectedExecutionHandler}的{@link RejectedExecutionHandler＃rejectedExecution（Runnable，ThreadPoolExecutor）} 方法。
 * 提供了四个预定义的处理程序策略：
 *
 *  <li> 在默认的{@link ThreadPoolExecutor.AbortPolicy}中，处理程序在拒绝时抛出运行时{@link java.util.concurrent.exception.RejectedExecutionException}。 </li>
 *  <li> 在{@link ThreadPoolExecutor.CallerRunsPolicy}中，调用{@code execute}的线程本身运行任务。这提供了一种简单的反馈控制机制，该机制将减慢新任务的提交速度。 </li>
 *  <li> 在{@link ThreadPoolExecutor.DiscardPolicy}中，简单地删除了无法执行的任务。  </li>
 *  <li> 在{@link ThreadPoolExecutor.DiscardOldestPolicy}中，如果未关闭执行程序，则将丢弃工作队列开头的任务，然后重试执行（这可能会再次失败，导致此重复。）</li>
 *
 * 可以定义和使用其他类型的{@link RejectedExecutionHandler}类。这样做需要一定的注意尤其是在设计策略仅在特定容量或排队策略下工作时。
 *  </dd>
 *
 * <dt>挂钩方法</dt>
 *
 * <dd>此类提供{@code protected}可重写{@link #beforeExecute（Thread，Runnable）}和{@link #afterExecute（Runnable，Throwable）}方法，
 *  这些方法在执行每个任务之前和之后被称为。
 *  这些可用于操纵执行环境；例如，重新初始化ThreadLocals，收集统计信息或添加日志条目。
 *  另外，方法{@link #terminated}可以被覆盖以执行一旦执行程序完全终止，则需要执行的任何特殊处理。
 *
 * <p>如果钩子或回调方法引发异常，则内部worker线程可能进而失败并突然终止。</dd>
 *
 * <dt>队列维护</dt>
 *
 * <dd>方法{@link #getQueue（）}允许访问工作队列，以进行监视和调试。强烈建议不要将此方法用于任何其他目的。
 * 两种提供的方法{@link #remove（Runnable）}和{@link #purge}可用于取消大量排队任务时的存储回收。</dd>
 *
 * <dt>定案</dt>
 *
 * <dd>程序 AND 中不再引用的池中没有剩余线程，将自动{@code shutdown}。
 *      如果您甚至希望确保即使在用户忘记调用{@link #shutdown}时也可以回收未引用的池，
 *      那么您必须通过设置适当的keep-alive时间（使用下限）来设置未使用的线程最终会死掉零个核心线程或设置{@link #allowCoreThreadTimeOut（boolean）}。
 * </dd>
 *
 * </dl>
 *
 * <p><b>扩展示例</b>.
 * 此类的大多数扩展覆盖一个或多个受保护的hook方法。
 * 例如，这是一个子类，它添加了一个简单的暂停/继续功能：
 *
 *  <pre> {@code
 * class PausableThreadPoolExecutor extends ThreadPoolExecutor {
 *   private boolean isPaused;
 *   private ReentrantLock pauseLock = new ReentrantLock();
 *   private Condition unpaused = pauseLock.newCondition();
 *
 *   public PausableThreadPoolExecutor(...) { super(...); }
 *
 *   protected void beforeExecute(Thread t, Runnable r) {
 *     super.beforeExecute(t, r);
 *     pauseLock.lock();
 *     try {
 *       while (isPaused) unpaused.await();
 *     } catch (InterruptedException ie) {
 *       t.interrupt();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void pause() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = true;
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void resume() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = false;
 *       unpaused.signalAll();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ThreadPoolExecutor extends AbstractExecutorService {

    /**
     * 主池控制状态ctl是一个原子整数包装两个概念字段:
     *          workerCount，指示有效线程数
     *          runState，指示是否运行，关闭等
     *
     * 为了将它们打包为一个int，我们将workerCount限制为（2 ^ 29）-1（约5亿个）线程，而不是（2 ^ 31）-1（2 *十亿）个其他可以表示的线程。
     * 如果将来可能出现此问题，可以将该变量更改为AtomicLong，并在下面调整shift / mask常数。
     * 但是直到需要出现时，使用int的这段代码才更快，更简单。
     *
     * workerCount是已被允许且不允许停止的工人数。
     * 该值可能与实际的活动线程数暂时不同，例如，当ThreadFactory在被询问时未能创建线程，
     * 而当退出的线程仍在执行终止之前的簿记操作时，该值可能会暂时不同。
     * 用户可见的池大小报告为工作集的当前大小。
     *
     * runState提供主要的生命周期控制，并具有以下值：
     *   RUNNING:  接受新任务并处理排队的任务
     *   SHUTDOWN: 不接受新任务，而是处理排队的任务
     *   STOP:     不接受新任务，不处理排队任务，和中断进行中的任务
     *   TIDYING:  所有任务都已终止，workerCount为零，线程转换为状态TIDYING,将运行Terminated（）挂钩方法
     *   TERMINATED: terminated() 已完成
     *
     * 这些值之间的数字顺序很重要，可以进行有序的比较。 runState随时间单调增加，但不必达到每个状态。过渡是：
     *      RUNNING -> SHUTDOWN:            在调用shutdown（）时，可能隐式在finalize（）中
     *      (RUNNING or SHUTDOWN) -> STOP:  调用shutdownNow（）时
     *      SHUTDOWN -> TIDYING:            当队列和池都为空时
     *      STOP -> TIDYING:                当池为空时
     *      TIDYING -> TERMINATED:          当Terminate（）挂钩方法完成时
     *
     *
     * 当状态达到TERMINATED时，在awaitTermination（）中等待的线程将返回。
     *
     * 检测到从SHUTDOWN到TIDYING的转换比您想要的要简单得多，因为在SHUTDOWN状态期间队列在非空之后可能变为空，
     * 反之亦然，但是我们只有在看到队列为空后才能终止，请参阅workerCount为0（有时需要重新检查-请参阅下面的）。
     */
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    // runState存储在高位
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;

    // 包装和开箱CTL
    private static int runStateOf(int c)     { return c & ~CAPACITY; }

    //获取当前worker工作者的数量
    private static int workerCountOf(int c)  { return c & CAPACITY; }

    private static int ctlOf(int rs, int wc) { return rs | wc; }

    /*
     * 不需要解压缩ctl的位字段访问器。 这些取决于位布局和workerCount永远不会为负。
     */

    private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }

    /**
     * 尝试CAS递增ctl的workerCount字段
     */
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    /**
     * 尝试CAS递减ctl的workerCount字段
     */
    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    /**
     * 减少ctl的workerCount字段。仅在线程突然终止时调用此方法（请参阅processWorkerExit）。
     *  其他减量在getTask中执行。
     */
    private void decrementWorkerCount() {
        do {} while (! compareAndDecrementWorkerCount(ctl.get()));
    }

    /**
     * 用于保留任务并移交给工作线程的队列。
     * 我们不需要返回null的workQueue.poll（）必然意味着workQueue.isEmpty（），
     * 因此仅依靠isEmpty来查看队列是否为空（例如，在确定是否从*过渡时，我们必须*这样做）关闭并整理）。
     * 这可容纳特殊用途的队列，例如DelayQueues，允许poll（）返回null，即使延迟延迟到期后它可能随后返回non-null。
     */
    private final java.util.concurrent.blocking.queue.BlockingQueue<Runnable> workQueue;

    /**
     * 上锁工人进入工作场所并进行相关簿记。
     * 虽然我们可以使用某种并发集，但事实证明通常最好使用锁。
     * 原因之一是序列化了interruptIdleWorkers，从而避免了不必要的中断风暴，尤其是在关机期间。
     * 否则，退出线程将同时中断那些尚未中断的线程。
     * 它还简化了与统计数据有关的maximumPoolSize等的簿记工作。
     * 在关闭和shutdownNow时，我们还将mainLock保持不变，以确保在单独检查中断和实际中断的权限时，确保工作集是稳定的。
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * 集包含池中的所有工作线程。仅在保持mainLock时访问
     */
    private final HashSet<Worker> workers = new HashSet<Worker>();

    /**
     * 等待条件以支持awaitTermination
     */
    private final Condition termination = mainLock.newCondition();

    /**
     * 跟踪达到的最大池大小。仅在mainLock下访问
     */
    private int largestPoolSize;

    /**
     * 计数器完成的任务。仅在工作线程终止时更新。仅在mainLock下访问。
     */
    private long completedTaskCount;

    /*
     * 所有用户控制参数都声明为volatile，以便正在进行的操作基于最新的值，但不需要锁定，因为没有内部不变性依赖于它们与其他操作同步变化。
     */

    /**
     * 新线程的工厂。
     * 使用此工厂创建所有线程（通过addWorker方法）。
     * 所有调用者都必须做好准备，以使addWorker失败，这可能反映出系统或用户的策略限制了线程数。
     * 即使未将其视为错误，创建线程失败也可能导致新任务被拒绝或现有任务仍停留在队列中。
     *
     * 我们走得更远，甚至在遇到诸如OutOfMemoryError之类的错误时仍会保留池不变式，而在尝试创建线程时可能会抛出该错误。
     * 由于需要在Thread.start中分配本机堆栈，因此此类错误相当普遍，并且用户将希望执行清理池关闭来进行清理。
     * 可能有足够的内存供清理代码完成而不遇到另一个OutOfMemoryError。
     */
    private volatile ThreadFactory threadFactory;

    /**
     * 在执行饱和或关闭时调用处理程序
     */
    private volatile RejectedExecutionHandler handler;

    /**
     * 空闲线程等待工作的超时时间（以纳秒为单位）。
     * 当存在多个corePoolSize或allowCoreThreadTimeOut时，线程将使用此超时。否则，他们将永远等着新工作。
     */
    private volatile long keepAliveTime;

    /**
     * 如果为false（默认值），则即使处于空闲状态，核心线程也保持活动状态。
     * 如果为true，则核心线程使用keepAliveTime来超时等待工作。
     */
    private volatile boolean allowCoreThreadTimeOut;

    /**
     * 除非设置allowCoreThreadTimeOut，否则核心池大小是保持活动状态（并且不允许超时等）的最小工作线程数，在这种情况下，最小值为零。
     */
    private volatile int corePoolSize;

    /**
     * 最大池大小。请注意，实际最大值在内部受CAPACITY限制。
     */
    private volatile int maximumPoolSize;

    /**
     * 默认拒绝执行处理程序
     */
    private static final RejectedExecutionHandler defaultHandler =
        new AbortPolicy();

    /**
     * 调用shutdown和shutdownNow所需的权限。
     * 我们还要求（请参阅checkShutdownAccess）要求调用者实际中断工作集中的线程的权限
     * （由Thread.interrupt控制，后者依赖于* ThreadGroup.checkAccess，而后者又依赖于* SecurityManager.checkAccess）。
     * 仅当这些检查通过时，才会尝试关机。
     *
     * Thread.interrupt的所有实际调用（请参阅* interruptIdleWorkers和interruptWorkers）都将忽略*SecurityExceptions，
     * 这意味着尝试的中断将以静默方式失败。在关机的情况下，它们不应失*除非SecurityManager的策略不一致，有*允许访问线程，有时不允许访问。
     * 在这种情况下，无法真正中断线程可能会禁用或延迟完全终止。
     * interruptIdleWorkers的其他用法是建议性的，实际中断失败只会延迟*配置更改的响应，因此不会进行特殊处理。
     */
    private static final RuntimePermission shutdownPerm =
        new RuntimePermission("modifyThread");

    /* 执行终结器时要使用的上下文，或者为null。 */
    private final AccessControlContext acc;

    /**
     * Class Worker主要维护线程运行任务的中断控制状态，以及其他次要簿记。
     * 此类机会性地扩展了AbstractQueuedSynchronizer 以简化获取和释放围绕每个任务执行的锁。
     * 这可以防止旨在唤醒等待任务的工作线程而不是中断正在运行的任务的中断。
     * 我们实现一个简单的非重入互斥锁，而不是使用ReentrantLock，
     * 因为我们不希望辅助任务在调用诸如setCorePoolSize之类的池控制方法时能够重新获取该锁。
     * 另外，为了抑制直到线程真正开始运行任务之前的中断，我们将lock状态初始化为负值，并在启动时将其清除（在runWorker中）。
     */
    private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
        /**
         * 此类永远不会序列化，但是我们提供了serialVersionUID来禁止Javac警告。
         */
        private static final long serialVersionUID = 6138294804551838833L;

        /** 此工作程序正在其中运行的线程。如果工厂失败，则为null。 */
        final Thread thread;

        /** 要运行的初始任务。可能为null */
        Runnable firstTask;

        /** 每个线程完成任务计数器 */
        volatile long completedTasks;

        /**
         * 使用给定的第一个任务和线程工厂中的线程创建
         * @param firstTask 第一项任务（如果没有则为null）
         */
        Worker(Runnable firstTask) {
            setState(-1); // 禁止中断，直到runWorker
            this.firstTask = firstTask;

            //因为Worker实现了Runnable接口，可以将this作为参数传入
            this.thread = getThreadFactory().newThread(this);
        }

        /** 将主运行循环委托给外部runWorker */
        public void run() {
            runWorker(this);
        }

        // 锁方法
        // 值0代表解锁状态.
        // 值1代表锁定状态.

        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }

    /*
     * 设置控制状态的方法
     */

    /**
     * 将runState转换为给定目标，如果已经至少为给定目标，则将其保留
     *
     * @param targetState 所需的状态，SHUTDOWN或STOP（但不是TIDYING或TERMINATED-为此使用tryTerminate）
     */
    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }

    /**
     * 如果（关闭和池并且队列为空）或（停止和池为空），则转换为TERMINATED状态。
     * 如果有资格终止，但workerCount不为零，则中断空闲的worker以确保传播关闭信号。
     * 必须在可能导致终止的任何操作之后调用此方法，以减少关闭状态下的工作人员计数或从队列中删除任务。
     * 该方法是非私有的，以允许从ScheduledThreadPoolExecutor访问。
     */
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            if (isRunning(c) ||
                runStateAtLeast(c, TIDYING) ||
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            if (workerCountOf(c) != 0) { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // 否则重试失败的CAS
        }
    }

    /*
     * 控制对工作线程的中断的方法
     */

    /**
     * 如果有安全管理器，请确保调用者具有*权限，通常可以关闭线程（请参阅shutdownPerm）。
     * 如果通过，则另外确保允许调用者中断每个工作线程。
     * 即使Security Manager特别对待某些线程，即使第一次通过检查也可能不正确。
     */
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * 中断所有线程，即使处于活动状态也是如此。忽略SecurityExceptions（在这种情况下，某些线程可能会保持不中断）。
     */
    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断可能正在等待任务的线程（如表示未锁定），以便它们可以检查*终止或配置更改。
     * 忽略SecurityExceptions（在这种情况下，某些线程可能保持不间断）
     *
     * @param onlyOne 如果为true，则最多中断一名工人。
     *                仅当启用终止功能时*仅从tryTerminate调用，但仍有其他工作程序。
     *                在这种情况下，在所有线程当前正在等待的情况下，最多个正在等待的工作程序被中断以传播关闭信号。
     *                中断任意线程可确保自关机开始以来新到达的工作人员最终也将退出。
     *                为保证最终终止，始终总是仅中断一个空闲的工作程序，
     *                而shutdown（）会中断所有*的工作程序，以便多余的工作程序迅速退出，而不是等待散乱的任务完成，就足够了。
     */
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 避免记住布尔参数的含义，是interruptIdleWorkers的常见形式
     */
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    /*
     * 杂项实用程序，其中大多数还导出到 ScheduledThreadPoolExecutor
     */

    /**
     * 调用给定命令的拒绝执行处理程序。 受Packaged保护，供ScheduledThreadPoolExecutor使用。
     */
    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * 在调用shutdown的运行状态转换之后执行任何进一步的清理。
     * 这里没有操作，但是由ScheduledThreadPoolExecutor用于取消延迟的任务。
     */
    void onShutdown() {
    }

    /**
     * ScheduledThreadPoolExecutor需要进行状态检查，以在关机期间启用正在运行的任务
     *
     * @param shutdownOK 如果关闭，则应返回true
     */
    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(ctl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    /**
     * 通常使用rainTo将任务队列排入新列表。
     * 但是，如果队列是DelayQueue或其他类型的队列，而poll或drainTo可能无法删除某些元素，则会将它们逐个删除。
     */
    private List<Runnable> drainQueue() {
        java.util.concurrent.blocking.queue.BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

    /*
     * 工作人员创建，运行和清理的方法
     */

    /**
     * 检查是否可以针对当前线程池状态和给定的界限（核心或最大值）添加新的工作程序。
     * 如果是这样，则将调整工作人员计数，并在可能的情况下创建并启动一个新的工作人员，并将firstTask作为其第一个任务运行。
     * 如果池已停止或可以关闭，则此方法返回false。
     * 如果在询问时线程工厂无法创建线程，它也会返回false。
     * 如果线程创建失败（由于线程工厂返回null或由于异常（通常是Thread.start（）中的OutOfMemoryError）），我们将进行干净的回滚。
     *
     * @param firstTask 新线程应首先运行的任务（如果没有则为null）。
     *                  当初始线程的数量少于corePoolSize线程（在这种情况下，我们总是启动一个），
     *                  或队列已满时（在这种情况下，我们必须使用初始的第一个任务在execute（）方法中）创建工作程序，以绕过队列。绕过队列）。
     *                  最初，空闲线程通常是通过prestartCoreThread创建的，或者用于替换其他垂死的工作线程。
     *
     * @param core 如果为true，则使用corePoolSize作为绑定，否则maximumPoolSize。
     *             这里使用布尔值指示符而不是值，以确保在检查其他pool状态后读取新值
     *
     * @return 如果成功，则为true
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // 仅在必要时检查队列是否为空
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // 重读ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // 否则CAS由于workerCount更改而失败；重试内部循环
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            //创建一个新的Worker
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // 按住锁时重新检查。 如果ThreadFactory失败，或者在获得锁之前关闭，请退出。
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // 预检查t是否可启动
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    //执行任务的start()
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

    /**
     * 回滚工作线程创建。
     *      -从工作人员中删除工作人员（如果存在）
     *      -减少工作人员计数
     *      -如果此工作人员的存在阻止了终止工作，则重新检查是否终止工作
     */
    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null)
                workers.remove(w);
            decrementWorkerCount();
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 为垂死的工人进行清理和簿记。
     * 仅从工作线程调用。除非设置了completelyAbruptly，否则假定workerCount已被调整为帐户以退出。
     * 此方法从工作程序集中删除线程，并且如果它由于用户任务异常而退出，
     * 或者如果少于的corePoolSize工作程序正在运行或者队列非空但没有工作程序，则可能终止该池或替换工作程序。
     *
     * @param w the worker
     * @param completedAbruptly 如果工人由于用户异常而死亡
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }

        tryTerminate();

        int c = ctl.get();
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
            addWorker(null, false);
        }
    }

    /**
     * 根据当前配置设置执行阻塞或定时等待任务，或者如果此工作程序由于以下任何原因而必须退出，则返回null:
     * 1. 超过了maximumPoolSize工人（由于对setMaximumPoolSize的调用）
     * 2. 线程池是stop状态.
     * 3. 线程池已关闭，队列为空.
     * 4. 该工作程序等待任务超时，并且超时工作程序会终止（即* {@code allowCoreThreadTimeOut || workerCount> corePoolSize}）在定时等待之前和之后，
     *      以及队列是否为*非空，此工作程序不是池中的最后一个线程。
     *
     * @return 任务，如果工作人员必须退出，则返回null，在这种情况下workerCount递减
     */
    private Runnable getTask() {
        boolean timedOut = false; // 最后的poll（）是否超时?

        for (;;) {

            //获取ctl当前值
            int c = ctl.get();
            //判断线程池当前状态
            int rs = runStateOf(c);

            // 判断线程池知否关闭，并且线程池停止&&消息队列为空
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                //减少一个worker工作者
                decrementWorkerCount();
                return null;
            }

            //获取当前工作者的数量
            int wc = workerCountOf(c);

            // 如果允许核心线程空闲，或者 当前wc > corePoolSize；则timed=true
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            // 判断当前工作者的数量 > maximumPoolSize || (timed && timedOut) &&
            // wc > 1 || workQueue.isEmpty()
            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                // 减少一个worker工作者
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }

            try {
                //从消息队列中获取一个任务
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    /**
     * 主工作者运行循环。反复从队列中获取任务并*执行它们，同时解决许多问题：
     *
     * 1. 我们可以从一个初始任务开始，在这种情况下，我们不需要获得第一个任务。
     *      否则，只要pool正在运行，我们就会从getTask获得任务。
     *      如果返回null，则工作程序由于池状态或配置参数更改而退出。
     *      其他退出是由于外部代码中的异常引发而导致的，在这种情况下，completedAbruptly成立，这通常导致processWorkerExit替换此线程。
     *
     * 2. 在运行任何任务之前，将获取锁以防止任务执行期间其他池中断，然后确保除非线程池正在停止，否则此线程未设置其中断。
     *
     * 3. 每个任务运行之前都会调用beforeExecute，这可能会引发异常，在这种情况下，我们会导致线程死掉（破坏循环，completedAbruptly为true）而不处理任务。
     *
     * 4. 假设beforeExecute正常完成，我们将运行任务，收集其引发的任何异常以发送给afterExecute。
     *      我们分别处理RuntimeException，Error（规格均保证我们可以捕获）和任意Throwables。
     *      因为我们不能在Runnable.run中重新抛出Throwables，所以我们将它们包装在出路（到线程的UncaughtExceptionHandler）的Errors中。
     *      任何抛出的异常也*保守地导致线程死亡。
     *
     * 5. task.run完成后，我们调用afterExecute，这可能还会引发异常，这也会导致线程死亡。
     *      根据JLS Sec 14.20，此异常是*即使task.run抛出也将有效的异常。
     *
     * 异常机制的最终结果是afterExecute和线程的UncaughtExceptionHandler具有与用户代码有关的尽可能准确的信息。
     *
     * @param w the worker
     */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // 允许中断
        boolean completedAbruptly = true;
        try {
            //如果task不为空，并且从队列中获取任务不为空
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // 如果池正在停止，请确保线程被中断；
                // 如果没有，请确保线程不被中断。
                // 这需要第二种情况下的重新检查以处理shutdownNow竞赛，同时清除中断
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

    // 公共构造函数和方法

    /**
     * 使用给定的初始参数以及默认的线程工厂和拒绝的执行处理程序创建一个新的{@code ThreadPoolExecutor}。
     * 使用{@link Executors}工厂*方法之一代替此通用构造函数可能更方便。
     *
     * @param corePoolSize 保留在池中的线程数，即使处于空闲状态，除非设置了{@code allowCoreThreadTimeOut}
     * @param maximumPoolSize 池中允许的最大线程数
     * @param keepAliveTime 当线程数大于内核数时，这是多余的空闲线程在终止之前等待新任务的最长时间。
     * @param unit {@code keepAliveTime}参数的时间单位
     * @param workQueue 在执行任务之前用于保留任务的队列。此队列将仅保存由{@code execute}方法提交的{@code Runnable}任务。
     * @throws IllegalArgumentException 如果满足以下条件之一：<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              java.util.concurrent.blocking.queue.BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }

    /**
     * 使用给定的初始参数和默认的拒绝执行处理程序创建一个新的{@code ThreadPoolExecutor}。
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              java.util.concurrent.blocking.queue.BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             threadFactory, defaultHandler);
    }

    /**
     * 使用给定的初始参数和默认的拒绝执行处理程序创建一个新的{@code ThreadPoolExecutor}。
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              java.util.concurrent.blocking.queue.BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), handler);
    }

    /**
     * 使用给定的初始参数创建一个新的{@code ThreadPoolExecutor}。
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              java.util.concurrent.blocking.queue.BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ?
                null :
                AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * 在将来的某个时间执行给定的任务。任务可以在新线程或现有池线程中执行
     *
     * 如果由于该执行程序已关闭或已达到其容量而无法提交执行任务，则该任务由当前的{@code RejectedExecutionHandler}处理。
     *
     * @param command 要执行的任务
     * @throws java.util.concurrent.exception.RejectedExecutionException 如果任务无法接受执行，则由{@code RejectedExecutionHandler}自行决定
     * @throws NullPointerException if {@code command} is null
     */
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        /*
         * 进行3个步骤:
         * 1. 如果正在运行的线程少于corePoolSize线程，请尝试使用给定命令作为其第一个任务来启动一个新线程。
         *      对addWorker的调用从原子上检查runState和workerCount，从而通过返回false来防止在不应该添加线程的情况下发出错误警报。
         *
         * 2. 如果任务可以成功排队，那么我们仍然需要再次检查是否应该添加线程（因为现有线程自上次检查后就死掉了），
         *      或者自进入此方法以来该池已关闭。因此，我们重新检查状态，并在必要时回滚排队，如果停止，或者如果没有线程，则启动一个新线程。
         *
         * 3. 如果我们无法将任务排队，那么我们尝试添加一个新的线程。如果失败，我们知道我们已关闭或处于饱和状态，因此拒绝该任务
         */
        int c = ctl.get();

        //当前线程数<corePoolSize
        if (workerCountOf(c) < corePoolSize) {
            // 添加一个worker对该任务进行处理
            // core为true，标明添加一个核心线程
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }

        //判断当前线程池是否还在运行中，并且任务加入队列是否成功
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            //如果当前线程池是否还在运行中，并且从队列中移除任务成功
            if (! isRunning(recheck) && remove(command))
                //拒绝执行该任务
                reject(command);

            //否则添加一个非核心线程执行该任务
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        //如果队列已经满了，则执行拒绝策略
        else if (!addWorker(command, false))
            reject(command);
    }

    /**
     * 启动有序关闭，在该关闭中执行先前提交的任务，但不接受任何新任务。
     * 如果调用已经关闭，则调用不会产生任何其他影响。
     *
     * 该方法不等待先前提交的任务完成。使用{@link #awaitTermination awaitTermination} 来做到这一点。
     *
     * @throws SecurityException {@inheritDoc}
     */
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        //获取锁
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(SHUTDOWN);
            interruptIdleWorkers();
            onShutdown(); // 挂接ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    /**
     * 尝试停止所有正在执行的任务，暂停正在等待的任务的处理，并返回正在等待执行的任务的列表。
     * 从此方法返回后，这些任务将从任务队列中耗尽（删除）。
     *
     * <p>该方法不等待活动执行的任务终止。使用{@link #awaitTermination awaitTermination}进行。
     *
     * <p>除了尽最大努力阻止停止处理正在执行的任务之外，没有任何保证。
     *      此实现通过{@link Thread＃interrupt}取消任务，因此任何未能响应中断的任务都可能永远不会终止。
     *
     * @throws SecurityException {@inheritDoc}
     */
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    //返回判断线程池是否shutDown
    public boolean isShutdown() {
        return ! isRunning(ctl.get());
    }

    /**
     * 如果此执行程序正在{@link #shutdown}或{@link #shutdownNow}之后终止，但尚未完全终止，则返回true。
     * 该方法可能对调试有用。
     * 在关闭后有足够的时间返回{@code true}可能表明提交的任务已被忽略或抑制了中断，从而导致该执行程序无法正确终止。
     *
     * @return {@code true}（如果终止但尚未终止）
     */
    public boolean isTerminating() {
        int c = ctl.get();
        return ! isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runStateAtLeast(ctl.get(), TERMINATED))
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 当该执行程序不再被引用且没有线程时，调用{@code shutdown}。
     */
    protected void finalize() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null || acc == null) {
            shutdown();
        } else {
            PrivilegedAction<Void> pa = () -> { shutdown(); return null; };
            AccessController.doPrivileged(pa, acc);
        }
    }

    /**
     * 设置用于创建新线程的线程工厂。
     *
     * @param threadFactory 新线程工厂
     * @throws NullPointerException 如果threadFactory为null
     * @see #getThreadFactory
     */
    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null)
            throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    /**
     * 返回用于创建新线程的线程工厂
     *
     * @return 当前的线程工厂
     * @see #setThreadFactory(ThreadFactory)
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * 为无法执行的任务设置新的处理程序：拒绝策略
     *
     * @param handler the new handler
     * @throws NullPointerException 如果处理程序为null
     * @see #getRejectedExecutionHandler
     */
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null)
            throw new NullPointerException();
        this.handler = handler;
    }

    /**
     * 返回当前可执行程序的处理程序
     *
     * @return the current handler
     * @see #setRejectedExecutionHandler(RejectedExecutionHandler)
     */
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    /**
     * 设置核心线程数。
     * 这将覆盖构造函数中所有设置为corePoolSize的值。
     * 如果新值小于当前值，则多余的现有线程将在下次变为空闲时终止。
     * 如果更大，则将在需要时启动新线程以执行所有排队的任务。
     *
     * @param corePoolSize 新的核心线程数大小
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @see #getCorePoolSize
     */
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0)
            throw new IllegalArgumentException();
        //记录新核心线程数 与 旧核心线程数 的差值
        int delta = corePoolSize - this.corePoolSize;

        //将核心线程数 设置为 当前核心线程数
        this.corePoolSize = corePoolSize;

        if (workerCountOf(ctl.get()) > corePoolSize)
            interruptIdleWorkers();
        else if (delta > 0) {
            // 我们真的不知道“需要”多少个新线程。
            // 作为一种试探法，请预先启动足够的新工作线程（最多达到新的//核心大小）以处理队列中当前的任务数量，但是如果在此期间队列变空则停止。
            int k = Math.min(delta, workQueue.size());
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty())
                    break;
            }
        }
    }

    /**
     * 返回线程的核心数量
     *
     * @return 核心线程数
     * @see #setCorePoolSize
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * 启动一个核心线程，使其闲置地等待工作。
     * 仅在执行新任务时才覆盖启动核心线程的默认策略。
     * 如果所有核心线程均已启动，则此方法将返回{@code false}。
     *
     * @return 如果线程已启动则返回{@code true}
     */
    public boolean prestartCoreThread() {
        return workerCountOf(ctl.get()) < corePoolSize &&
            addWorker(null, true);
    }

    /**
     * 与prestartCoreThread相同，除了安排即使corePoolSize为0至少启动一个线程
     */
    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize)
            addWorker(null, true);
        else if (wc == 0)
            addWorker(null, false);
    }

    /**
     * 启动所有核心线程，使它们空闲地等待工作。
     * 仅在执行新任务时才覆盖启动核心线程的默认策略
     *
     * @return the number of threads started
     */
    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, true))
            ++n;
        return n;
    }

    /**
     * 如果此池允许核心线程超时，则返回true；如果在keepAlive时间内没有任务到达，则终止；
     * 如果新任务到达，则在需要时替换。
     * 如果为true，则适用于非核心线程的相同保持活动策略也适用于核心线程。
     * 如果为false（默认值），则由于缺少传入任务，核心线程永远不会终止。
     *
     * @return {@code true}（如果允许核心线程超时），否则为{@code false}
     * @since 1.6
     */
    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    /**
     * 设置策略，以控制核心线程是否可能超时并在保持活动时间内没有任务到达时终止；在新任务到达时根据需要替换。
     * 如果为false，则由于缺少传入任务，因此核心线程永远不会终止。
     * 如果为true，则适用于*非核心线程的相同的保持活动策略也适用于核心线程。
     * 为了避免连续替换线程，设置{@code true}时，保持活动时间必须大于零。通常应在主动使用池之前调用此方法。
     *
     * @param value {@code true}如果应该超时，否则{@code false}
     * @throws IllegalArgumentException 如果值是{@code true}并且当前的保持活动时间不大于零
     *
     * @since 1.6
     */
    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0)
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value)
                interruptIdleWorkers();
        }
    }

    /**
     * 设置允许的最大线程数。
     * 这将覆盖构造函数中设置的任何值。
     * 如果新值小于当前值，则多余的现有线程将在下次空闲时终止。
     *
     * @param maximumPoolSize 新的最大值
     * @throws IllegalArgumentException 如果新的最大值是小于或等于零，或者小于{@linkplain #getCorePoolSize()}核心池大小
     * @see #getMaximumPoolSize
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
            throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(ctl.get()) > maximumPoolSize)
            interruptIdleWorkers();
    }

    /**
     * 返回允许的最大线程数
     *
     * @return 允许的最大线程数
     * @see #setMaximumPoolSize
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * 设置线程在终止*之前可能保持空闲的时间限制。
     * 如果池中当前有n个线程以上的核心数量，则在等待这段时间而不处理任务之后，多余的线程将被终止。
     * 这将覆盖构造函数中设置的任何值。
     *
     * @param time 等待的时间。时间值为零将导致多余的线程在执行任务后立即终止
     * @param unit {@code time}参数的时间单位
     * @throws IllegalArgumentException if {@code time} less than zero or
     *         if {@code time} is zero and {@code allowsCoreThreadTimeOut}
     * @see #getKeepAliveTime(TimeUnit)
     */
    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0)
            throw new IllegalArgumentException();
        if (time == 0 && allowsCoreThreadTimeOut())
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0)
            interruptIdleWorkers();
    }

    /**
     * 返回线程保持活动时间，该时间为终止核心之前超出核心池大小的线程可能保持*空闲的时间。
     *
     * @param unit the desired time unit of the result
     * @return the time limit
     * @see #setKeepAliveTime(long, TimeUnit)
     */
    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    /* 用户级队列实用程序 */

    /**
     * 返回此执行程序使用的任务队列。
     * 对任务队列的访问主要用于调试和监视。 此队列可能正在使用中。检索任务队列不会阻止排队的任务执行。
     *
     * @return the task queue
     */
    public java.util.concurrent.blocking.queue.BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * 如果执行器存在该任务，则将其从执行器的内部队列中移除，如果尚未启动，则导致该任务无法运行
     *
     * <p>此方法作为取消计划的一部分可能很有用。
     *  在放入内部队列之前，它可能无法删除已转换为其他形式的任务。
     *  例如，使用{@code Submit}输入的任务可能会*转换为保持{@code Future}状态的表单。
     *  但是，在这种情况下，方法{@link #purge}可用于删除那些已被取消的期货。
     *
     * @param task 删除任务
     * @return {@code true} if the task was removed
     */
    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // 如果SHUTDOWN且现在为空
        return removed;
    }

    /**
     * 尝试从工作队列中删除所有已取消的{@link java.util.concurrent.future.Future} 任务。
     * 此方法可用作*存储回收操作，对功能没有其他影响。
     * 被取消的任务永远不会执行，但是可能*累积在工作队列中，直到工作线程可以主动*删除它们为止。
     * 而是调用此方法尝试立即将其删除。
     * 但是，在存在其他线程干扰的情况下，此方法可能无法删除任务。
     */
    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof java.util.concurrent.future.Future<?> && ((java.util.concurrent.future.Future<?>)r).isCancelled())
                    it.remove();
            }
        } catch (ConcurrentModificationException fallThrough) {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray())
                if (r instanceof java.util.concurrent.future.Future<?> && ((java.util.concurrent.future.Future<?>)r).isCancelled())
                    q.remove(r);
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }

    /* 统计 */

    /**
     * 返回池中的当前线程数
     *
     * @return 线程数
     */
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 消除isTerminated（）&& getPoolSize（）> 0的罕见可能性
            return runStateAtLeast(ctl.get(), TIDYING) ? 0
                : workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回正在主动执行任务的线程数量。
     *
     * @return the number of threads
     */
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers)
                if (w.isLocked())
                    ++n;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回同时存在于池中的最大线程数
     *
     * @return the number of threads
     */
    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回计划执行的任务总数。
     * 由于任务和线程的状态可能在计算过程中动态变化，因此返回的值只是一个近似值。
     *
     * @return the number of tasks
     */
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked())
                    ++n;
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回已完成个任务的大概总数。
     * 由于任务和线程的状态在计算过程中可能会动态变化，因此返回值仅是一个近似值，而在连续调用中不会降低。
     *
     * @return the number of tasks
     */
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)
                n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 返回一个标识该池及其状态的字符串，包括运行状态和估计的工作程序以及任务计数的指示
     *
     * @return a string identifying this pool, as well as its state
     */
    public String toString() {
        long ncompleted;
        int nworkers, nactive;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            ncompleted = completedTaskCount;
            nactive = 0;
            nworkers = workers.size();
            for (Worker w : workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked())
                    ++nactive;
            }
        } finally {
            mainLock.unlock();
        }
        int c = ctl.get();
        String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" :
                     (runStateAtLeast(c, TERMINATED) ? "Terminated" :
                      "Shutting down"));
        return super.toString() +
            "[" + rs +
            ", pool size = " + nworkers +
            ", active threads = " + nactive +
            ", queued tasks = " + workQueue.size() +
            ", completed tasks = " + ncompleted +
            "]";
    }

    /* Extension hooks */

    /**
     * 在给定线程中执行给定Runnable之前调用的方法。
     * 线程{@code t}调用该方法，该线程将执行任务{@code r}​​，并且可用于重新初始化ThreadLocals或执行日志记录。
     *
     * <p>该实现不执行任何操作，但可以在*子类中进行自定义。
     *      注意：为了正确地嵌套多个重写，子类通常应在此方法的末尾调用{@code super.beforeExecute}。
     *
     * @param t 将运行任务{@code r}​​的线程
     * @param r 将要执行的任务
     */
    protected void beforeExecute(Thread t, Runnable r) { }

    /**
     * 给定Runnable执行完成时调用的方法。
     * 此方法由执行任务的线程调用。
     * 如果非空，则Throwable是导致执行突然终止的未捕获{@code RuntimeException} *或{@code Error}。
     *
     * <p>此实现不执行任何操作，但可以在子类中进行自定义。
     * 注意：为了正确嵌套多个重写，子类通常应在此方法的开头调用{@code super.afterExecute}。
     *
     * <p>如果将动作明确或通过诸如{{@code Submit}之类的方法包含在任务（例如* {@link FutureTask}）中，则这些任务对象会捕获并维护计算异常，因此不会导致突然终止，
     * 并且内部异常是not传递给此方法的。
     * 如果您想使用此方法捕获两种失败，则可以进一步针对此类情况进行探测，例如在此示例子类中，该子类将打印直接原因或如果任务已中止则显示基础异常：
     *
     *  <pre> {@code
     * class ExtendedExecutor extends ThreadPoolExecutor {
     *   // ...
     *   protected void afterExecute(Runnable r, Throwable t) {
     *     super.afterExecute(r, t);
     *     if (t == null && r instanceof Future<?>) {
     *       try {
     *         Object result = ((Future<?>) r).get();
     *       } catch (CancellationException ce) {
     *           t = ce;
     *       } catch (ExecutionException ee) {
     *           t = ee.getCause();
     *       } catch (InterruptedException ie) {
     *           Thread.currentThread().interrupt(); // ignore/reset
     *       }
     *     }
     *     if (t != null)
     *       System.out.println(t);
     *   }
     * }}</pre>
     *
     * @param r 已完成的运行
     * @param t 导致终止的异常；如果*执行正常完成，则返回null
     */
    protected void afterExecute(Runnable r, Throwable t) { }

    /**
     * 执行程序终止时调用的方法。
     * 默认实现不执行任何操作。注意：要正确嵌套多个覆盖，子类通常应在此方法内调用* {@code super.terminated}。
     */
    protected void terminated() { }

    /* 预定义的RejectedExecutionHandlers */

    /**
     * 处理被拒绝任务的处理程序，可以直接在{@code execute}方法的调用线程中运行被拒绝任务，除非执行器已关闭，在这种情况下，该任务将被丢弃。
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        /**
         * 创建一个{@code CallerRunsPolicy}
         */
        public CallerRunsPolicy() { }

        /**
         * 除非执行器已关闭，否则在调用者线程中执行任务r，在这种情况下，该任务将被丢弃。
         *
         * @param r 请求执行的可运行任务
         * @param e 试图执行此任务的执行者
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    /**
     * 抛出{@code RejectedExecutionException}的拒绝任务处理程序
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        /**
         * 创建一个{@code AbortPolicy}。
         */
        public AbortPolicy() { }

        /**
         * 始终抛出RejectedExecutionException
         *
         * @param r 请求执行的可运行任务
         * @param e 试图执行此任务的执行者
         * @throws java.util.concurrent.exception.RejectedExecutionException always
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }
    }

    /**
     * 拒绝任务的处理程序，静默丢弃拒绝任务。
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardPolicy}.
         */
        public DiscardPolicy() { }

        /**
         * 不执行任何操作，这具有丢弃任务r的作用
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    /**
     * 处理被拒绝任务的处理程序，它丢弃最旧的未处理请求，然后重试{@code execute}，除非执行器被关闭，在这种情况下，该任务将被丢弃。
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        /**
         * 为给定的执行者创建一个{@code DiscardOldestPolicy}
         */
        public DiscardOldestPolicy() { }

        /**
         * 获取并忽略执行者会立即执行的下一个任务（如果立即可用），
         * 然后重试任务r的执行，除非执行者被关闭，在这种情况下，任务r会被丢弃.
         *
         * @param r 请求执行的可运行任务
         * @param e 试图执行此任务的执行者
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                //将消息队列中的下一个即将执行的下一个任务忽略
                e.getQueue().poll();
                //直接执行当前任务
                e.execute(r);
            }
        }
    }
}
