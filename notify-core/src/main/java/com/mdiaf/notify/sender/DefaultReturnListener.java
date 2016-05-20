package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Eason on 15/10/4.
 */
public class DefaultReturnListener implements IReturnListener {

    private final static Logger logger = LoggerFactory.getLogger(IReturnListener.class);

    @Override
    public void handleReturn(IMessage message) {
        // do nothing
        logger.info("[NOTIFY] MESSAGE RETURN:{}", message.getHeader().toString());
    }
}
