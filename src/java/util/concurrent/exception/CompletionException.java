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

/**
 * 在完成结果或任务的过程中遇到错误或其他异常时抛出的异常。
 *
 * @since 1.8
 * @author Doug Lea
 */
public class CompletionException extends RuntimeException {
    private static final long serialVersionUID = 7830266012832686185L;

    /**
     * 构造一个无详细信息的{@code CompletionException}。
     * 原因尚未初始化，随后可以通过调用{@link #initCause（Throwable）initCause}来初始化。
     */
    protected CompletionException() { }

    /**
     * 使用指定的detail 消息构造一个{@code CompletionException}。
     * 原因尚未初始化，并且随后可以通过调用{@link #initCause（Throwable）initCause}来初始化。
     *
     * @param message 详细信息
     */
    protected CompletionException(String message) {
        super(message);
    }

    /**
     * 使用指定的详细信息和原因构造一个{@code CompletionException}。
     *
     * @param  message the detail message
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method)
     */
    public CompletionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造具有指定原因的{@code CompletionException}。
     * 详细消息设置为{@code（cause == null？null：* cause.toString（））}（通常包含{@code cause}的类和*详细消息）。
     *
     * @param  cause 原因（已保存，以供以后通过{@link #getCause（）}方法检索）
     */
    public CompletionException(Throwable cause) {
        super(cause);
    }
}
