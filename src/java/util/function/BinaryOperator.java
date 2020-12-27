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
import java.util.Comparator;

/**
 * 表示对两个相同类型的操作数的操作，产生与操作数相同类型的结果。
 * 这是{@link BiFunction}的专门化，用于操作数和结果都是同一类型的情况。
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object)}.
 *
 * @param <T> 操作数的类型和操作符的结果
 *
 * @see BiFunction
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T,T,T> {
    /**
     * 返回一个{@link BinaryOperator}，根据指定的{@code Comparator}返回两个元素中较小的一个。
     *
     * @param <T> 比较器输入参数的类型
     * @param comparator 用于比较两个值的{@code Comparator}
     * @return 一个{@code BinaryOperator}，根据提供的{@code Comparator}返回其操作数中较小的一个。
     * @throws NullPointerException if the argument is null
     */
    public static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * 返回一个{@link BinaryOperator}，根据指定的{@code Comparator}返回两个元素中较大的一个。
     *
     * @param <T> 比较器输入参数的类型
     * @param comparator 用于比较两个值的{@code Comparator}
     * @return {@code BinaryOperator}，根据提供的{@code Comparator}返回其操作数中较大的一个。
     * @throws NullPointerException if the argument is null
     */
    public static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }
}
