package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;

/**
 * Created by Eason on 15/12/1.
 */
public class ReturnListenerTest implements IReturnListener {

    @Override
    public void handleReturn(IMessage message) {
        System.err.println("message returned. OMG!! " + message.getHeader().getUniqueId());
    }
}
