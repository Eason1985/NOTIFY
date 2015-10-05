package com.mdiaf.notify.message;

import com.mdiaf.notify.utils.SerializationUtils;

import java.io.Serializable;

/**
 * Created by Eason on 15/10/4.
 */
public class ObjectMessage implements IMessage {

    private static final long serialVersionUID = 9054303586749629129L;
    private byte[] body;

    private MessageHeader header;

    public ObjectMessage(Object body) {
        if (body instanceof Serializable){
            this.body = SerializationUtils.serialize(body);
            header = new MessageHeader();
            return;
        }
        throw new MessageConversionException("the body in ObjectMessage should implements Serializable");
    }

    @Override
    public MessageHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(MessageHeader messageHeader) {
        this.header = messageHeader;
    }

    @Override
    public Object getBody() {
        return SerializationUtils.deserialize(body);
    }

    @Override
    public byte[] toBytes() {
        return SerializationUtils.serialize(this);
    }

}
