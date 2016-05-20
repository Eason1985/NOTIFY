package com.mdiaf.notify.message;

import com.mdiaf.notify.utils.SerializationUtil;

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
}
