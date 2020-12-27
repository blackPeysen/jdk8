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

/**
 * 表示接受两个参数并产生一个整型值结果的函数。
 * 这是为{@link BiFunction}生成{@code int}的基元专门化。
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsInt(Object, Object)}.
 *
 * @param <T> 函数的第一个参数的类型
 * @param <U> 函数的第二个参数的类型
 *
 * @see BiFunction
 * @since 1.8
 */
@FunctionalInterface
public interface ToIntBiFunction<T, U> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    int applyAsInt(T t, U u);
}
