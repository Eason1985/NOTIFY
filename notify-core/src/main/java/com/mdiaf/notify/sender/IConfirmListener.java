package com.mdiaf.notify.sender;

import com.mdiaf.notify.store.LocalStoreException;

/**
 * Created by Eason on 15/11/18.
 */
public interface IConfirmListener {

    void handleAck(String msgUnique)
            throws LocalStoreException;

    void handleNack(String msgUnique)
            throws LocalStoreException;
}
