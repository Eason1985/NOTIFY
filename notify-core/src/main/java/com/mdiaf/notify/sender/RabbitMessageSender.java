package com.mdiaf.notify.sender;

import com.mdiaf.notify.conf.Configuration;
import com.mdiaf.notify.conf.IChannel;
import com.mdiaf.notify.conf.RabbitChannel;
import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.store.IMessageStore;
import com.mdiaf.notify.store.MessageStoreManager;
import com.mdiaf.notify.store.MessageWrapper;
import com.mdiaf.notify.store.StoreException;
import com.mdiaf.notify.utils.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Eason on 15/9/14.
 */
public final class RabbitMessageSender implements IMessageSender, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(IMessageSender.class);

    //todo create it by myself one day
    private ConnectionFactory connectionFactory;

    private Configuration configuration = new Configuration();

    private IMessageStore messageStore;

    private final static Map<String, IMessageSender> messageSenderHolder = new HashMap<>();
    private final static AtomicInteger NUMBER = new AtomicInteger(0);
    private final static String STORE_NAME = "producer_";

    @Override
    public void send(IMessage message, String topic, String messageType) throws IOException {
        IChannel channel = null;
        try {
            message.getHeader().setTopic(topic);
            message.getHeader().setType(messageType);
            messageStore.saveOrUpdate(message);
            channel = RabbitChannel.getOrCreate(connectionFactory.createConnection(), configuration);
            channel.send(message, topic, messageType);
        } catch (StoreException e) {
            throw new IOException("[NOTIFY]message local store fault.", e);
        } finally {
            if (channel != null) {
                channel.free();
            }
        }
    }

    @Override
    public void expireSend(IMessage message, String topic, String messageType, long delay) throws IOException {
        IChannel channel = null;
        try {
            message.getHeader().setDelay(delay);
            message.getHeader().setTopic(topic);
            message.getHeader().setType(messageType);
            messageStore.saveOrUpdate(message);
            channel = RabbitChannel.getOrCreate(connectionFactory.createConnection(), configuration);
            channel.expireSend(message, topic, messageType, delay);
        } catch (StoreException e) {
            throw new IOException("[NOTIFY]message local store fault.", e);
        } finally {
            if (channel != null) {
                channel.free();
            }
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
        synchronized (messageSenderHolder) {
            if (messageSenderHolder.containsKey(connectionFactory.getHost())) {
                throw new RuntimeException("Only one sender per connectionFactory here.please check your config and confirm it.");
            }
            setMessageStore();
            setConfiguration();
            setTimer();
            messageSenderHolder.put(connectionFactory.getHost(), this);
        }

    }

    private void setTimer() {
        Timer timer;
        synchronized (NUMBER) {
            NUMBER.incrementAndGet();
            timer = new Timer("messageSendTimer-" + NUMBER.intValue(), true);
        }

        timer.schedule(new SenderTimer(), configuration.getTimerDelay(), Configuration.SENDER_TIMER_PERIOD);
    }

    private void setMessageStore() {
        String key = String.valueOf(IPUtil.ipToLong(connectionFactory.getHost()));
        String name = STORE_NAME + key;
        messageStore = MessageStoreManager.getOrCreate(configuration, name);
    }

    private void setConfiguration() {
        if (configuration.getConfirmListener() == null) {
            configuration.setConfirmListener(new LocalStoreConfirmListener(this.messageStore));
        }

        if (configuration.getReturnListener() == null) {
            configuration.setReturnListener(new DefaultReturnListener());
        }
    }

    private class SenderTimer extends TimerTask {

        @Override
        public void run() {
            try {
                List<IMessage> messageList = RabbitMessageSender.this.messageStore.
                        findMomentBefore(RabbitMessageSender.this.configuration.getResendPeriod());

                if (logger.isDebugEnabled()) {
                    logger.info("[NOTIFY] {} messages wait for resend", messageList.size());
                }

                for (IMessage message : messageList) {
                    if (message instanceof MessageWrapper) {
                        MessageWrapper wrapper = (MessageWrapper) message;
                        if (wrapper.getCount() >= RabbitMessageSender.this.configuration.getMaxResend()) {
                            RabbitMessageSender.this.configuration.getReturnListener().handleReturn(wrapper.getIMessage());
                            RabbitMessageSender.this.messageStore.deleteByUniqueId(wrapper.getHeader().getUniqueId());
                            continue;
                        }

                        if (wrapper.getHeader().getDelay() > 0) {
                            RabbitMessageSender.this.expireSend(wrapper.getIMessage(), wrapper.getHeader().getTopic(),
                                    wrapper.getHeader().getType(), wrapper.getHeader().getDelay());
                            continue;
                        }

                        RabbitMessageSender.this.send(wrapper.getIMessage(), wrapper.getHeader().getTopic(), wrapper.getHeader().getType());
                    } else {
                        //todo
                    }
                }
            } catch (Exception e) {
                logger.error("[NOTIFY]sendTimer error.", e);
            }
        }
    }
}
