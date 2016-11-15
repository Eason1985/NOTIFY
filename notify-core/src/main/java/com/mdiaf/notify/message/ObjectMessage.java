package com.mdiaf.notify.message;

import com.mdiaf.notify.utils.SerializationUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Eason on 15/10/4.
 */
public class ObjectMessage implements IMessage {

    private static final long serialVersionUID = 9054303586749629129L;
    private final byte[] body;

    private MessageHeader header;

    public ObjectMessage(Object body) {
        if (body instanceof Serializable) {
            this.body = SerializationUtil.serialize(body);
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
        return SerializationUtil.deserialize(body);
    }

    @Override
    public byte[] toBytes() {
        return SerializationUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectMessage that = (ObjectMessage) o;

        return Objects.equals(that.getHeader().getUniqueId(), this.getHeader().getUniqueId());
    }

}
