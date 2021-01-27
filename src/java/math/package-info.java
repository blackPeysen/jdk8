/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 提供用于执行任意精度整数（{@code BigInteger}）和任意精度十进制（{@code BigDecimal}）的类。
 * {@code BigInteger}与原始整数类型类似，只是它提供任意精度，因此对{@code BigInteger}的操作不会溢出或丢失精度。
 * 除标准算术运算外，{@code BigInteger}还提供模块化算术，GCD计算，素数测试，素数生成，位操作以及一些其他杂项运算。
 *
 * {@code BigDecimal}提供适用于货币计算等的任意精度的带符号十进制数字。
 * {@code BigDecimal}为用户提供了对舍入行为的完全控制，允许用户从八个舍入模式的全面集合中进行选择。
 *
 * @since JDK1.1
 */
package java.math;
