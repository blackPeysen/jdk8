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

package java.util.concurrent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.AccessControlException;
import sun.security.util.SecurityConstants;

/**
 * 用于{@link Executor}、{@link  ExecutorService}、
 * {@link ScheduledExecutorService}、{@link  ThreadFactory}和{@link Callable}类的工厂和实用程序方法。
 * 这个类支持以下几种方法:
 *
 * <ul>
 *   <li> Methods that create and return an {@link ExecutorService}
 *        set up with commonly useful configuration settings.
 *   <li> Methods that create and return a {@link ScheduledExecutorService}
 *        set up with commonly useful configuration settings.
 *   <li> Methods that create and return a "wrapped" ExecutorService, that
 *        disables reconfiguration by making implementation-specific methods
 *        inaccessible.
 *   <li> Methods that create and return a {@link ThreadFactory}
 *        that sets newly created threads to a known state.
 *   <li> Methods that create and return a {@link Callable}
 *        out of other closure-like forms, so they can be used
 *        in execution methods requiring {@code Callable}.
 * </ul>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class Executors {

    /**
     * 创建一个线程池，该线程池重用固定数量的线程在一个共享的无界队列上运行。
     * 在任何时候，至多{@code nThreads}线程将是活动的处理任务。
     * 如果在所有线程都处于活动状态时提交额外的任务，它们将在队列中等待，直到有一个线程可用。
     * 如果任何线程在执行过程中由于失败而终止在关闭之前，如果需要执行后续任务，将会有一个新的线程代替它。
     * 池中的线程将一直存在，直到显式地出现{@link ExecutorService#shutdown shutdown}。
     *
     * @param nThreads the number of threads in the pool
     * @return the newly created thread pool
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 创建一个线程池，该线程池重用固定数量的线程在一个共享的无界队列上运行，
     * 在需要时使用提供的ThreadFactory创建新线程。
     * 在任何时候，At most {@code nThreads}线程将是活动处理任务。
     * 如果在所有线程都“活动”时提交额外的任务，它们将在队列中等待，直到某个线程“可用”为止。
     * 如果任何线程在关闭之前的执行过程中由于失败而终止，那么如果需要执行后续任务，
     * 则会有一个新的线程来替代它。
     * 池中的线程将存在，直到显式地{@link ExecutorService#shutdown shutdown}。
     *
     * @param nThreads the number of threads in the pool
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     */
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
    }

    /**
     * 创建一个线程池，该线程池维护足够的线程以支持给定的并行度级别，并且可以使用多个队列来减少争用。
     * 并行度级别对应于积极参与或可参与任务处理的最大线程数。线程的实际数量可能会动态地增加或减少。
     * 工作窃取池不能保证提交的任务执行的顺序。
     *
     * @param parallelism the targeted parallelism level
     * @return the newly created thread pool
     * @throws IllegalArgumentException if {@code parallelism <= 0}
     * @since 1.8
     */
    public static ExecutorService newWorkStealingPool(int parallelism) {
        return new ForkJoinPool
            (parallelism,
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }

    /**
     * 使用所有{@link Runtime#availableProcessors available processors}作为其目标并行度级别，创建一个工作窃取线程池。
     * @return the newly created thread pool
     * @see #newWorkStealingPool(int)
     * @since 1.8
     */
    public static ExecutorService newWorkStealingPool() {
        return new ForkJoinPool
            (Runtime.getRuntime().availableProcessors(),
             ForkJoinPool.defaultForkJoinWorkerThreadFactory,
             null, true);
    }



    /**
     * 创建一个执行程序，该执行程序使用一个工作线程操作一个无界队列。
     * (但是请注意，如果这个单线程在shutdown之前的执行过程中由于失败而终止，那么如果需要执行后续的任务，
     * 就会有一个新的线程来代替它。)任务保证按顺序执行，
     * 并且在任何给定时间内都不会有多个任务处于活动状态。
     * 与其他等价的{@code newFixedThreadPool(1)}不同，返回的执行器保证不可重新配置以使用其他线程。
     *
     * @return the newly created single-threaded Executor
     */
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }

    /**
     * 创建一个执行器，该执行器使用一个工作线程操作一个无界队列，并使用提供的ThreadFactory在需要时创建一个新线程。
     * 与其他等价的{@code newFixedThreadPool(1, threadFactory)}不同，返回的执行器保证不可重新配置以使用额外的线程。
     *
     * @param threadFactory 创建新线程时使用的工厂
     *
     * @return the newly created single-threaded Executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>(),
                                    threadFactory));
    }

    /**
     * 创建一个线程池，该线程池根据需要创建新线程，但是将在以前构造的线程可用时重用它们。
     * 这些池通常会提高执行许多短期异步任务的程序的性能。
     * 调用{@code execute}将重用以前构造的线程(如果可用)。
     * 如果没有可用的现有线程，将创建一个新的线程并将其添加到池中。
     * 已经60秒没有使用的线程将被终止并从缓存中删除。
     * 因此，长时间保持空闲的池不会消耗任何资源。
     * 注意，可以使用{@link ThreadPoolExecutor}构造函数创建具有类似属性但不同细节(例如超时参数)的池。
     *
     * @return the newly created thread pool
     */
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }

    /**
     * 创建一个线程池，该线程池根据需要创建新线程，但是将在以前构造的线程可用时重用它们，并在需要时使用提供的ThreadFactory创建新线程。
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     * @throws NullPointerException if threadFactory is null
     */
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>(),
                                      threadFactory);
    }

    /**
     * 创建一个单线程执行器，该执行器可以调度命令在给定的延迟后运行或定期执行。
     * (但是请注意，如果这个单线程在shutdown之前的执行过程中由于失败而终止，那么如果需要执行后续的任务，就会有一个新的线程来代替它。)任务保证按顺序执行，
     * 并且在任何给定时间内都不会有多个任务处于活动状态。
     * 与其他等价的* {@code newScheduledThreadPool(1)}不同，返回的执行器*保证不可重新配置以使用其他线程。
     * @return the newly created scheduled executor
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1));
    }

    /**
     * Creates a single-threaded executor that can schedule commands
     * to run after a given delay, or to execute periodically.  (Note
     * however that if this single thread terminates due to a failure
     * during execution prior to shutdown, a new one will take its
     * place if needed to execute subsequent tasks.)  Tasks are
     * guaranteed to execute sequentially, and no more than one task
     * will be active at any given time. Unlike the otherwise
     * equivalent {@code newScheduledThreadPool(1, threadFactory)}
     * the returned executor is guaranteed not to be reconfigurable to
     * use additional threads.
     * @param threadFactory the factory to use when creating new
     * threads
     * @return a newly created scheduled executor
     * @throws NullPointerException if threadFactory is null
     */
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1, threadFactory));
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param corePoolSize the number of threads to keep in the pool,
     * even if they are idle
     * @param threadFactory the factory to use when the executor
     * creates a new thread
     * @return a newly created scheduled thread pool
     * @throws IllegalArgumentException if {@code corePoolSize < 0}
     * @throws NullPointerException if threadFactory is null
     */
    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    /**
     * Returns an object that delegates all defined {@link
     * ExecutorService} methods to the given executor, but not any
     * other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * @param executor the underlying implementation
     * @return an {@code ExecutorService} instance
     * @throws NullPointerException if executor null
     */
    public static ExecutorService unconfigurableExecutorService(ExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedExecutorService(executor);
    }

    /**
     * Returns an object that delegates all defined {@link
     * ScheduledExecutorService} methods to the given executor, but
     * not any other methods that might otherwise be accessible using
     * casts. This provides a way to safely "freeze" configuration and
     * disallow tuning of a given concrete implementation.
     * @param executor the underlying implementation
     * @return a {@code ScheduledExecutorService} instance
     * @throws NullPointerException if executor null
     */
    public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledExecutorService executor) {
        if (executor == null)
            throw new NullPointerException();
        return new DelegatedScheduledExecutorService(executor);
    }

    /**
     * Returns a default thread factory used to create new threads.
     * This factory creates all new threads used by an Executor in the
     * same {@link ThreadGroup}. If there is a {@link
     * java.lang.SecurityManager}, it uses the group of {@link
     * System#getSecurityManager}, else the group of the thread
     * invoking this {@code defaultThreadFactory} method. Each new
     * thread is created as a non-daemon thread with priority set to
     * the smaller of {@code Thread.NORM_PRIORITY} and the maximum
     * priority permitted in the thread group.  New threads have names
     * accessible via {@link Thread#getName} of
     * <em>pool-N-thread-M</em>, where <em>N</em> is the sequence
     * number of this factory, and <em>M</em> is the sequence number
     * of the thread created by this factory.
     * @return a thread factory
     */
    public static ThreadFactory defaultThreadFactory() {
        return new DefaultThreadFactory();
    }

    /**
     * Returns a thread factory used to create new threads that
     * have the same permissions as the current thread.
     * This factory creates threads with the same settings as {@link
     * Executors#defaultThreadFactory}, additionally setting the
     * AccessControlContext and contextClassLoader of new threads to
     * be the same as the thread invoking this
     * {@code privilegedThreadFactory} method.  A new
     * {@code privilegedThreadFactory} can be created within an
     * {@link AccessController#doPrivileged AccessController.doPrivileged}
     * action setting the current thread's access control context to
     * create threads with the selected permission settings holding
     * within that action.
     *
     * <p>Note that while tasks running within such threads will have
     * the same access control and class loader settings as the
     * current thread, they need not have the same {@link
     * java.lang.ThreadLocal} or {@link
     * java.lang.InheritableThreadLocal} values. If necessary,
     * particular values of thread locals can be set or reset before
     * any task runs in {@link ThreadPoolExecutor} subclasses using
     * {@link ThreadPoolExecutor#beforeExecute(Thread, Runnable)}.
     * Also, if it is necessary to initialize worker threads to have
     * the same InheritableThreadLocal settings as some other
     * designated thread, you can create a custom ThreadFactory in
     * which that thread waits for and services requests to create
     * others that will inherit its values.
     *
     * @return a thread factory
     * @throws AccessControlException if the current access control
     * context does not have permission to both get and set context
     * class loader
     */
    public static ThreadFactory privilegedThreadFactory() {
        return new PrivilegedThreadFactory();
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given task and returns the given result.  This
     * can be useful when applying methods requiring a
     * {@code Callable} to an otherwise resultless action.
     * @param task the task to run
     * @param result the result to return
     * @param <T> the type of the result
     * @return a callable object
     * @throws NullPointerException if task null
     */
    public static <T> Callable<T> callable(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<T>(task, result);
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given task and returns {@code null}.
     * @param task the task to run
     * @return a callable object
     * @throws NullPointerException if task null
     */
    public static Callable<Object> callable(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        return new RunnableAdapter<Object>(task, null);
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given privileged action and returns its result.
     * @param action the privileged action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    public static Callable<Object> callable(final PrivilegedAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() { return action.run(); }};
    }

    /**
     * Returns a {@link Callable} object that, when
     * called, runs the given privileged exception action and returns
     * its result.
     * @param action the privileged exception action to run
     * @return a callable object
     * @throws NullPointerException if action null
     */
    public static Callable<Object> callable(final PrivilegedExceptionAction<?> action) {
        if (action == null)
            throw new NullPointerException();
        return new Callable<Object>() {
            public Object call() throws Exception { return action.run(); }};
    }

    /**
     * Returns a {@link Callable} object that will, when called,
     * execute the given {@code callable} under the current access
     * control context. This method should normally be invoked within
     * an {@link AccessController#doPrivileged AccessController.doPrivileged}
     * action to create callables that will, if possible, execute
     * under the selected permission settings holding within that
     * action; or if not possible, throw an associated {@link
     * AccessControlException}.
     * @param callable the underlying task
     * @param <T> the type of the callable's result
     * @return a callable object
     * @throws NullPointerException if callable null
     */
    public static <T> Callable<T> privilegedCallable(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallable<T>(callable);
    }

    /**
     * Returns a {@link Callable} object that will, when called,
     * execute the given {@code callable} under the current access
     * control context, with the current context class loader as the
     * context class loader. This method should normally be invoked
     * within an
     * {@link AccessController#doPrivileged AccessController.doPrivileged}
     * action to create callables that will, if possible, execute
     * under the selected permission settings holding within that
     * action; or if not possible, throw an associated {@link
     * AccessControlException}.
     *
     * @param callable the underlying task
     * @param <T> the type of the callable's result
     * @return a callable object
     * @throws NullPointerException if callable null
     * @throws AccessControlException if the current access control
     * context does not have permission to both set and get context
     * class loader
     */
    public static <T> Callable<T> privilegedCallableUsingCurrentClassLoader(Callable<T> callable) {
        if (callable == null)
            throw new NullPointerException();
        return new PrivilegedCallableUsingCurrentClassLoader<T>(callable);
    }

    // Non-public classes supporting the public methods

    /**
     * A callable that runs given task and returns given result
     */
    static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;
        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }
        public T call() {
            task.run();
            return result;
        }
    }

    /**
     * A callable that runs under established access control settings
     */
    static final class PrivilegedCallable<T> implements Callable<T> {
        private final Callable<T> task;
        private final AccessControlContext acc;

        PrivilegedCallable(Callable<T> task) {
            this.task = task;
            this.acc = AccessController.getContext();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<T>() {
                        public T run() throws Exception {
                            return task.call();
                        }
                    }, acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    /**
     * A callable that runs under established access control settings and
     * current ClassLoader
     */
    static final class PrivilegedCallableUsingCurrentClassLoader<T> implements Callable<T> {
        private final Callable<T> task;
        private final AccessControlContext acc;
        private final ClassLoader ccl;

        PrivilegedCallableUsingCurrentClassLoader(Callable<T> task) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // Calls to getContextClassLoader from this class
                // never trigger a security check, but we check
                // whether our callers have this permission anyways.
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

                // Whether setContextClassLoader turns out to be necessary
                // or not, we fail fast if permission is not available.
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            this.task = task;
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<T>() {
                        public T run() throws Exception {
                            Thread t = Thread.currentThread();
                            ClassLoader cl = t.getContextClassLoader();
                            if (ccl == cl) {
                                return task.call();
                            } else {
                                t.setContextClassLoader(ccl);
                                try {
                                    return task.call();
                                } finally {
                                    t.setContextClassLoader(cl);
                                }
                            }
                        }
                    }, acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    /**
     * 默认的线程工厂
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * 捕获访问控制上下文和类装入器的线程工厂
     */
    static class PrivilegedThreadFactory extends DefaultThreadFactory {
        private final AccessControlContext acc;
        private final ClassLoader ccl;

        PrivilegedThreadFactory() {
            super();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                // Calls to getContextClassLoader from this class
                // never trigger a security check, but we check
                // whether our callers have this permission anyways.
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);

                // Fail fast
                sm.checkPermission(new RuntimePermission("setContextClassLoader"));
            }
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public Thread newThread(final Runnable r) {
            return super.newThread(new Runnable() {
                public void run() {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            Thread.currentThread().setContextClassLoader(ccl);
                            r.run();
                            return null;
                        }
                    }, acc);
                }
            });
        }
    }

    /**
     * 只公开ExecutorService实现的ExecutorService方法的包装器类
     */
    static class DelegatedExecutorService extends AbstractExecutorService {
        private final ExecutorService e;
        DelegatedExecutorService(ExecutorService executor) { e = executor; }
        public void execute(Runnable command) { e.execute(command); }
        public void shutdown() { e.shutdown(); }
        public List<Runnable> shutdownNow() { return e.shutdownNow(); }
        public boolean isShutdown() { return e.isShutdown(); }
        public boolean isTerminated() { return e.isTerminated(); }
        public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
            return e.awaitTermination(timeout, unit);
        }
        public Future<?> submit(Runnable task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Callable<T> task) {
            return e.submit(task);
        }
        public <T> Future<T> submit(Runnable task, T result) {
            return e.submit(task, result);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
            return e.invokeAll(tasks);
        }
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                             long timeout, TimeUnit unit)
            throws InterruptedException {
            return e.invokeAll(tasks, timeout, unit);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
            return e.invokeAny(tasks);
        }
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                               long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return e.invokeAny(tasks, timeout, unit);
        }
    }

    static class FinalizableDelegatedExecutorService
        extends DelegatedExecutorService {
        FinalizableDelegatedExecutorService(ExecutorService executor) {
            super(executor);
        }
        protected void finalize() {
            super.shutdown();
        }
    }

    /**
     * A wrapper class that exposes only the ScheduledExecutorService
     * methods of a ScheduledExecutorService implementation.
     */
    static class DelegatedScheduledExecutorService
            extends DelegatedExecutorService
            implements ScheduledExecutorService {
        private final ScheduledExecutorService e;
        DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
            super(executor);
            e = executor;
        }
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return e.schedule(command, delay, unit);
        }
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return e.schedule(callable, delay, unit);
        }
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return e.scheduleAtFixedRate(command, initialDelay, period, unit);
        }
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }

    /** Cannot instantiate. */
    private Executors() {}
}
