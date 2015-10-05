package com.mdiaf.notify.message;

/**
 * Created by Eason on 15/10/4.
 */
public class MessageConversionException extends RuntimeException {
    private static final long serialVersionUID = 6346619632210310275L;

    public MessageConversionException() {
        super();
    }

    public MessageConversionException(String message) {
        super(message);
    }

    public MessageConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageConversionException(Throwable cause) {
        super(cause);
    }

    protected MessageConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
