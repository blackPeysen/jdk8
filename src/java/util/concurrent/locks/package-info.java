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
 * 接口和类提供了一个框架，用于锁定和等待与内置同步和监视器不同的条件。
 * 该框架在使用锁和条件时允许更大的灵活性，但以更笨拙的语法为代价。
 *
 * <p>{@link java.util.concurrent.locks.Lock}接口支持语义不同（可重入，公平等）的锁定规则，并且可以在非块结构上下文中使用，包括手和锁重新排序算法。
 * 主要实现是{@link java.util.concurrent.locks.ReentrantLock}。
 *
 * <p>{@link java.util.concurrent.locks.ReadWriteLock}接口类似地定义了可以在读者之间共享但独占作者的锁。
 * 仅提供一个实现{@link java.util.concurrent.locks.ReentrantReadWriteLock}，因为它涵盖了大多数标准用法上下文。
 * 但是程序员可以创建自己的实现来满足非标准要求。
 *
 * <p>{@link java.util.concurrent.locks.Condition}接口描述了可能与Locks关联的条件变量。
 * 这些用法类似于使用* {@code Object.wait}访问的隐式监视器，但是提供了扩展功能。
 * 特别是，多个{@code Condition}对象可以与单个{@code Lock}相关联。
 * 为避免兼容性问题，{@code Condition}方法的名称与相应的{@code Object}版本不同。
 *
 * <p>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer} 类是有用的超类，用于定义锁和依赖队列阻塞线程的其他同步器。
 * {@link java.util.concurrent.locks.AbstractQueuedLongSynchronizer}类提供了相同的功能，但扩展了对同步状态的64位的支持。
 * 两者都扩展了类{@link java.util.concurrent.locks.AbstractOwnableSynchronizer}，这是一个简单的类，有助于记录当前正在进行独占同步的线程。
 * {@link java.util.concurrent.locks.LockSupport} 类提供了较低级别的阻止和取消阻止支持，这对于实现自己的自定义lock类的开发人员很有用。
 *
 * @since 1.5
 */
package java.util.concurrent.locks;
