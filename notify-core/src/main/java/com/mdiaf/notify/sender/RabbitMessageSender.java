package com.mdiaf.notify.sender;

import com.mdiaf.notify.conf.Configuration;
import com.mdiaf.notify.conf.IChannel;
import com.mdiaf.notify.conf.RabbitChannel;
import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.store.IMessageStore;
import com.mdiaf.notify.store.JDBCTemplateFactory;
import com.mdiaf.notify.store.MessageWrapper;
import com.mdiaf.notify.store.ProducerMessageStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Eason on 15/9/14.
 */
public class RabbitMessageSender implements IMessageSender, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(IMessageSender.class);

    //todo create it by myself one day
    private ConnectionFactory connectionFactory;

    private Configuration configuration = new Configuration();

    private IMessageStore messageStore;

    private Timer timer;
    private final static AtomicInteger NUMBER = new AtomicInteger(0);

    /**
     * local or remote
     */
    private final static String MODE_LOCAL = "local";
    private final static String MODE_REMOTE = "remote";
    private String mode = MODE_LOCAL;//default is local

    @Override
    public void send(IMessage message, String topic, String messageType) throws IOException {
        IChannel channel = null;
        try {
            message.getHeader().setTopic(topic);
            message.getHeader().setType(messageType);
            channel = RabbitChannel.getOrCreate(connectionFactory.createConnection(), configuration);
            messageStore.saveOrUpdate(message);
            channel.send(message, topic, messageType);
        } catch (SQLException e) {
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
            channel.free();
        } catch (SQLException e) {
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

    public void setMode(String mode) {
        if (StringUtils.isBlank(mode)) {
            return;
        }
        this.mode = mode;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setMessageStore();
        setConfiguration();
        setTimer();
    }

    private void setTimer() {
        synchronized (NUMBER) {
            NUMBER.incrementAndGet();
            timer = new Timer("messageSendTimer-"+NUMBER.intValue(), true);
        }

        timer.schedule(new SenderTimer(), Configuration.TIMER_DELAY, Configuration.SENDER_TIMER_PERIOD);
    }

    private void setMessageStore() {

        if (MODE_LOCAL.equalsIgnoreCase(this.mode)) {
            messageStore = new ProducerMessageStore(JDBCTemplateFactory.LOCAL.getJdbcTemplate());
        }else if (MODE_REMOTE.equalsIgnoreCase(this.mode)) {
            messageStore = new ProducerMessageStore(JDBCTemplateFactory.REMOTE.getJdbcTemplate());
        }else {
            throw new RuntimeException("mode must in (local, remote)");
        }
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

                if (logger.isInfoEnabled()) {
                    logger.info("[NOTIFY] {} messages wait for resend", messageList.size());
                }

                for (IMessage message : messageList) {
                    if (message instanceof MessageWrapper) {
                        MessageWrapper wrapper = (MessageWrapper) message;
                        if (wrapper.getCount() >= RabbitMessageSender.this.configuration.getMaxResend()) {
                            RabbitMessageSender.this.configuration.getReturnListener().handleReturn(message);
                            RabbitMessageSender.this.messageStore.deleteByUniqueId(message.getHeader().getUniqueId());
                            continue;
                        }

                        if (wrapper.getHeader().getDelay() > 0) {
                            if (message.getHeader().getDelay() < System.currentTimeMillis() - ((MessageWrapper) message).getSendTimestamp()*1000) {
                                RabbitMessageSender.this.expireSend(message, message.getHeader().getTopic(), message.getHeader().getType(), message.getHeader().getDelay());
                                continue;
                            }
                        }

                        RabbitMessageSender.this.send(message, message.getHeader().getTopic(), message.getHeader().getType());
                    }else {
                        //todo
                    }
                }
            } catch (Exception e) {
                logger.error("[NOTIFY]sendTimer error.", e);
            }
        }
    }
}
