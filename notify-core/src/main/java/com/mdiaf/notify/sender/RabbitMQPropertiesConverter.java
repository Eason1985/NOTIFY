package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.*;
import com.mdiaf.notify.utils.SerializationUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;



/**
 * build {@link AMQP.BasicProperties} from {@link IMessage}
 * Created by Eason on 15/10/4.
 */
public class RabbitMQPropertiesConverter {

    public static final String CONTENT_TYPE_BYTES = "application/octet-stream";

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    public static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String CONTENT_TYPE_JSON_ALT = "text/x-json";

    public static final String CONTENT_TYPE_XML = "application/xml";

    public static final String PROPERTY_TOPIC = "topic";
    public static final String PROPERTY_TYPE = "type";


    public static AMQP.BasicProperties fromMessage(IMessage message){
        if (message instanceof StringMessage){
            return fromMessage((StringMessage) message);
        }

        if (message instanceof ObjectMessage){
            return fromMessage((ObjectMessage)message);
        }

        if (message instanceof BytesMessage){
            return fromMessage((BytesMessage)message);
        }

        return new AMQP.BasicProperties.Builder().build();
    }

    public static AMQP.BasicProperties fromMessage(StringMessage message){
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.contentEncoding(message.getCharset());
        builder.contentType(CONTENT_TYPE_TEXT_PLAIN);
        return builder.build();
    }

    public static AMQP.BasicProperties fromMessage(ObjectMessage message){
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.contentType(CONTENT_TYPE_SERIALIZED_OBJECT);
        return builder.build();
    }

    public static AMQP.BasicProperties fromMessage(BytesMessage message){
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.contentType(CONTENT_TYPE_BYTES);
        return builder.build();
    }

    public static IMessage toMessage(AMQP.BasicProperties properties , byte[] bytes){
        IMessage message = (IMessage) SerializationUtils.deserialize(bytes);
        message.getHeader().setMessageId(properties.getMessageId());
        return message;
    }

    public static IMessage toMessage(AMQP.BasicProperties properties , byte[] bytes , Envelope envelope){
        IMessage message = (IMessage) SerializationUtils.deserialize(bytes);
        message.getHeader().setMessageId(properties.getMessageId());
        message.getHeader().setTopic(envelope.getExchange());
        message.getHeader().setType(envelope.getRoutingKey());
        message.getHeader().setRedeliver(envelope.isRedeliver());
        return message;
    }
}
