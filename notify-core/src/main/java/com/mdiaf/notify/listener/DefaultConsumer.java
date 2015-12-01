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

    public DefaultConsumer(Channel channel, IMessageStore messageStore, IMessageListener messageListener) {
        this.channel = channel;
        this.messageStore = messageStore;
        this.messageListener = messageListener;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    @Override
    public void handleCancelOk(String consumerTag) {

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {

    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {

    }

    @Override
    public void handleRecoverOk(String consumerTag) {

    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        long deliveryTag = envelope.getDeliveryTag();

        IMessage message = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("deliveryTag:"+deliveryTag );
                logger.debug("consumerTag:" + consumerTag);
            }

            message = RabbitMQPropertiesConverter.toMessage(properties, body, envelope);
            message.getHeader().setMessageId(properties.getMessageId());
            messageStore.saveOrUpdate(message);
            channel.basicAck(deliveryTag, false);
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
}
