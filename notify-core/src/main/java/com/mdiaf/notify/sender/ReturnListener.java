package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;
import com.rabbitmq.client.AMQP;

import java.io.IOException;

/**
 * if the message is delivered fault,then the returnListener will be invoke. <p>
 * do nothing default.
 * Created by Eason on 15/9/26.
 */
public interface ReturnListener {

    void handleReturn(int replyCode,
                      String replyText,
                      IMessage message)
            throws IOException;
}
