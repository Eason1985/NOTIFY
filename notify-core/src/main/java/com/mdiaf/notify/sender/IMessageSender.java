package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;

import java.io.IOException;

/**
 * 两种角色：消息生产者、消息消费者
 * 一种消息可以有多个订阅者
 *
 *topic + messageType 唯一确认一种消息
 *
 * Created by Eason on 15/9/11.
 */
public interface IMessageSender {
    /**
     *
     * @param message
     * @param topic  as exchange , make sure the topic exist in RabbitMQ , before you invoke this method.
     * @param messageType as routingKey
     */
    void send(IMessage message, String topic, String messageType) throws IOException;

    /**
     * @param message
     * @param topic as exchange , as exchange , make sure the topic exist in RabbitMQ , before you invoke this method.
     * @param messageType as routingKey
     * @param delay millisecond of unit
     */
    void expireSend(IMessage message, String topic, String messageType, long delay) throws IOException;

    /**
     * if the message is delivered fault,then the returnListener will be invoke. <p>
     * do nothing default.
     * @param returnListener
     */
    void setReturnListener(ReturnListener returnListener);
}
