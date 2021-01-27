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

package java.util.concurrent.exception;

import java.util.concurrent.future.FutureTask;

/**
 * 异常，指示无法取消产生价值的任务的结果，例如{@link FutureTask}，因为任务已取消。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class CancellationException extends IllegalStateException {
    private static final long serialVersionUID = -9202173006928992231L;

    /**
     * 构造一个无详细信息的{@code CancellationException}。
     */
    public CancellationException() {}

    /**
     * 使用指定的detail 消息构造一个{@code CancellationException}。
     *
     * @param message 详细信息
     */
    public CancellationException(String message) {
        super(message);
    }
}
