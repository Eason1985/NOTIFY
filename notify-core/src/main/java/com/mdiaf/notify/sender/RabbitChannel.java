package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Eason on 15/11/17.
 */
public class RabbitChannel implements IChannel {

    private final static Logger logger = LoggerFactory.getLogger(IChannel.class);

    private final Channel delegate;

    private final Connection conn;

    private final Configuration configuration;

    private final ConcurrentHashMap<String, String> noConfirms = new ConcurrentHashMap<>();

    private final static ConcurrentLinkedQueue<IChannel> CHANNELS = new ConcurrentLinkedQueue<IChannel>();

    public static IChannel getOrCreate(Connection conn, Configuration configuration) throws IOException {
        Asserts.notNull(conn, "Connection can not be null.");

        IChannel channel = CHANNELS.poll();
        if (channel != null && channel.isOpen()) {
            return channel;
        }

        return new RabbitChannel(conn, configuration);
    }

    public void free() {
        synchronized (CHANNELS) {
            if (!CHANNELS.contains(this)) {
                CHANNELS.add(this);
            }
        }
    }

    private RabbitChannel(Connection conn, Configuration configuration) throws IOException {
        this.conn = conn;
        this.delegate = conn.createChannel(false);
        this.configuration = configuration;
        initDelegate();
    }

    private void initDelegate() throws IOException {
        delegate.confirmSelect();
        delegate.addReturnListener(new InternalReturnListener(configuration.getReturnListener()));
        delegate.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                logger.debug("confirm deliveryTag:" + deliveryTag);
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {

            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RabbitChannel channel = (RabbitChannel) o;

        return new EqualsBuilder()
                .append(delegate, channel.delegate)
                .append(conn, channel.conn)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(delegate)
                .append(conn)
                .append(noConfirms)
                .toHashCode();
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public String getNoConfirmMessageUniqueId(long deliveryTag) {
        return null;
    }

    @Override
    public void confirmAck(long deliveryTag) {

    }

    @Override
    public void send(IMessage message, String topic, String messageType) throws IOException {

    }

    @Override
    public void expireSend(IMessage message, String topic, String messageType, long delay) throws IOException {

    }

    private class InternalReturnListener implements com.rabbitmq.client.ReturnListener {

        private ReturnListener listener ;

        public InternalReturnListener(ReturnListener listener) {
            this.listener = listener;
        }

        @Override
        public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
            IMessage message = RabbitMQPropertiesConverter.toMessage(properties , body);
            message.getHeader().setTopic(exchange);
            message.getHeader().setType(routingKey);
            listener.handleReturn(replyCode , replyText , message);
        }
    }
}