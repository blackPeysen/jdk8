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
 * 阻塞操作超时时引发异常。
 * 指定了超时的阻塞操作需要一种手段来*指示发生了超时。
 * 对于许多这样的操作，可以返回表示超时的值；如果不可能或不希望这样做，则应声明并抛出{@code TimeoutException}。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class TimeoutException extends Exception {
    private static final long serialVersionUID = 1900926677490660714L;

    /**
     * 构造一个没有指定详细信息的{@code TimeoutException}。
     */
    public TimeoutException() {}

    /**
     * 使用指定的detail消息构造一个{@code TimeoutException}。
     *
     * @param message 详细信息
     */
    public TimeoutException(String message) {
        super(message);
    }
}
