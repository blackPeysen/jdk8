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
 * 表示接受两个参数并产生结果的函数。
 * 这是{@link Function}的二元专门化。
 *
 * <p>这是一个<a href="包摘要 一个函数方法是{@link apply(Object, Object)}。
 *
 * @param <T> 函数的第一个参数的类型
 * @param <U> 函数的第二个参数的类型
 * @param <R> 函数结果的类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface BiFunction<T, U, R> {

    /**
     * 将此函数应用于给定的参数。
     *
     * @param t 第一个函数参数
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u);

    /**
     * 返回一个复合函数，该函数首先将此函数应用于其输入，然后将{@code after}函数应用于结果。
     * 如果任意一个函数的求值抛出异常，则将其传递给组合函数的调用者。
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after 应用此函数后要应用的函数
     * @return 一个组合函数，它首先应用这个函数，然后在函数之后应用
     * @throws NullPointerException if after is null
     */
    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }

}
