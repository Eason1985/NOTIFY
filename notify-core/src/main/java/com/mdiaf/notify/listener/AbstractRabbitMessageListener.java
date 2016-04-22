package com.mdiaf.notify.listener;

import com.mdiaf.notify.conf.Configuration;
import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.sender.RabbitMQPropertiesConverter;
import com.mdiaf.notify.store.*;
import com.mdiaf.notify.utils.IPUtil;
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

    private volatile String DLQ;
    private volatile String queue;
    /**
     * as queue name
     */
    protected volatile String groupId;

    private volatile Channel channel;

    private final static AtomicInteger NUMBER = new AtomicInteger(0);
    private final static String STORE_NAME = "consumer_";

    protected Configuration configuration = new Configuration();

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
        //todo 为了不与1.0版本冲突，在所有queue前新增前缀
        this.queue = "V-" + groupId + "." + messageType;
        this.DLQ = queue + ".DLQ";

        setMessageStore();
        setChannel();
        setConsumer();
        //if this.channel close, timer can reopen it .
        setTimer();
    }

    private void setTimer(){
        Timer timer;
        synchronized (NUMBER) {
            NUMBER.incrementAndGet();
            timer = new Timer("messageListener-"+NUMBER.intValue(), true);
        }

        timer.schedule(new HeartbeatTimer(), configuration.getTimerDelay(), 60 * 1000);
        timer.schedule(new ListenerTimer(), configuration.getTimerDelay(), Configuration.RECEIVED_TIMER_PERIOD);
    }

    private void setMessageStore() {
        String key = String.valueOf(IPUtil.ipToLong(connectionFactory.getHost()));
        String name = STORE_NAME + key;
        messageStore = MessageStoreManager.getOrCreate(configuration, name);
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private void setChannel() throws IOException {
        Connection conn = connectionFactory.createConnection();
        channel = conn.createChannel(false);
        //给监听的队列声明一个死信队列
        channel.queueDeclare(DLQ, true, false, true, null);
        channel.queueBind(DLQ, topic, DLQ);

        //定义监听队列。
        Map<String , Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-routing-key", DLQ);
        arguments.put("x-dead-letter-exchange", topic);
        channel.queueDeclare(queue, true, false, true, arguments);
        //设置监听队列
        channel.queueBind(queue, topic, messageType);

    }

    protected void setConsumer() throws IOException {
        channel.basicQos(1);// 均衡投递
        channel.basicConsume(queue, false, new DefaultConsumer(channel, messageStore, this, groupId, topic, messageType));
    }

    private class HeartbeatTimer extends TimerTask {

        @Override
        public void run() {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("[NOTIFY]HeartbeatTimer Running...");
                }

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
                        findMessages(topic, messageType, groupId);

                if (logger.isDebugEnabled()) {
                    logger.info("[NOTIFY]{} messages wait for consume", messageList.size());
                }

                for (IMessage message : messageList) {
                    if (message instanceof MessageWrapper) {
                        MessageWrapper wrapper = (MessageWrapper) message;
                        if (wrapper.getSendTimestamp()
                                > System.currentTimeMillis() / 1000 - AbstractRabbitMessageListener.this.configuration.getResendPeriod()){
                            // not yet
                            continue;
                        }

                        if (wrapper.getCount() >= AbstractRabbitMessageListener.this.configuration.getMaxResend()) {
                            AbstractRabbitMessageListener.this.configuration.getReturnListener().handleReturn(wrapper.getIMessage());
                            sendToDLQ(wrapper.getIMessage());
                            AbstractRabbitMessageListener.this.messageStore.deleteByUniqueId(wrapper.getHeader().getUniqueId());
                            continue;
                        }

                        try {
                            handle(wrapper.getIMessage());
                            messageStore.deleteByUniqueId(wrapper.getHeader().getUniqueId());
                        }catch (Exception e) {
                            logger.error("[NOTIFY]timer handle error.", e);
                            messageStore.saveOrUpdate(wrapper.getIMessage());
                        }

                    }else {
                        //todo
                    }
                }
            } catch (Exception e) {
                logger.error("[NOTIFY]sendTimer error.", e);
            }
        }

        private void sendToDLQ(IMessage message) throws IOException {
            AbstractRabbitMessageListener.this.
                    channel.basicPublish(topic, DLQ, true, RabbitMQPropertiesConverter.fromMessage(message).build(), message.toBytes());
        }
    }
}
