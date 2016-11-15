package com.mdiaf.notify.sender;

import com.mdiaf.notify.store.IMessageStore;
import com.mdiaf.notify.store.LocalStoreException;
import com.mdiaf.notify.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by Eason on 15/11/19.
 */
public class LocalStoreConfirmListener implements IConfirmListener {

    private static final Logger logger = LoggerFactory.getLogger(IConfirmListener.class);

    private IMessageStore messageStore;

    public LocalStoreConfirmListener(IMessageStore messageStore) {
        this.messageStore = messageStore;
    }

    @Override
    public void handleAck(String msgUnique) throws LocalStoreException {
        try {
            messageStore.deleteByUniqueId(msgUnique);
            // if handleAck fault,we will resend this message again by a timer.
        } catch (StoreException e) {
            logger.warn("[NOTIFY]handleAck fault.cause:" , e);
            throw new LocalStoreException(e);
        }
    }

    @Override
    public void handleNack(String msgUnique) throws LocalStoreException {
        logger.warn("[NOTIFY]message:{} send fault,and resend later.", msgUnique);
    }
}
