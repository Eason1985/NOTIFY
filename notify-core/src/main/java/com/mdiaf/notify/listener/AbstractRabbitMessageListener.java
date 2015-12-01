package com.mdiaf.notify.listener;

import com.mdiaf.notify.conf.Configuration;
import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.store.ConsumerMessageStore;
import com.mdiaf.notify.store.IMessageStore;
import com.mdiaf.notify.store.JDBCTemplateFactory;
import com.mdiaf.notify.store.MessageWrapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *Just provide one listener per Topic and per messageType here.
 * Created by Eason on 15/9/26.
 */
public abstract class AbstractRabbitMessageListener implements IMessageListener , InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(IMessageListener.class);

    protected ConnectionFactory connectionFactory;

    private IMessageStore messageStore;
    /**
     * as exchange
     */
    protected volatile String topic;
    /**
     * as routing key
     */
    protected volatile String messageType;
    /**
     * as queue name
     */
    protected volatile String groupId;

    private volatile Channel channel;

    private Timer timer;
    private final static AtomicInteger NUMBER = new AtomicInteger(0);

    protected Configuration configuration;

    /**
     * local or remote
     */
    private final static String MODE_LOCAL = "local";
    private final static String MODE_REMOTE = "remote";
    private String mode = MODE_LOCAL;//default is local

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(topic) || StringUtils.isBlank(messageType) || StringUtils.isBlank(groupId)){
            throw new Exception("missing parameters in your messageListener config");
        }

        setMessageStore();
        setChannel();
        setConsumer();
        //if this.channel close, timer can reopen it .
        setTimer();
    }

    private void setTimer(){
        synchronized (NUMBER) {
            NUMBER.incrementAndGet();
            timer = new Timer("messageListener-"+NUMBER.intValue(), true);
        }

        timer.schedule(new HeartbeatTimer(), 3 * 60 * 1000, 3 * 1000);
        timer.schedule(new ListenerTimer(), 3 * 60 * 1000, 60 * 1000);
    }

    private void setMessageStore() {
        if (MODE_LOCAL.equalsIgnoreCase(this.mode)) {
            messageStore = new ConsumerMessageStore(JDBCTemplateFactory.LOCAL.getJdbcTemplate());
        }else if (MODE_REMOTE.equalsIgnoreCase(this.mode)) {
            messageStore = new ConsumerMessageStore(JDBCTemplateFactory.REMOTE.getJdbcTemplate());
        }else {
            throw new RuntimeException("mode must in (local, remote)");
        }
    }

    public void setMode(String mode) {
        if (StringUtils.isBlank(mode)) {
            return;
        }
        this.mode = mode;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void setChannel() throws IOException {
        String queueName = groupId + "." + messageType;
        Connection conn = connectionFactory.createConnection();
        channel = conn.createChannel(false);
        //给监听的队列声明一个死信队列
        channel.queueDeclare(queueName + ".DLQ", true, false, true, null);
        channel.queueBind(queueName + ".DLQ", topic, queueName + ".DLQ");

        //定义监听队列。
        Map<String , Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-routing-key", groupId + ".DLQ");
        arguments.put("x-dead-letter-exchange", topic);
        channel.queueDeclare(queueName, true, false, true, arguments);
        //设置监听队列
        channel.queueBind(queueName, topic, messageType);

    }

    protected void setConsumer() throws IOException {
        String queueName = groupId + "." + messageType;
        channel.basicQos(1);// 均衡投递
        channel.basicConsume(queueName, false, new DefaultConsumer(channel, messageStore, this));
    }

    private class HeartbeatTimer extends TimerTask {

        @Override
        public void run() {
            try {
                if (!channel.isOpen()) {
                    setChannel();
                }
            }catch (Exception e) {
                logger.error("[NOTIFY]HeartbeatTimer error.", e);
            }
        }
    }

    private class ListenerTimer extends TimerTask {

        @Override
        public void run() {
            try {
                List<IMessage> messageList = AbstractRabbitMessageListener.this.messageStore.
                        findMomentBefore(AbstractRabbitMessageListener.this.configuration.getResendPeriod());
                for (IMessage message : messageList) {
                    if (message instanceof MessageWrapper) {
                        MessageWrapper wrapper = (MessageWrapper) message;
                        if (wrapper.getCount() >= AbstractRabbitMessageListener.this.configuration.getMaxResend()) {
                            AbstractRabbitMessageListener.this.configuration.getReturnListener().handleReturn(message);
                            AbstractRabbitMessageListener.this.messageStore.deleteByUniqueId(message.getHeader().getUniqueId());
                            return;
                        }

                        try {
                            handle(message);
                            messageStore.deleteByUniqueId(message.getHeader().getUniqueId());
                        }catch (Exception e) {
                            logger.error("[NOTIFY]timer handle error.", e);
                            messageStore.saveOrUpdate(message);
                        }

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
