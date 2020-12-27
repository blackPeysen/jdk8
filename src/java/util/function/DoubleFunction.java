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
 * 表示接受双值实参并产生结果的函数： 将一个double参数转换为指定的返回类型的实例对象
 * 这是{@link Function}使用{@code double}的原语专门化。
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(double)}.
 *
 * @param <R> 函数结果的类型
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface DoubleFunction<R> {

    /**
     * 对给定的参数应用这个函数。
     *
     * @param value 函数参数
     * @return 函数的结果
     */
    R apply(double value);
}
