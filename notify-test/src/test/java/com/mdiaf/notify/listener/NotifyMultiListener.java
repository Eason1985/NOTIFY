package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Eason on 15/10/6.
 */
public class NotifyMultiListener extends AbstractRabbitMultiMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(NotifyListener.class);

    @Override
    public void handle(IMessage message) throws Exception {
        TestMessage testMessage = (TestMessage) message.getBody();
        System.err.println("[NOTIFY]NotifyMultiListener:" + testMessage);
//        throw new Exception("test");
    }
}
