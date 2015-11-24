package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.store.IMessageStore;
import com.mdiaf.notify.store.JDBCTemplateFactory;
import com.mdiaf.notify.store.ProducerMessageStroe;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eason on 15/9/14.
 */
public class RabbitMessageSender implements IMessageSender, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(IMessageSender.class);
    //todo create it by myself one day
    private ConnectionFactory connectionFactory;

    private Configuration configuration;

    private IMessageStore messageStore;

    @Override
    public void send(IMessage message, String topic, String messageType) throws IOException {
        try {
            messageStore.save(message);
            IChannel channel = RabbitChannel.getOrCreate(connectionFactory.createConnection(), configuration);
            channel.send(message, topic, messageType);
            channel.free();
        } catch (SQLException e) {
            throw new IOException("[NOTIFY]message local store fault.", e);
        }
    }

    @Override
    public void expireSend(IMessage message, String topic, String messageType, long delay) throws IOException {
        try {
            messageStore.save(message);
            IChannel channel = RabbitChannel.getOrCreate(connectionFactory.createConnection(), configuration);
            channel.expireSend(message, topic, messageType, delay);
            channel.free();
        } catch (SQLException e) {
            throw new IOException("[NOTIFY]message local store fault.", e);
        }
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        messageStore = new ProducerMessageStroe(JDBCTemplateFactory.LOCAL.getJdbcTemplate());
    }
}
