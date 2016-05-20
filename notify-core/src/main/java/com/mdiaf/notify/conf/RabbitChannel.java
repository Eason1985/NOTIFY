package com.mdiaf.notify.conf;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.sender.IConfirmListener;
import com.mdiaf.notify.sender.RabbitMQPropertiesConverter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

/**
 * Created by Eason on 15/11/17.
 */
public class RabbitChannel implements IChannel {

    private final static Logger logger = LoggerFactory.getLogger(IChannel.class);

    private final Channel delegate;

    private final Connection conn;

    private final Configuration configuration;

    private final ConcurrentHashMap<Long/**deliveryTag**/, String/**uniqueId**/> noConfirms = new ConcurrentHashMap<>();

    private final static ConcurrentLinkedQueue<IChannel> CHANNELS = new ConcurrentLinkedQueue<>();

    public static IChannel getOrCreate(Connection conn, Configuration configuration) throws IOException {
        Asserts.notNull(conn, "[NOTIFY]Connection can not be null.");

        IChannel channel = CHANNELS.poll();
        if (channel != null && channel.isOpen()) {
            return channel;
        }

        return new RabbitChannel(conn, configuration);
    }

    @Override
    public void free() {
        synchronized (CHANNELS) {
            if (!CHANNELS.contains(this) && this.isOpen()) {
                CHANNELS.add(this);
            }
        }
    }

    @Override
    public void close() {
        try {
            delegate.close();
            noConfirms.clear();
        } catch (IOException | TimeoutException e) {
            logger.warn("[NOTIFY]{}", e.getMessage());
        }

    }

    private RabbitChannel(Connection conn, Configuration configuration) throws IOException {
        this.conn = conn;
        this.delegate = conn.createChannel(false);
        this.configuration = configuration;
        initDelegate();

        if (logger.isDebugEnabled()) {
            logger.debug("Channel create success.num={}", delegate.getChannelNumber());
        }
    }

    private void initDelegate() throws IOException {
        delegate.confirmSelect();
        delegate.addConfirmListener(new InternalConfirmListener(configuration.getConfirmListener()));
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
        return delegate.isOpen();
    }

    @Override
    public void send(IMessage message, String topic, String messageType) throws IOException {
//        if (logger.isDebugEnabled()) {
//            logger.debug("[NOTIFY]send=>[header:{}]",
//                    message.getHeader().toString());
//        }

        delegate.basicPublish(topic, messageType, true, RabbitMQPropertiesConverter.fromMessage(message).build(), message.toBytes());
        noConfirms.put(delegate.getNextPublishSeqNo() - 1, message.getHeader().getUniqueId());
    }

    @Override
    public void expireSend(IMessage message, String topic, String messageType, long delay) throws IOException {
        String delayMessageType = messageType + ".delay." + delay;
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", delay);
        args.put("x-dead-letter-exchange", topic);
        args.put("x-dead-letter-routing-key", messageType);
        delegate.queueDeclare(delayMessageType, true, false, true, args);
        AMQP.BasicProperties.Builder builder = RabbitMQPropertiesConverter.fromMessage(message);
        delegate.basicPublish("", delayMessageType, true, builder.build(), message.toBytes()); //发送消息到延迟queue
        noConfirms.put(delegate.getNextPublishSeqNo() - 1, message.getHeader().getUniqueId());
    }

    private class InternalConfirmListener implements com.rabbitmq.client.ConfirmListener {

        private IConfirmListener confirmListener;

        public InternalConfirmListener(IConfirmListener confirmListener) {
            this.confirmListener = confirmListener;
        }

        @Override
        public void handleAck(long deliveryTag, boolean multiple) throws IOException {
            String uniqueId = noConfirms.get(deliveryTag);
            if (StringUtils.isBlank(uniqueId)) {
                logger.warn("[NOTIFY]deliveryTag:{} not in the noConfirms.", deliveryTag);
                return;
            }
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("[NOTIFY]confirm=>deliveryTag={},channelNum={}, uniqueId={}"
//                        , deliveryTag, delegate.getChannelNumber(), uniqueId);
//            }

            confirmListener.handleAck(uniqueId);
            noConfirms.remove(deliveryTag);
        }

        @Override
        public void handleNack(long deliveryTag, boolean multiple) throws IOException {
            String uniqueId = noConfirms.get(deliveryTag);
            if (StringUtils.isBlank(uniqueId)) {
                logger.warn("[NOTIFY]uniqueId:{} not in the noConfirms.", uniqueId);
                return;
            }
            confirmListener.handleNack(uniqueId);
            noConfirms.remove(deliveryTag);
        }
    }
}