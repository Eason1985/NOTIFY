package com.mdiaf.notify.message;

import java.io.Serializable;

/**
 * 用于异步通信的消息定义
 * 包含 {@link Serializable} body 和 {@link MessageHeader} 两部分 <p>
 * Created by Eason on 15/10/4.
 */
public interface IMessage extends Serializable{

    /**
     * @return messageHeader of the message
     */
    MessageHeader getHeader();

    /**
     * set messageHeader
     * @param messageHeader
     */
    void setHeader(MessageHeader messageHeader);

    /**
     * get body
     */
    Object getBody();

    /**
     * @return bytes of the body
     */
    byte[] toBytes() ;

    //todo  extension property, just like org.springframework.amqp.core.MessageProperties
//    MessageProperties getMessageProperties();
}
