package org.mason.b3;

/**
 * Created by bmason42 on 10/4/15.
 */
public class B3Exception extends RuntimeException{
    public B3Exception() {
    }

    public B3Exception(String message) {
        super(message);
    }

    public B3Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public B3Exception(Throwable cause) {
        super(cause);
    }

    public B3Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
