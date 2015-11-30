package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.message.MessageHeader;

/**
 * Created by Eason on 15/11/28.
 */
public class MessageWrapper implements IMessageWrapper {

    private final IMessage message;
    private final int count;
    private final long sendTimestamp;

    public MessageWrapper(IMessage message, int count, long sendTimestamp) {
        this.message = message;
        this.count = count;
        this.sendTimestamp = sendTimestamp;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public long getSendTimestamp() {
        return sendTimestamp;
    }

    @Override
    public MessageHeader getHeader() {
        return message.getHeader();
    }

    @Override
    public void setHeader(MessageHeader messageHeader) {
        message.setHeader(messageHeader);
    }

    @Override
    public Object getBody() {
        return message.getBody();
    }

    @Override
    public byte[] toBytes() {
        return message.toBytes();
    }
}
