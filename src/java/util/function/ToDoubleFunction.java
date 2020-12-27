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
 * 表示产生双值结果的函数： 将一个实例对象 转换成 double
 * 这是为{@link Function}产生{@code double}的原语专门化。
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsDouble(Object)}.
 *
 * @param <T> 函数的输入类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface ToDoubleFunction<T> {

    /**
     * 对给定的参数应用这个函数。
     *
     * @param value 函数参数
     * @return the function result
     */
    double applyAsDouble(T value);
}
