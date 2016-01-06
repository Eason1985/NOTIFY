package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Eason on 15/10/5.
 */
public class NotifyListener extends AbstractRabbitMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(NotifyListener.class);

    @Override
    public void handle(IMessage message) throws Exception {
        TestMessage testMessage = (TestMessage) message.getBody();
        System.err.println("[NOTIFY]NotifyListener:" + testMessage);
//        throw new Exception("test");
    }

}
