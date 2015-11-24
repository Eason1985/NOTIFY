package com.mdiaf.notify.sender;

import com.mdiaf.notify.store.LocalStoreException;

import java.io.IOException;

/**
 * Created by Eason on 15/11/18.
 */
public interface ConfirmListener {

    void handleAck(String msgUnique)
            throws LocalStoreException;

    void handleNack(String msgUnique)
            throws LocalStoreException;
}
