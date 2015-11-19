package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;

import java.io.IOException;

/**
 * Created by Eason on 15/11/18.
 */
public interface ConfirmListener {

    void handleAck(IMessage message)
            throws IOException;

    void handleNack(IMessage message)
            throws IOException;
}
