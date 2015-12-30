package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;

/**
 * Created by Eason on 15/10/6.
 */
public class NotifyMultiListener extends AbstractRabbitMultiMessageListener {
    @Override
    public void handle(IMessage message) throws Exception {
        TestMessage testMessage = (TestMessage) message.getBody();
        throw new Exception("test");
    }
}
