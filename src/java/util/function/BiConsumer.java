/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 表示接受两个输入参数且不返回任何结果的操作。
 * 这是{@link Consumer}的两种特性专门化。
 * 与大多数其他功能接口不同，{@code BiConsumer}被期望通过副作用操作。
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object)}.
 *
 * @param <T> 操作的第一个参数的类型
 * @param <U> 操作的第二个参数的类型
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface BiConsumer<T, U> {

    /**
     * 对给定的参数执行此操作。
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     */
    void accept(T t, U u);

    /**
     * 返回一个复合的{@code BiConsumer}，该{@code BiConsumer}按顺序执行这个操作，
     * 后面跟着{@code after}操作。
     * 如果执行其中一个操作引发异常，则将其转发给组合操作的调用者。
     * 如果执行此操作会引发异常，则不会执行{@code after}操作。
     *
     * @param after 在这个操作之后要执行的操作
     * @return 一个由{@code BiConsumer}组成的函数，它按顺序执行这个操作，然后再执行{@code after}操作
     * @throws NullPointerException if {@code after} is null
     */
    default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}
