/*
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
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

/**
 * 一个混合样式接口，用于标记在给定延迟后应该对其采取行动的对象。
 *
 * <p>该接口的实现必须定义一个{@code compareTo}方法，该方法提供与其{@code getDelay}方法一致的排序。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Delayed extends Comparable<Delayed> {

    /**
     * 在给定的时间单位内，返回与此对象相关的剩余延迟。
     *
     * @param unit 时间单位
     * @return 剩下的延迟;0或负值表示延迟已经过去
     */
    long getDelay(TimeUnit unit);
}
