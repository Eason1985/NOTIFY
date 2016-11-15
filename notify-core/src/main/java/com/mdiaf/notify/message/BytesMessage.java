package com.mdiaf.notify.message;

import com.mdiaf.notify.utils.SerializationUtil;

import java.util.Objects;

/**
 * Created by Eason on 15/10/4.
 */
public class BytesMessage implements IMessage {

    private static final long serialVersionUID = 216737541369926910L;
    private volatile MessageHeader header;

    private byte[] body;

    public BytesMessage(byte[] body) {
        this.body = body;
        this.header = new MessageHeader();
    }

    @Override
    public MessageHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public byte[] toBytes() {
        return SerializationUtil.serialize(this);
    }

    @Override
    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BytesMessage that = (BytesMessage) o;

        return Objects.equals(that.getHeader().getUniqueId(), this.getHeader().getUniqueId());
    }

}
