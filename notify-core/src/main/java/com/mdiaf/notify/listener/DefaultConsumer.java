package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.sender.RabbitMQPropertiesConverter;
import com.mdiaf.notify.store.IMessageStore;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

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

    public DefaultConsumer(Channel channel, IMessageStore messageStore, IMessageListener messageListener, String groupId) {
        this.channel = channel;
        this.messageStore = messageStore;
        this.messageListener = messageListener;
        this.groupId = groupId;
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
            messageStore.saveOrUpdate(message);
            channel.basicAck(deliveryTag, false);

            if (logger.isDebugEnabled()) {
                logger.debug("[NOTIFY]received=>[topic:{},type:{},groupId:{},uniqueId:{}]",
                        message.getHeader().getTopic(),
                        message.getHeader().getType(),
                        message.getHeader().getGroupId(),
                        message.getHeader().getUniqueId());
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
}
