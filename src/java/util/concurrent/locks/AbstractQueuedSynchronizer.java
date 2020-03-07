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
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import sun.misc.Unsafe;

/**
 * 提供一个框架来实现阻塞锁和相关的同步器(信号量、事件等)，这些同步器依赖于先进先出(FIFO)等待队列。
 * 这个类被设计成是大多数类型的同步器的有用基础，这些同步器依赖于单个原子{@code int}值来表示状态。
 * 子类必须定义改变这个状态的受保护的方法，而哪个定义了这个状态对于被获取或被释放的对象意味着什么。
 * 考虑到这些，这个类中的其他方法执行所有的排队和阻塞机制。
 * 子类可以维护其他状态字段，但是只有使用{@link #getState}、{@link #setState}和{@link #compareAndSetState}
 * 方法自动更新的{@code int}值才会被跟踪到同步。
 *
 * <p>子类应该被定义为非公共的内部助手类，用于实现其封闭类的同步属性。
 * 类{@code AbstractQueuedSynchronizer}不实现任何同步接口。
 * 相反，它定义了像{@link #acquireInterruptibly}这样的方法，
 * 这些方法可以被具体的锁和相关的同步器调用来实现它们的公共方法。
 *
 * <p>这个类支持默认的独占的模式和共享的模式。
 *      当以独占模式获取时，试图由其他线程获取的操作无法成功。
 *      共享模式由多个线程获得可能(但不一定)成功。
 * 这个类不“理解”除了在机械意义上的差异外，当共享模式获取成功时，
 * 下一个等待的线程(如果存在的话)也必须确定它是否也可以获取。
 * 在不同模式下等待的线程共享相同的FIFO队列。
 * 通常，实现子类只支持其中一种模式，但是这两种模式都可以发挥作用，例如在{@link ReadWriteLock}中。
 * 仅支持独占或仅支持共享模式的子类不需要定义支持未使用模式的方法。
 *
 * <p>This class defines a nested {@link ConditionObject} class that
 * can be used as a {@link Condition} implementation by subclasses
 * supporting exclusive mode for which method {@link
 * #isHeldExclusively} reports whether synchronization is exclusively
 * held with respect to the current thread, method {@link #release}
 * invoked with the current {@link #getState} value fully releases
 * this object, and {@link #acquire}, given this saved state value,
 * eventually restores this object to its previous acquired state.  No
 * {@code AbstractQueuedSynchronizer} method otherwise creates such a
 * condition, so if this constraint cannot be met, do not use it.  The
 * behavior of {@link ConditionObject} depends of course on the
 * semantics of its synchronizer implementation.
 *
 * <p>该类为内部队列提供检查、插装和监视方法，以及条件对象的类似方法。
 * 可以根据需要使用{@code AbstractQueuedSynchronizer}将它们导出到类中，用于它们的同步机制。
 *
 * <p>这个类的序列化只存储底层的原子整数维护状态，所以反序列化的对象有空线程队列。
 * 需要可串行化的典型子类将定义一个{@code readObject}方法，该方法在反串行化时将此恢复为已知的初始状态。
 *
 * <h3>Usage</h3>
 *
 * <p>要使用这个类作为同步器的基础，
 *  通过使用{@link #getState}、{@link #setState}和{@link #compareAndSetState}检查或修改同步状态，重新定义以下方法(如适用):
 * <ul>
 * <li> {@link #tryAcquire}
 * <li> {@link #tryRelease}
 * <li> {@link #tryAcquireShared}
 * <li> {@link #tryReleaseShared}
 * <li> {@link #isHeldExclusively}
 * </ul>
 *
 * 默认情况下，这些方法中的每一个都会抛出{@link UnsupportedOperationException}。
 * 这些方法的实现必须在内部是线程安全的，并且通常应该是简短的而不是阻塞的。
 * 定义这些方法是只支持使用这个类的方法。
 * 所有其他方法都声明为{@code final}，因为它们不能独立地更改。
 *
 * <p>您可能还会发现继承自{@link AbstractOwnableSynchronizer}的方法对于跟踪拥有独占同步器的线程非常有用。
 *  建议您使用它们——这使得监视和诊断工具能够帮助用户确定哪些线程持有锁
 *
 * <p>即使这个类基于一个内部FIFO队列，它也不会自动执行FIFO获取策略。互斥同步的核心采用以下形式:
 *
 * <pre>
 * Acquire:
 *     while (!tryAcquire(arg)) {
 *        <em>如果线程还没有排队，则对其进行排队</em>;
 *        <em>可能阻塞当前线程</em>;
 *     }
 *
 * Release:
 *     if (tryRelease(arg))
 *        <em>取消第一个排队线程的阻塞</em>;
 * </pre>
 *
 * (共享模式类似，但可能涉及级联信号.)
 *
 * <p id="barging">因为签入获取是在排队之前调用的，
 * 一个新获取的线程可能会比其他被阻塞和排队的线程先驳船。
 * 但是，如果需要，您可以定义{@code tryAcquire}或{@code tryacquiremred}来通过内部调用一个或多个检查方法来禁用barging，
 * 从而提供一个fairFIFO获取顺序。
 * 特别是，如果{@link # hasqueuedformer}(一个专门为公平同步器使用的方法)返回{@code true}，
 * 大多数公平同步器可以定义{@code tryAcquire} 返回{@code false}。
 * 其他的变化是可能的。
 *
 * <p>吞吐量和可伸缩性通常在缺省barging(也称为贪心， 放弃， convoyavoidance)策略中最高。
 *  虽然不能保证这是公平的或无饥饿的，但允许较早的队列线程在较晚的队列线程之前进行重新争用，
 *  并且每个重新争用都有无偏的机会对传入线程成功。同样，当获得的时候不要“旋转”在通常意义上，
 *  它们可以执行多次调用{@code tryAcquire}，并在阻塞之前穿插其他计算。
 *  当独占性同步只短暂持有时，这就提供了spin的大部分好处，而当它不持有时，则没有大部分的负债。
 *  如果需要，您可以*通过前面的调用来获取带有“快速路径”检查的方法，
 *  可能需要预先检查{@link #hasContended}或{@link #hasQueuedThreads}，
 *  只有在同步器可能不存在竞争时才这样做。
 *
 * <p>这个类为同步提供了一个有效的和可扩展的基础，部分通过专门化它的使用范围到同步器，
 * 这些同步器可以依赖于{@code int}状态、获取和释放参数，以及一个内部FIFO等待队列。
 * 当这还不够时，您可以使用{@link java.util.concurrent 从较低的层次构建同步器。原子原子}类，
 * 您自己的自定义{@link java.util.Queue}类和{@link LockSupport}阻塞支持
 *
 * <h3>Usage Examples</h3>
 *
 * <p>这是一个不可重入的互斥锁类，它使用值0表示解锁状态，1表示锁状态。
 * 虽然不可重入锁并不严格要求记录当前所有者线程，但是这个类这样做是为了更容易监视使用情况。
 * 它还支持条件和暴露一种仪器方法:
 *
 *  <pre> {@code
 * class Mutex implements Lock, java.io.Serializable {
 *
 *   // Our internal helper class
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     // 报告是否处于锁定状态
 *     protected boolean isHeldExclusively() {
 *       return getState() == 1;
 *     }
 *
 *     // 如果状态为零，则获取锁
 *     public boolean tryAcquire(int acquires) {
 *       assert acquires == 1; // 否则未使用
 *       if (compareAndSetState(0, 1)) {
 *         setExclusiveOwnerThread(Thread.currentThread());
 *         return true;
 *       }
 *       return false;
 *     }
 *
 *     // 通过将状态设置为0来释放锁
 *     protected boolean tryRelease(int releases) {
 *       assert releases == 1; // Otherwise unused
 *       if (getState() == 0) throw new IllegalMonitorStateException();
 *       setExclusiveOwnerThread(null);
 *       setState(0);
 *       return true;
 *     }
 *
 *     // 提供了条件
 *     Condition newCondition() { return new ConditionObject(); }
 *
 *     // Deserializes properly
 *     private void readObject(ObjectInputStream s)
 *         throws IOException, ClassNotFoundException {
 *       s.defaultReadObject();
 *       setState(0); // reset to unlocked state
 *     }
 *   }
 *
 *   // 同步对象完成所有的工作。我们只是期待它
 *   private final Sync sync = new Sync();
 *
 *   public void lock()                { sync.acquire(1); }
 *   public boolean tryLock()          { return sync.tryAcquire(1); }
 *   public void unlock()              { sync.release(1); }
 *   public Condition newCondition()   { return sync.newCondition(); }
 *   public boolean isLocked()         { return sync.isHeldExclusively(); }
 *   public boolean hasQueuedThreads() { return sync.hasQueuedThreads(); }
 *   public void lockInterruptibly() throws InterruptedException {
 *     sync.acquireInterruptibly(1);
 *   }
 *   public boolean tryLock(long timeout, TimeUnit unit)
 *       throws InterruptedException {
 *     return sync.tryAcquireNanos(1, unit.toNanos(timeout));
 *   }
 * }}</pre>
 *
 * <p>这是一个类似于{@link java.util.concurrent 的锁存器类。它只需要一个{@code信号}来触发。
 *  因为锁存器是非排他的，所以它使用{@code shared} *获取和释放方法。
 *
 *  <pre> {@code
 * class BooleanLatch {
 *
 *   private static class Sync extends AbstractQueuedSynchronizer {
 *     boolean isSignalled() { return getState() != 0; }
 *
 *     protected int tryAcquireShared(int ignore) {
 *       return isSignalled() ? 1 : -1;
 *     }
 *
 *     protected boolean tryReleaseShared(int ignore) {
 *       setState(1);
 *       return true;
 *     }
 *   }
 *
 *   private final Sync sync = new Sync();
 *   public boolean isSignalled() { return sync.isSignalled(); }
 *   public void signal()         { sync.releaseShared(1); }
 *   public void await() throws InterruptedException {
 *     sync.acquireSharedInterruptibly(1);
 *   }
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    private static final long serialVersionUID = 7373984972572414691L;
    /**
     * 创建一个初始同步状态为0的新{@code AbstractQueuedSynchronizer}实例
     */
    protected AbstractQueuedSynchronizer() { }

    /**
     * 等待队列节点类。
     *
     * <p>等待队列是“CLH”(Craig、Landin和Hagersten)锁队列的变体。
     * CLH锁通常用于自旋锁。
     * 相反，我们使用它们来阻塞同步器，但是使用相同的基本策略，
     * 即在其节点的前身中保留关于线程的一些控制信息。
     * 每个节点中的“status”字段跟踪线程是否应该阻塞。
     * 一个节点在它的前任释放时被通知。
     * 否则，队列的每个节点都充当一个持有单个等待线程的通知样式的监视器。
     * 状态字段并不控制线程是否被授予锁等。如果一个线程是队列中的第一个，它可能会尝试获取。
     * 但是成为第一并不能保证成功;它只给你竞争的权利。因此，当前发布的竞争者线程可能需要重新等待。
     *
     * <p>要加入到CLH锁中，您需要原子性地将其作为新尾拼接进来。要退出队列，只需设置head字段。
     * <pre>
     *      +------+  prev +-----+       +-----+
     * head |      | <---- |     | <---- |     |  tail
     *      +------+       +-----+       +-----+
     * </pre>
     *
     * <p>插入CLH队列只需要在“tail”上执行一个单原子操作，因此有一个简单的原子点从未排队到排队。
     * 类似地，退出队列*只涉及更新“head”。
     * 然而，节点需要做更多的工作来确定谁是他们的继任者，部分原因是为了处理超时和中断可能导致的取消。
     *
     * <p>“prev”链接(在原来的CLH锁中没有使用)主要用于处理取消。
     * 如果一个节点被取消，它的后继节点(通常)会重新链接到一个未取消的前辈节点。
     * 有关自旋锁的类似机制的解释，请参阅Scott和Scherer的论文，
     * 网址为* http://www.cs.rochester.edu/u/scott/synchronization/
     *
     * <p>我们还使用“next”链接来实现阻塞机制。
     * 每个节点的线程id保存在它自己的节点中，因此前辈通过遍历next链接来通知下一个节点唤醒，
     * 以确定它是哪个线程。确定后继必须避免使用新加入队列的节点来设置其前辈的“下一个”字段。
     * 在必要时，当节点的后续节点为空时，从原子性更新的“tail”向后检查，可以解决这个问题。
     * (换句话说，下一个链接是优化，因此我们通常不需要反向扫描。)
     *
     * <p>对消为基本*算法引入了一些保守性。
     * 因为我们必须轮询其他节点的取消，所以我们可能会忽略被取消的节点是在前面还是在后面。
     * 解决这一问题的方法是，总是在继任者被取消时取消他们的职位，这样他们就可以稳定地拥有一个新的前任，
     * 除非我们能找到一个未被取消的前任来承担这一责任。
     *
     * <p>CLH 队列需要一个虚构的头节点来启动。
     * 但是，我们不会在构建时创建它们，因为如果从来没有争用，就会浪费精力。
     * 相反，将构造节点，并在第一个争用时设置head和tail指针。
     *
     * <p>等待条件的线程使用相同的节点，但是使用额外的链接。
     *      条件只需要在简单(非并发)链接队列中链接节点，因为它们只在独占时被访问。
     *      在等待时，节点被插入到条件队列中。
     *      收到信号后，节点被转移到主队列。status字段的特殊值用于标记节点所在的队列。
     *
     * <p>感谢Dave Dice、Mark Moir、Victor Luchangco、Bill Scherer和Michael Scott，以及JSR-166专家组的成员，他们对这个类的设计提供了有用的想法、讨论和批评。
     */
    static final class Node {
        /** 指示节点在共享模式下等待的标记 */
        static final Node SHARED = new Node();
        /** 指示节点正在排他模式中等待的标记 */
        static final Node EXCLUSIVE = null;

        /** 表示线程已取消的等待状态值 */
        static final int CANCELLED =  1;
        /** 等待状态值，指示后续线程需要取消停靠 */
        static final int SIGNAL    = -1;
        /** 表示线程处于等待状态*/
        static final int CONDITION = -2;
        /**
         * 表示下一个默认值的waitStatus值应该无条件传播
         */
        static final int PROPAGATE = -3;

        /**
         * 状态字段，只接受值:
         *   SIGNAL:     此节点的后续节点被(或即将被)阻塞(通过park)，
         *               因此当前节点在释放或取消时必须取消其后续节点。
         *               为了避免竞争，获取方法必须首先表明它们需要一个信号，
         *               然后重试原子获取，如果失败，阻塞。
         *
         *   CANCELLED:  由于超时或中断，此节点被取消。节点永远不会离开这个状态。
         *               特别是，一个取消节点的线程永远不会再阻塞。
         *
         *   CONDITION:  此节点当前位于条件队列上。
         *               在传输之前，它不会被用作同步队列节点，此时状态将被设置为0。
         *               (这里使用这个值与字段的其他用法无关，但是简化了机制。)
         *
         *   PROPAGATE:  一个被释放的节点应该被传播到其他节点。
         *               这是在doReleaseShared中设置的(仅针对head节点)，以确保传播继续，
         *               即使其他操作已经进行了干预。
         *
         *   0:          以上皆非
         *
         * 值以数字形式排列以简化使用。非负值表示节点不需要信号。
         * 因此，大多数代码不需要检查特定的值，只需检查符号。
         *
         * 对于正常的同步节点，字段初始化为0，对于条件节点，字段初始化为CONDITION。
         * 可以使用CAS(或者在可能的情况下，使用无条件的volatile写)修改它
         */
        volatile int waitStatus;

        /**
         * 链接到当前节点/线程所依赖的前任节点来检查等待状态。
         * 在排队时分配，在退出排队时为空(为了GC)。
         * 此外，在取消一个前辈时，我们在查找一个未取消的前辈时发生短路，该未取消的前辈将始终存在，
         * 因为head节点从未被取消:一个节点只有在成功获取后才成为head。
         * 一个cancel的线程永远不会成功获取，并且一个线程只cancel自身，不取消任何其他节点。
         */
        volatile Node prev;

        /**
         * 链接到当前节点/线程*在发布时退出的后续节点。
         * 在排队时分配，绕过取消的前一个时调整，离开排队时为空(为了GC)。
         * enq操作直到附件之后才分配前任的下一个字段，
         * 所以看到一个空的下一个字段并不一定意味着node在队列的末尾。
         * 但是，如果下一个字段出现为空，我们可以从尾部扫描prev到重复检查。
         * 取消节点的下一个字段被设置为指向节点本身，而不是null，以简化isOnSyncQueue的工作。
         */
        volatile Node next;

        /**
         * 加入此节点的线程。初始化结构和无效后使用。
         */
        volatile Thread thread;

        /**
         * 链接到下一个处于等待状态的节点，或共享的特殊值。
         * 因为条件队列在独占模式下仅被访问，所以我们只需要一个简单的链接队列来在节点等待条件时保持节点。
         * 然后它们被转移到队列中重新获取。
         * 由于条件只能是排他的，我们通过使用特殊值来表示共享模式来保存字段。
         */
        Node nextWaiter;

        /**
         * 如果节点在共享模式下等待，则返回true。
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 返回前一个节点，如果为空则抛出NullPointerException。
         * 当前任不能为空时使用。可以省略null检查，但它是用来帮助VM的。
         *
         * @return the predecessor of this node
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // 用于建立初始标头或共享标头
        }

        Node(Thread thread, Node mode) {     // 使用addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // 使用的条件
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    /**
     * 等待队列的头，延迟初始化。
     * 除了初始化外，它只通过setHead方法进行修改。
     * 注意:如果head存在，则保证不会取消它的等待状态。
     */
    private transient volatile Node head;

    /**
     * 等待队列的尾部，延迟初始化。仅通过方法enq()修改以添加新的等待节点
     */
    private transient volatile Node tail;

    /**
     * 同步状态:state
     */
    private volatile int state;

    /**
     * 返回同步状态的当前值。该操作的内存语义为{@code volatile} read。
     * @return current state value
     */
    protected final int getState() {
        return state;
    }

    /**
     * 设置同步状态的值。该操作具有{@code volatile}写的内存语义。
     * @param newState the new state value
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * 如果当前状态值等于期望值，则自动将同步状态设置为给定的已更新值。
     * 此操作具有{@code volatile}读和写的内存语义。
     *
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} 如果成功。False返回表示实际的值不等于期望值。
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // 请参阅下面的intrinsics设置来支持这一点
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    // 排队公用参数

    /**
     * 比使用定时停车快的纳秒数。粗略的估计就足以提高对非常短的超时的响应能力。
     */
    static final long spinForTimeoutThreshold = 1000L;

    /**
     * 将节点插入队列，必要时进行初始化。见上图。
     * @param node 要插入的节点
     * @return 节点的前任
     */
    private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            /**
             * 当循环第一次时，tail=null，则t=null，
             *    完成aqs队列的初始化，将头尾节点都指向一个Thread=null的Node节点
             */
            if (t == null) { // 必须初始化
                // 如果t=null时，则初始化一个Thread=null的Node节点，并赋值为head头节点
                if (compareAndSetHead(new Node()))
                    // 将尾节点tail也指向同一个node节点
                    tail = head;
            } else {
                /**
                 * 当循环第二次时，tail !=null，则t!=null
                 *  a、将当前入队的node的上一个节点指向t，即指向head头节点
                 *  b、compareAndSetTail(t, node)：判断当前尾节点是否是t，如果是则将尾节点指向node
                 *  c、将t节点的下一个节点指向node，即head头节点的next指向node。完成双向链表。
                 */
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * 为当前线程和给定模式创建和排队节点
     * 第一个节点入队时：
     *      a、tail=null，即pred=null，直接调用enq()方法
     *
     * 第二个节点入队时：
     *      a、tail!=null,即pred!=null,先是改变头尾节点
     *      b、维护第二个节点与第一个节点的双向关系
     *      c、将tail尾节点指向第二个节点
     *
     * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
     * @return the new node
     */
    private Node addWaiter(Node mode) {
        //使用当前线程构建Node节点
        Node node = new Node(Thread.currentThread(), mode);

        // 将tail队尾节点复制给pred
        Node pred = tail;

        // 判断pred是否为空，即判断该队列中是否有节点在排队
        if (pred != null) {
            // a、如果pred不为空，说明队列不为空，则将当前node的pred->pred节点
            node.prev = pred;
            // c、判断尾节点是否是pred，如果是，则将尾节点指向node
            if (compareAndSetTail(pred, node)) {
                // b、将pred的next节点指向当前node节点，完成双向链表的维护
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }

    /**
     * 将队列的头部设置为节点，从而退出队列。
     * 仅通过获取方法调用。为了GC和抑制不必要的信号和遍历，还会为空出未使用的字段。
     *
     * @param node the node
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**
     * 唤醒node的后继节点(如果存在的话)。
     *
     * @param node the node
     */
    private void unparkSuccessor(Node node) {
        /*
         * 如果状态为负(即(可能需要信号)试清除预期信号。
         * 如果这个失败或者状态被等待线程改变了，这是可以的。
         */
        int ws = node.waitStatus;
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0);

        /*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         */
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }

    /**
     * 释放共享模式的动作——信号后继并确保传播。
     * (注:对于独占模式，释放相当于调用unpark继任人的头，如果它需要信号。)
     */
    private void doReleaseShared() {
        /*
         * Ensure that a release propagates, even if there are other
         * in-progress acquires/releases.  This proceeds in the usual
         * way of trying to unparkSuccessor of head if it needs
         * signal. But if it does not, status is set to PROPAGATE to
         * ensure that upon release, propagation continues.
         * Additionally, we must loop in case a new node is added
         * while we are doing this. Also, unlike other uses of
         * unparkSuccessor, we need to know if CAS to reset status
         * fails, if so rechecking.
         */
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }

    /**
     * 设置队列的头部，并检查后续队列是否可能在共享模式下等待，
     * 如果是这样，如果传播> 0或*传播状态已设置。
     *
     * @param node the node
     * @param propagate the return value from a tryAcquireShared
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);
        /*
         * Try to signal next queued node if:
         *   Propagation was indicated by caller,
         *     or was recorded (as h.waitStatus either before
         *     or after setHead) by a previous operation
         *     (note: this uses sign-check of waitStatus because
         *      PROPAGATE status may transition to SIGNAL.)
         * and
         *   The next node is waiting in shared mode,
         *     or we don't know, because it appears null
         *
         * The conservatism in both of these checks may cause
         * unnecessary wake-ups, but only when there are multiple
         * racing acquires/releases, so most need signals now or soon
         * anyway.
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    // Utilities for various versions of acquire

    /**
     * 取消正在进行的获取尝试.
     *
     * @param node the node
     */
    private void cancelAcquire(Node node) {
        // 如果node不存在，请忽略它
        if (node == null)
            return;

        node.thread = null;

        // 跳过取消前任
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        Node predNext = pred.next;

        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        node.waitStatus = Node.CANCELLED;

        // If we are the tail, remove ourselves.
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if (pred != head &&
                ((ws = pred.waitStatus) == Node.SIGNAL ||
                 (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }

    /**
     * 检查和更新未能获取的节点的状态。
     * 如果线程阻塞，返回true。
     * 这是所有获取循环中的主信号控制。需要pred == node.prev。
     *
     * @param pred 节点的前任保持状态
     * @param node the node
     * @return {@code true} if thread should block
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        // 获取当前pred节点的线程状态
        int ws = pred.waitStatus;
        /**
         * 第一次时，pred为头节点，ws=0
         */
        if (ws == Node.SIGNAL)
            /*
             * 这个节点已经设置了请求释放的状态来通知它，这样它就可以安全地停车了。
             */
            return true;
        if (ws > 0) {
            /*
             * 前任被取消了。跳过前一项并表示重试。
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * 等待状态必须为0或传播。
             * 告诉他们我们需要信号，但不要停车。来电者将需要*重试，以确保它不能获得之前停车。
             * 将pred的状态置为-1
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * 方便的方法中断当前线程
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * 方便方法停车后再检查是否中断
     *
     * @return {@code true} 如果中断返回true
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }

    /*
     * Various flavors of acquire, varying in exclusive/shared and
     * control modes.  Each is mostly the same, but annoyingly
     * different.  Only a little bit of factoring is possible due to
     * interactions of exception mechanics (including ensuring that we
     * cancel if tryAcquire throws exception) and other control, at
     * least not without hurting performance too much.
     */

    /**
     * 对于已经在队列中的线程，以独占的不可中断模式获取。用于条件等待方法以及获取。
     *
     * @param node the node
     * @param arg the acquire argument
     * @return {@code true} if interrupted while waiting
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            /**
             * 这个for死循环就是自旋锁的体现，先是尝试去获取锁，获取失败才会将线程进行阻塞
             */
            for (;;) {
                // 获取节点的上一个节点
                final Node p = node.predecessor();
                /**
                 * 此时先自旋一次
                 *   判断p是否为头节点
                 *      如果是，说明该线程是第一个进行排队的节点，为了效率，自旋去获取锁
                 *      如果不是，说明队列中至少还有其他线程在排队，为了效率，不需要进行自旋
                 */
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                /**
                 * 如果不是头节点或者自旋失败，则将该Thread进行阻塞
                 *   shouldParkAfterFailedAcquire():将上一个node节点的状态设置为-1，并返回true
                 *   parkAndCheckInterrupt():将该线程进行park阻塞
                 */
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以互斥中断模式获取
     * @param arg the acquire argument
     */
    private void doAcquireInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以独占时间模式获取
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以共享不可中断模式获取.
     * @param arg the acquire argument
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以共享中断模式获取.
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg)
        throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 以共享时间模式获取.
     *
     * @param arg the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                    nanosTimeout > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    // 主要出口的方法

    /**
     * 尝试以独占模式获取。
     * 这个方法应该查询如果对象的状态允许它在exclusive模式下被获取，如果允许，则获取它。
     *
     * <p>执行acquire的线程总是调用这个方法。
     * 如果此方法报告失败，则获取方法可能会对线程进行排队(如果它还没有排队)，
     * 直到其他线程的某个释放发出信号。
     * 这可以用来实现方法{@link Lock#tryLock()}。
     *
     * <p>默认的实现抛出{@link UnsupportedOperationException}.
     *
     * @param arg 获取参数。
     *        这个值总是传递给获取方法的，或者是保存到条件wait的值。该值是未解释的，可以表示您喜欢的任何内容。
     * @return {@code true} if successful. Upon success, this object has
     *         been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 尝试将状态设置为以独占模式反映发布
     *
     * <p>执行release的线程总是调用此方法.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this object is now in a fully released
     *         state, so that any waiting threads may attempt to acquire;
     *         and {@code false} otherwise.
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 尝试以共享模式获取。
     * 该方法应该查询对象的状态是否允许以共享模式获取它，如果允许，则应该获取它。
     *
     * <p>执行acquire的线程总是调用这个方法。
     * 如果此方法报告失败，则获取方法可能会对线程进行排队(如果它还没有排队)，直到其他线程的某个释放发出信号。
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return a negative value on failure; zero if acquisition in shared
     *         mode succeeded but no subsequent shared-mode acquire can
     *         succeed; and a positive value if acquisition in shared
     *         mode succeeded and subsequent shared-mode acquires might
     *         also succeed, in which case a subsequent waiting thread
     *         must check availability. (Support for three different
     *         return values enables this method to be used in contexts
     *         where acquires only sometimes act exclusively.)  Upon
     *         success, this object has been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to set the state to reflect a release in shared mode.
     *
     * <p>This method is always invoked by the thread performing release.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param arg the release argument. This value is always the one
     *        passed to a release method, or the current state value upon
     *        entry to a condition wait.  The value is otherwise
     *        uninterpreted and can represent anything you like.
     * @return {@code true} if this release of shared mode may permit a
     *         waiting acquire (shared or exclusive) to succeed; and
     *         {@code false} otherwise
     * @throws IllegalMonitorStateException if releasing would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if shared mode is not supported
     */
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * 如果同步仅针对当前(调用)线程*，则返回{@code true}。
     * 此方法在每次调用非等待的{@link ConditionObject}方法时调用。
     * (调用{@link #release}代替等待方法。)
     *
     * <p>The default implementation throws {@link
     * UnsupportedOperationException}. This method is invoked
     * internally only within {@link ConditionObject} methods, so need
     * not be defined if conditions are not used.
     *
     * @return {@code true} if synchronization is held exclusively;
     *         {@code false} otherwise
     * @throws UnsupportedOperationException if conditions are not supported
     */
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * 以独占模式获取，忽略中断。
     * 通过至少一次调用{@link #tryAcquire}实现，成功返回。
     * 否则线程将排队，可能会重复阻塞和取消阻塞，调用{@link #tryAcquire}直到成功。
     * 这个方法可以用来实现方法{@link Lock# Lock}。
     *
     * @param arg 获取参数。
     *        这个值被传递给{@link #tryAcquire}，但是没有被解释，可以代表你喜欢的任何东西。
     */
    public final void acquire(int arg) {
        /**
         * tryAcquire()和acquireQueued()是互斥的
         *      如果tryAcquire()==true，加!后为false，则不执行acquireQueued()
         *      如果tryAcquire()==false，加!后为true，则执行acquireQueued()
         *
         *  addWaiter()用于向队列中添加线程进行等待
         *  acquireQueued()用于将线程park，阻塞线程
         */
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

    /**
     * 以独占模式获取，如果中断则中止。
     * 首先检查中断状态，然后至少调用一次{@link #tryAcquire}，在成功时返回。
     * 否则线程将排队，可能会重复阻塞和取消阻塞，调用{@link #tryAcquire}，直到成功或线程被中断。
     * 这个方法可以用来实现方法{@link Lock#lockInterruptibly}。
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    /**
     * Attempts to acquire in exclusive mode, aborting if interrupted,
     * and failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquire}, returning on success.  Otherwise, the thread is
     * queued, possibly repeatedly blocking and unblocking, invoking
     * {@link #tryAcquire} until success or the thread is interrupted
     * or the timeout elapses.  This method can be used to implement
     * method {@link Lock#tryLock(long, TimeUnit)}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }

    /**
     * 以独占模式发布。
     * 如果{@link #tryRelease}返回true，则通过取消阻塞一个或多个线程来实现。
     * 此方法可用于实现方法{@link Lock#unlock}。
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryRelease} but is otherwise uninterpreted and
     *        can represent anything you like.
     * @return the value returned from {@link #tryRelease}
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    /**
     * Acquires in shared mode, ignoring interrupts.  Implemented by
     * first invoking at least once {@link #tryAcquireShared},
     * returning on success.  Otherwise the thread is queued, possibly
     * repeatedly blocking and unblocking, invoking {@link
     * #tryAcquireShared} until success.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     */
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    /**
     * Acquires in shared mode, aborting if interrupted.  Implemented
     * by first checking interrupt status, then invoking at least once
     * {@link #tryAcquireShared}, returning on success.  Otherwise the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted.
     * @param arg the acquire argument.
     * This value is conveyed to {@link #tryAcquireShared} but is
     * otherwise uninterpreted and can represent anything
     * you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (tryAcquireShared(arg) < 0)
            doAcquireSharedInterruptibly(arg);
    }

    /**
     * Attempts to acquire in shared mode, aborting if interrupted, and
     * failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquireShared}, returning on success.  Otherwise, the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted or the timeout elapses.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquireShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
            doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**
     * Releases in shared mode.  Implemented by unblocking one or more
     * threads if {@link #tryReleaseShared} returns true.
     *
     * @param arg the release argument.  This value is conveyed to
     *        {@link #tryReleaseShared} but is otherwise uninterpreted
     *        and can represent anything you like.
     * @return the value returned from {@link #tryReleaseShared}
     */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    // 队列检验方法

    /**
     * 查询是否有线程正在等待获取。
     * 注意，由于中断和超时导致的取消可能会在任何时候发生，
     * {@code true}的返回并不保证任何其他线程会获得。
     *
     * <p>在这个实现中，该操作以常量时间返回
     *
     * @return {@code true} if there may be other threads waiting to acquire
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * 查询是否有线程争用过这个同步器;
     * 也就是说，如果一个获取方法曾经被阻塞。
     *
     * <p>在这个实现中，该操作以常量时间返回
     *
     * @return {@code true} if there has ever been contention
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * 返回队列中的第一个(等待时间最长的)线程，如果当前没有线程排队，则返回{@code null}。
     *
     * <p>In this implementation, this operation normally returns in
     * constant time, but may iterate upon contention if other threads are
     * concurrently modifying the queue.
     *
     * @return the first (longest-waiting) thread in the queue, or
     *         {@code null} if no threads are currently queued
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * Version of getFirstQueuedThread called when fastpath fails
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null) ||
            ((h = head) != null && (s = h.next) != null &&
             s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         */

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }

    /**
     * 如果给定线程当前正在排队，则返回true。
     *
     * <p>此实现遍历队列以确定给定线程是否存在。
     *
     * @param thread the thread
     * @return {@code true} if the given thread is on the queue
     * @throws NullPointerException if the thread is null
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }

    /**
     * 返回{@code true}如果第一个明显的排队线程(如果存在一个)正在排它模式中等待。
     * 如果这个方法返回{@code true}，并且当前线程正在尝试以shared模式获取
     * (也就是说，这个方法是从{@link # tryacquirered}调用的)，
     * 那么可以保证当前线程不是第一个排队的线程。只能在ReentrantReadWriteLock中作为启发使用。
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
            (s = h.next)  != null &&
            !s.isShared()         &&
            s.thread != null;
    }

    /**
     * 查询是否有任何线程等待获取的时间比当前线程长。
     *
     * <p>此方法的调用相当于(但可能比):
     *  <pre> {@code
     * getFirstQueuedThread() != Thread.currentThread() &&
     * hasQueuedThreads()}</pre>
     *
     * <p>注意，由于中断和超时导致的取消可能随时发生，所以{@code true}返回不能保证其他线程在当前线程之前获得。
     * 同样，在此方法返回{@code false}后，由于队列为空，另一个线程可能会赢得race to enqueue。
     *
     * <p>此方法被设计用于一个公平的同步器，
     * 以避免<a href="AbstractQueuedSynchronizer#barging">barging</a>。
     * 这样一个同步器的{@link #tryAcquire}方法应该返回{@code false}，
     * 而它的{@link # tryacquired}方法应该返回一个负值，
     * 如果这个方法返回{@code true}(除非这是一个重入获取)。
     * 例如，{@code tryAcquire}方法为一个公平的，可重入的，排他的模式同步器可能看起来像这样:
     *
     *  <pre> {@code
     * protected boolean tryAcquire(int arg) {
     *   if (isHeldExclusively()) {
     *     // A reentrant acquire; increment hold count
     *     return true;
     *   } else if (hasQueuedPredecessors()) {
     *     return false;
     *   } else {
     *     // try to acquire normally
     *   }
     * }}</pre>
     *
     * @return {@code true}如果在当前线程之前有一个排队的线程，
     *         {@code false}如果当前线程在队列的最前面或者队列是空的
     * @since 1.7
     */
    public final boolean hasQueuedPredecessors() {
        // 这个的正确性取决于头的初始化
        // 尾朝上，头朝上。下一个是准确的如果电流
        // 线程是队列中的第一个
        Node t = tail; // 按反初始化顺序读取字段
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }


    // 仪器和监测方法

    /**
     * 返回等待获取的线程数量的估计值。
     * 这个值只是一个估计值，因为当这个方法遍历内部数据结构时，线程的数量可能会动态变化。
     * 此方法用于监控系统状态，不用于同步控制。
     *
     * @return the estimated number of threads waiting to acquire
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive monitoring facilities.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    /**
     * 返回一个集合，其中包含可能在独占模式下等待获取的线程。
     * 它具有与{@link #getQueuedThreads}相同的属性，只是它只返回那些由于独占获取而等待的线程。
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in shared mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to a shared acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * 返回标识此同步器及其状态的字符串。
     * 方括号中的状态包括字符串{@code "state ="}，后面跟着{@link #getState}的当前值，
     * 以及{@code "nonempty"}或{@code "empty"}，这取决于队列是否为空。
     *
     * @return a string identifying this synchronizer, as well as its state
     */
    public String toString() {
        int s = getState();
        String q  = hasQueuedThreads() ? "non" : "";
        return super.toString() +
            "[State = " + s + ", " + q + "empty queue]";
    }


    // 条件的内部支持方法

    /**
     * 如果一个节点(始终是最初放置在条件队列中的节点)现在正等待在同步队列上重新获取，则返回true。
     * @param node the node
     * @return true if is reacquiring
     */
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
        if (node.next != null) // If has successor, it must be on queue
            return true;
        /*
         * node.prev can be non-null, but not yet on queue because
         * the CAS to place it on queue can fail. So we have to
         * traverse from tail to make sure it actually made it.  It
         * will always be near the tail in calls to this method, and
         * unless the CAS failed (which is unlikely), it will be
         * there, so we hardly ever traverse much.
         */
        return findNodeFromTail(node);
    }

    /**
     * 如果节点在同步队列上，则通过从尾部向后搜索返回true。
     * 仅在isOnSyncQueue需要时调用。
     * @return true if present
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**
     * Transfers a node from a condition queue onto sync queue.
     * Returns true if successful.
     * @param node the node
     * @return true if successfully transferred (else the node was
     * cancelled before signal)
     */
    final boolean transferForSignal(Node node) {
        /*
         * If cannot change waitStatus, the node has been cancelled.
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         */
        Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }

    /**
     * Transfers node, if necessary, to sync queue after a cancelled wait.
     * Returns true if thread was cancelled before being signalled.
     *
     * @param node the node
     * @return true if cancelled before the node was signalled
     */
    final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        /*
         * If we lost out to a signal(), then we can't proceed
         * until it finishes its enq().  Cancelling during an
         * incomplete transfer is both rare and transient, so just
         * spin.
         */
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    /**
     * Invokes release with current state value; returns saved state.
     * Cancels node and throws exception on failure.
     * @param node the condition node for this wait
     * @return previous sync state
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    // 条件测量方法

    /**
     * Queries whether the given ConditionObject
     * uses this synchronizer as its lock.
     *
     * @param condition the condition
     * @return {@code true} if owned
     * @throws NullPointerException if the condition is null
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with this synchronizer. Note that because timeouts
     * and interrupts may occur at any time, a {@code true} return
     * does not guarantee that a future {@code signal} will awaken
     * any threads.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with this synchronizer. Note that
     * because timeouts and interrupts may occur at any time, the
     * estimate serves only as an upper bound on the actual number of
     * waiters.  This method is designed for use in monitoring of the
     * system state, not for synchronization control.
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with this
     * synchronizer.  Because the actual set of threads may change
     * dynamically while constructing this result, the returned
     * collection is only a best-effort estimate. The elements of the
     * returned collection are in no particular order.
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if exclusive synchronization
     *         is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this synchronizer
     * @throws NullPointerException if the condition is null
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /**
     * 作为{@link  Lock}实现基础的{@link * AbstractQueuedSynchronizer}的条件实现。
     *
     * <p>从锁和条件用户的角度来看，该类的方法文档描述的是机制，而不是行为规范。
     * 这个类的导出版本通常需要附带描述条件语义的文档，这些语义依赖于相关的{@code AbstractQueuedSynchronizer}。
     * <p>这个类是可序列化的，但是所有的字段都是暂时的，所以反序列化的条件没有等待者
     */
    public class ConditionObject implements Condition, java.io.Serializable {
        private static final long serialVersionUID = 1173984872572414699L;
        /** First node of condition queue. */
        private transient Node firstWaiter;
        /** Last node of condition queue. */
        private transient Node lastWaiter;

        /**
         * 创建一个新的{@code条件对象}实例
         */
        public ConditionObject() { }

        // Internal methods

        /**
         * Adds a new waiter to wait queue.
         * @return its new wait node
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // If lastWaiter is cancelled, clean out.
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**
         * Removes and transfers nodes until hit non-cancelled one or
         * null. Split out from signal in part to encourage compilers
         * to inline the case of no waiters.
         * @param first (non-null) the first node on condition queue
         */
        private void doSignal(Node first) {
            do {
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null);
        }

        /**
         * Removes and transfers all nodes.
         * @param first (non-null) the first node on condition queue
         */
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * Unlinks cancelled waiter nodes from condition queue.
         * Called only while holding lock. This is called when
         * cancellation occurred during condition wait, and upon
         * insertion of a new waiter when lastWaiter is seen to have
         * been cancelled. This method is needed to avoid garbage
         * retention in the absence of signals. So even though it may
         * require a full traversal, it comes into play only when
         * timeouts or cancellations occur in the absence of
         * signals. It traverses all nodes rather than stopping at a
         * particular target to unlink all pointers to garbage nodes
         * without requiring many re-traversals during cancellation
         * storms.
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }

        // public methods

        /**
         * Moves the longest-waiting thread, if one exists, from the
         * wait queue for this condition to the wait queue for the
         * owning lock.
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        public final void signal() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }

        /**
         * Moves all threads from the wait queue for this condition to
         * the wait queue for the owning lock.
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        /**
         * Implements uninterruptible condition wait.
         * <ol>
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * </ol>
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        /*
         * For interruptible waits, we need to track whether to throw
         * InterruptedException, if interrupted while blocked on
         * condition, versus reinterrupt current thread, if
         * interrupted while blocked waiting to re-acquire.
         */

        /** Mode meaning to reinterrupt on exit from wait */
        private static final int REINTERRUPT =  1;
        /** Mode meaning to throw InterruptedException on exit from wait */
        private static final int THROW_IE    = -1;

        /**
         * Checks for interrupt, returning THROW_IE if interrupted
         * before signalled, REINTERRUPT if after signalled, or
         * 0 if not interrupted.
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }

        /**
         * Throws InterruptedException, reinterrupts current thread, or
         * does nothing, depending on mode.
         */
        private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * Implements interruptible condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled or interrupted.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        /**
         * Implements absolute timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         *      throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         *      {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        //  support for instrumentation

        /**
         * Returns true if this condition was created by the given
         * synchronization object.
         *
         * @return {@code true} if owned
         */
        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         * Queries whether any threads are waiting on this condition.
         * Implements {@link AbstractQueuedSynchronizer#hasWaiters(ConditionObject)}.
         *
         * @return {@code true} if there are any waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        /**
         * Returns an estimate of the number of threads waiting on
         * this condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitQueueLength(ConditionObject)}.
         *
         * @return the estimated number of waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * Returns a collection containing those threads that may be
         * waiting on this Condition.
         * Implements {@link AbstractQueuedSynchronizer#getWaitingThreads(ConditionObject)}.
         *
         * @return the collection of threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *         returns {@code false}
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }

    /**
     * Setup to support compareAndSet. We need to natively implement
     * this here: For the sake of permitting future enhancements, we
     * cannot explicitly subclass AtomicInteger, which would be
     * efficient and useful otherwise. So, as the lesser of evils, we
     * natively implement using hotspot intrinsics API. And while we
     * are at it, we do the same for other CASable fields (which could
     * otherwise be done with atomic field updaters).
     */
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }

    /**
     * CAS head field. Used only by enq.
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS tail field. Used only by enq.
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    /**
     * CAS waitStatus field of a node.
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                                        expect, update);
    }

    /**
     * CAS next field of a node.
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
