/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */
package java.math;

/**
 * 为能够舍弃精度的数值运算指定舍入行为。
 * 每个舍入模式指示如何计算舍入结果的最低有效位。
 * 如果返回的位数少于表示精确数值结果所需的位数，则无论这些位数对数字值的贡献如何，丢弃的位数都将被称为舍弃分数。
 * 换句话说，被视为数值，丢弃的分数可能具有大于1的绝对值。
 *
 * <p>每个舍入模式的描述都包含一个表格，该表格列出了在所讨论的舍入模式下，不同的两位十进制值将如何舍入为一位十进制值。
 * 表中的结果列可通过创建具有指定值的{@code BigDecimal}数字，形成具有适当设置的{@link MathContext}对象（{@code precision}设置为{@code 1}，
 * 并将{@code roundingMode}设置为所讨论的舍入模式），然后用正确的{@code MathContext}对该数字调用{@link BigDecimal＃round round}。
 * 下表显示了所有舍入模式下这些舍入运算的结果。
 *
 *<table border>
 * <caption><b>不同舍入模式下的舍入操作摘要</b></caption>
 * <tr><th></th><th colspan=8>使用给定舍入模式将输入舍入到一位数字的结果</th>
 * <tr valign=top>
 * <th>Input Number</th>         <th>{@code UP}</th>
 *                                           <th>{@code DOWN}</th>
 *                                                        <th>{@code CEILING}</th>
 *                                                                       <th>{@code FLOOR}</th>
 *                                                                                    <th>{@code HALF_UP}</th>
 *                                                                                                   <th>{@code HALF_DOWN}</th>
 *                                                                                                                    <th>{@code HALF_EVEN}</th>
 *                                                                                                                                     <th>{@code UNNECESSARY}</th>
 *
 * <tr align=right><td>5.5</td>  <td>6</td>  <td>5</td>    <td>6</td>    <td>5</td>  <td>6</td>      <td>5</td>       <td>6</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>2.5</td>  <td>3</td>  <td>2</td>    <td>3</td>    <td>2</td>  <td>3</td>      <td>2</td>       <td>2</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>1.6</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>2</td>      <td>2</td>       <td>2</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>1.1</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>1.0</td>  <td>1</td>  <td>1</td>    <td>1</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>1</td>
 * <tr align=right><td>-1.0</td> <td>-1</td> <td>-1</td>   <td>-1</td>   <td>-1</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>-1</td>
 * <tr align=right><td>-1.1</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>-1.6</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-2</td>     <td>-2</td>      <td>-2</td>      <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>-2.5</td> <td>-3</td> <td>-2</td>   <td>-2</td>   <td>-3</td> <td>-3</td>     <td>-2</td>      <td>-2</td>      <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>-5.5</td> <td>-6</td> <td>-5</td>   <td>-5</td>   <td>-6</td> <td>-6</td>     <td>-5</td>      <td>-6</td>      <td>throw {@code ArithmeticException}</td>
 *</table>
 *
 *
 * <p>此{@code 枚举}旨在替换{@link BigDecimal} （{@link BigDecimal＃ROUND_UP}，{@link BigDecimal＃ROUND_DOWN}等）中基于整数的舍入模式常量的枚举。
 *
 * @see     BigDecimal
 * @see     MathContext
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @since 1.5
 */
public enum RoundingMode {

        /**
         * 舍入模式从零舍入。
         * 始终在非零废弃分数之前增加数字。请注意，这种四舍五入模式永远不会减小计算出的值的大小。
         * 向远离零的方向舍入：若舍入位为非零，则对舍入部分的前一位数字加1；若舍入位为零，则直接舍弃。即为向外取整模式。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode UP Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code UP} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    UP(BigDecimal.ROUND_UP),

        /**
         * 舍入模式向零舍入。
         * 切勿在舍弃小数（即截断）之前增加数字。注意，这种四舍五入模式永远不会增加计算值的大小。
         * 向接近零的方向舍入：不论舍入位是否为零，都直接舍弃。即为向内取整模式。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode DOWN Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code DOWN} rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    DOWN(BigDecimal.ROUND_DOWN),

        /**
         * 舍入模式向正无穷大舍入。
         * 如果结果为正，则行为与{@code RoundingMode.UP}相同；
         * 如果为负，则与{@code RoundingMode.DOWN}一样。
         * 注意此舍入模式永远不会减少计算值。
         * 向正无穷大的方向舍入：若 BigDecimal 为正，则舍入行为与 ROUND_UP 相同；若为负，则舍入行为与 ROUND_DOWN 相同。即为向上取整模式。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode CEILING Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code CEILING} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    CEILING(BigDecimal.ROUND_CEILING),

        /**
         * 舍入模式向负无穷大舍入。
         * 如果结果为肯定，则行为与{@code RoundingMode.DOWN}相同；
         * 如果为负，则与{@code RoundingMode.UP}一样。
         * 注意此舍入模式永远不会增加计算值。
         * 向负无穷大的方向舍入： 若 BigDecimal 为正，则舍入行为与 ROUND_UP 相同；若为负，则舍入行为与 ROUND_DOWN 相同。即为向上取整模式。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode FLOOR Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code FLOOR} rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    FLOOR(BigDecimal.ROUND_FLOOR),

        /**
         * 舍入模式向{@literal“最近的邻居”} 舍入，除非两个邻居等距。
         * 如果丢弃的*分数≥0.5，则行为与{@code RoundingMode.UP}相同；
         * 否则，其行为与{@code RoundingMode.DOWN}相同。
         * 请注意，这是学校通常教的四舍五入模式。
         * 向“最接近的”整数舍入：若舍入位大于等于5，则对舍入部分的前一位数字加1；若舍入位小于5，则直接舍弃。即为四舍五入模式。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode HALF_UP Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code HALF_UP} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_UP(BigDecimal.ROUND_HALF_UP),

        /**
         * 舍入模式向{@literal“最近的邻居”} 舍入，除非两个邻居都是等距的，在这种情况下舍入。
         * 如果丢弃的分数> 0.5，则行为与{@code RoundingMode.UP}相同；
         * 否则，其行为与{@code RoundingMode.DOWN}相同。
         * 向“最接近的”整数舍入：若舍入位大于5，则对舍入部分的前一位数字加1；若舍入位小于等于5，则直接舍弃。即为五舍六入模式。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode HALF_DOWN Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code HALF_DOWN} rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),

        /**
         * 舍入模式向{@literal“最近的邻居”} 舍入，除非两个邻居都等距，在这种情况下，向偶数邻居舍入。
         * 如果丢弃的分数左侧的数字为奇数，则{@code RoundingMode.HALF_UP}的行为；的行为与{@code RoundingMode.HALF_DOWN}相同。
         * 请注意，这是四舍五入模式，当在一系列计算中重复应用时，统计上最小化累积误差。
         * 它有时被称为{@literal“银行家四舍五入”，}并且*主要在美国使用。
         * 这种舍入模式类似于Java中用于{@code float}和{@code double}算术的舍入策略。
         * 向“最接近的”整数舍入：
         *         若（舍入位大于5）或者（舍入位等于5且前一位为奇数），则对舍入部分的前一位数字加1；
         *         若（舍入位小于5）或者（舍入位等于5且前一位为偶数），则直接舍弃。即为银行家舍入模式。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode HALF_EVEN Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code HALF_EVEN} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),

        /**
         * 舍入模式可以断言所请求的操作具有精确的*结果，因此不需要舍入。
         * 如果在产生不精确结果的操作上指定了这种舍入模式，则会抛出{@code ArithmeticException}。
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode UNNECESSARY Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code UNNECESSARY} rounding
         *<tr align=right><td>5.5</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>2.5</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>1.6</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>1.1</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>-1.6</td> <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>-2.5</td> <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>-5.5</td> <td>throw {@code ArithmeticException}</td>
         *</table>
         */
    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    // Corresponding BigDecimal rounding constant
    final int oldMode;

    /**
     * Constructor
     *
     * @param oldMode 与此模式对应的{@code BigDecimal}常量
     */
    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }

    /**
     * 返回与{@link BigDecimal}中的旧式整数舍入模式常量相对应的{@code RoundingMode}对象。
     *
     * @param  rm 旧式整数舍入模式进行转换
     * @return {@code RoundingMode} 对应于给定的整数。
     * @throws IllegalArgumentException 整数超出范围
     */
    public static RoundingMode valueOf(int rm) {
        switch(rm) {

        case BigDecimal.ROUND_UP:
            return UP;

        case BigDecimal.ROUND_DOWN:
            return DOWN;

        case BigDecimal.ROUND_CEILING:
            return CEILING;

        case BigDecimal.ROUND_FLOOR:
            return FLOOR;

        case BigDecimal.ROUND_HALF_UP:
            return HALF_UP;

        case BigDecimal.ROUND_HALF_DOWN:
            return HALF_DOWN;

        case BigDecimal.ROUND_HALF_EVEN:
            return HALF_EVEN;

        case BigDecimal.ROUND_UNNECESSARY:
            return UNNECESSARY;

        default:
            throw new IllegalArgumentException("argument out of range");
        }
    }
}
