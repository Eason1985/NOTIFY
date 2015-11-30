package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;

import java.io.IOException;

/**
 *
 * Created by Eason on 15/9/11.
 */
public interface IMessageSender {
    /**
     * step1:save message
     * step2:send it
     * step3:delete it when confirmed
     * @param message
     * @param topic  as exchange , make sure the topic exist in RabbitMQ , before you invoke this method.
     * @param messageType as routingKey
     */
    void send(IMessage message, String topic, String messageType) throws IOException;

    /**
     * step1:save message
     * step2:send it
     * step3:delete it when confirmed
     * @param message
     * @param topic as exchange , as exchange , make sure the topic exist in RabbitMQ , before you invoke this method.
     * @param messageType as routingKey
     * @param delay millisecond of unit
     */
    void expireSend(IMessage message, String topic, String messageType, long delay) throws IOException;

}
