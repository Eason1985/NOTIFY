package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.sender.RabbitMQPropertiesConverter;
import com.mdiaf.notify.store.IMessageStore;
import com.rabbitmq.client.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eason on 15/11/29.
 */
public class DefaultConsumer implements Consumer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConsumer.class);

    private Channel channel;

    private IMessageStore messageStore;

    private IMessageListener messageListener;

    private String consumerTag;

    private String groupId;

    private String msgType;

    private String topic;

    private final static List<DefaultConsumer> consumerHolder = new ArrayList<>();

    public DefaultConsumer(Channel channel, IMessageStore messageStore, IMessageListener messageListener, String groupId,
                           String topic, String msgType) {
        this.channel = channel;
        this.messageStore = messageStore;
        this.messageListener = messageListener;
        this.groupId = groupId;
        this.msgType = msgType;
        this.topic = topic;
        if (consumerHolder.contains(this)) {
            throw new RuntimeException(toString() + " has more than one listener here.");
        }
        consumerHolder.add(this);
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        if (logger.isDebugEnabled()) {
            logger.debug("[NOTIFY]handleConsumeOk:{}", consumerTag);
        }
        this.consumerTag = consumerTag;
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        if (logger.isDebugEnabled()) {
            logger.debug("[NOTIFY]handleCancelOk:{}", consumerTag);
        }
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("[NOTIFY]handleCancel:{}", consumerTag);
        }
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        if (logger.isDebugEnabled()) {
            logger.debug("[NOTIFY]handleShutdownSignal:{}", consumerTag);
        }
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        if (logger.isDebugEnabled()) {
            logger.debug("[NOTIFY]handleRecoverOk:{}", consumerTag);
        }
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        long deliveryTag = envelope.getDeliveryTag();

        IMessage message = null;
        try {
            message = RabbitMQPropertiesConverter.toMessage(properties, body, envelope);
            message.getHeader().setMessageId(properties.getMessageId());
            message.getHeader().setGroupId(groupId);
            message.getHeader().setDeliveredTag(envelope.getDeliveryTag());
            messageStore.saveOrUpdate(message);
            channel.basicAck(deliveryTag, false);

            if (logger.isDebugEnabled()) {
                logger.debug("[NOTIFY]received=>[header:{}]",
                        message.getHeader().toString());
            }

        } catch (Exception e) {
            logger.warn("message handler error,requeue it.", e);
            channel.basicNack(deliveryTag, false, true);
            return;
        }

        try {
            messageListener.handle(message);
            messageStore.deleteByUniqueId(message.getHeader().getUniqueId());
        } catch (Exception e) {
            try {
                messageStore.saveOrUpdate(message);
            } catch (SQLException e1) {
                logger.error("[NOTIFY]messageStore error.", e1);
            }
        }
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    @Override
    public String toString() {
        return "DefaultConsumer{" +
                "groupId='" + groupId + '\'' +
                ", msgType='" + msgType + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DefaultConsumer that = (DefaultConsumer) o;

        return new EqualsBuilder()
                .append(groupId, that.groupId)
                .append(msgType, that.msgType)
                .append(topic, that.topic)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(groupId)
                .append(msgType)
                .append(topic)
                .toHashCode();
    }
}
