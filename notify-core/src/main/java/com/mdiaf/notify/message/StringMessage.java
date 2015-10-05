package com.mdiaf.notify.message;

import com.mdiaf.notify.utils.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 * Created by Eason on 15/10/4.
 */
public class StringMessage implements IMessage {

    private static final long serialVersionUID = -137994579379218876L;

    private byte[] body ;

    private volatile MessageHeader header;

    private volatile String charset = "UTF-8";

    public StringMessage(String body) {
        this.body = body.getBytes();
        this.header = new MessageHeader();
    }

    /**
     *
     * @param body
     * @param charset use default value if set null
     */
    public StringMessage(String body , String charset) throws MessageConversionException {
        if (!StringUtils.isBlank(charset)){
            this.charset = charset;
        }
        try {
            this.body = body.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new MessageConversionException("failed to convert to Message content" , e);
        }
        this.header = new MessageHeader();
    }

    @Override
    public MessageHeader getHeader() {
        return header;
    }

    @Override
    public String getBody() throws MessageConversionException {
        try {
            return new String(body , charset);
        } catch (UnsupportedEncodingException e) {
            throw new MessageConversionException("failed to convert to Message content" , e);
        }
    }

    @Override
    public byte[] toBytes() {
        return SerializationUtils.serialize(this);
    }


    @Override
    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    public String getCharset() {
        return charset;
    }
}
