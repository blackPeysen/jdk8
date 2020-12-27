/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * <em>功能接口</em> 为lambda表达式和方法引用提供目标类型。
 * 每个函数接口都有一个抽象方法，对于该函数接口，称为函数方法， lambda表达式的参数和返回类型都匹配或适配于该方法。
 * 函数接口可以在多个上下文中提供目标类型，例如赋值上下文、方法调用或强制转换上下文:
 *
 * <pre>{@code
 *     // Assignment context
 *     Predicate<String> p = String::isEmpty;
 *
 *     // Method invocation context
 *     stream.filter(e -> e.getSize() > 10)...
 *
 *     // Cast context
 *     stream.map((ToIntFunction) e -> e.getSize())...
 * }</pre>
 *
 * <p>这个包中的接口是JDK使用的通用功能接口，用户代码也可以使用这些接口。
 * 虽然它们没有确定一组完整的函数形状来适应lambda表达式，但它们提供了足够的功能来满足常见的需求。
 * 其他为特定目的而提供的功能接口，例如{@link java.io。filfilter}定义在使用它们的包中。
 *
 * <p>这个包中的接口是用{@link java.lang.FunctionalInterface}注解的。
 * 这个注释并不是要求编译器将接口识别为功能接口，而只是帮助捕获设计意图，并要求编译器帮助识别偶然违反设计意图的情况。
 *
 * <p>功能接口通常表示抽象概念，如函数、动作或谓词。
 * 在记录函数接口或引用类型为函数接口的变量时，通常直接引用那些抽象概念，例如使用“这个函数”而不是“由这个对象表示的函数”。
 * 当一个API方法以这种方式接受或返回一个函数接口时，例如“将所提供的函数应用到…”，
 * 这被理解为对实现相应函数接口的对象的<i>non-null<i>引用，除非明确指定了潜在的空值。
 *
 * <p>这个包中的函数接口遵循可扩展的命名约定，如下所示:
 *
 * <ul>
 *     <li>有几个基本的功能形状，包括：
 *     {@link java.util.function.Function} (unary function from {@code T} to {@code R}),
 *     {@link java.util.function.Consumer} (unary function from {@code T} to {@code void}),
 *     {@link java.util.function.Predicate} (unary function from {@code T} to {@code boolean}),
 *     {@link java.util.function.Supplier} (nilary function to {@code R}).
 *     {@link java.util.function.UnaryOperator}
 *     {@link java.util.function.BinaryOperator}
 *     </li>
 *
 *     <li>基于最常用的方式，函数形状具有一种自然的相似性。
 *     基本形状可以通过一个粒度前缀来修改，以表示不同的粒度，例如{@link java.util.function。(从{@code T}和{@code U}到{@code R}的二进制函数)。
 *     </li>
 *
 *     <li>还有一些附加的派生函数形状扩展了基本函数形状，
 *     包括{@link java.util.function.UnaryOperator} (extends {@code Function})和{@link java.util.function.BinaryOperator}
 *     (扩展{@code BiFunction})。
 *     </li>
 *
 *     <li>函数接口的类型参数可以专门化为带有附加类型前缀的原语。
 *     为了专门化同时具有泛型返回类型和泛型参数的类型的返回类型，我们在{@code ToXxx}前面加上前缀，如{@link java.util.function.ToIntFunction}。
 *     否则，类型参数是从左到右专门化的，如{@link java.util.function。}或{@link java.util.function.ObjIntConsumer}。
 *     (类型前缀{@code Obj}用于表示我们不希望专门化这个参数，而是希望继续处理下一个参数，如{@link java.util.function.ObjIntConsumer}中所示。)
 *     这些模式可以组合在一起，如{@code IntToDoubleFunction}。
 *     </li>
 *
 *     <li>如果所有参数都有专门化前缀，则可以省略arity前缀(如{@link java.util.function.ObjIntConsumer})。
 *     </li>
 * </ul>
 *
 * @see java.lang.FunctionalInterface
 * @since 1.8
 */
package java.util.function;
