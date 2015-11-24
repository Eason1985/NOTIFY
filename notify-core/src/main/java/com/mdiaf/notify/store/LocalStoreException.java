package com.mdiaf.notify.store;

import java.io.IOException;

/**
 * Created by Eason on 15/11/24.
 */
public class LocalStoreException extends IOException {
    private static final long serialVersionUID = 8713395519497883644L;

    public LocalStoreException() {
        super();
    }

    public LocalStoreException(String message) {
        super(message);
    }

    public LocalStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalStoreException(Throwable cause) {
        super(cause);
    }
}
