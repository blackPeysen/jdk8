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
 * 表示接受一个参数并产生结果的函数。
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> 函数的输入类型
 * @param <R> 函数结果的类型
 *
 * @since 1.8
 */
@FunctionalInterface
public interface Function<T, R> {

    /**
     * 对给定的参数应用这个函数。
     *
     * @param t 函数参数
     * @return 函数的结果
     */
    R apply(T t);

    /**
     * 返回一个复合函数，该函数首先将{@code before}函数应用于其输入，然后将该函数应用于结果。
     * 如果任意一个函数的求值抛出异常，则将其传递给组合函数的调用者。
     *
     * @param <V> {@code before}函数和组成函数的输入类型
     * @param before 在应用此功能之前要应用的功能
     * @return 一个组合函数，首先应用{@code before}函数，然后应用此函数
     * @throws NullPointerException 如果before为null
     *
     * @see #andThen(Function)
     */
    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    /**
     * 返回一个复合函数，该函数首先将此函数应用于其输入，然后将{@code after}函数应用于结果。
     * 如果任意一个函数的求值抛出异常，则将其传递给组合函数的调用者。
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     *
     * @see #compose(Function)
     */
    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    /**
     * 返回一个始终返回其输入参数的函数。
     *
     * @param <T> 函数的输入和输出对象的类型
     * @return 始终返回其输入参数的函数
     */
    static <T> Function<T, T> identity() {
        return t -> t;
    }
}
