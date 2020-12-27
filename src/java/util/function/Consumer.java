/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.util.function;

import java.util.Objects;

/**
 * 表示接受单个输入参数且不返回结果的操作。
 * 与大多数其他功能接口不同，{@code Consumer}预期通过副作用进行操作。
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object)}.
 *
 * @param <T> 操作输入的类型
 *
 * @since 1.8
 */
@FunctionalInterface
public interface Consumer<T> {

    /**
     * 对给定参数执行此操作。
     *
     * @param t 输入参数
     */
    void accept(T t);

    /**
     * 返回一个组合的{@code 消费者}，该消费者依次执行这个操作和后面的{@code after}操作。
     * 如果执行任何一个操作引发异常，则将其转发给组合操作的调用者。
     * 如果执行此操作引发异常，则不会执行后面的{@code}操作。
     *
     * @param after 该操作完成后需要执行的操作
     * @return 组合的{@code 消费者}按顺序执行这个操作，然后再执行{@code after}操作
     * @throws NullPointerException if {@code after} is null
     */
    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
