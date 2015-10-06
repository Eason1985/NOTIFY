package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.sender.RabbitMQPropertiesConverter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Eason on 15/9/26.
 */
public abstract class AbstractRabbitMessageListener implements IMessageListener , InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(IMessageListener.class);

    private ConnectionFactory connectionFactory;
    /**
     * as exchange
     */
    private volatile String topic;
    /**
     * as routing key
     */
    private volatile String messageType;
    /**
     * as queue name
     */
    private volatile String groupId;

    private volatile String queueName;

    public void init(){
        Connection conn = connectionFactory.createConnection();
        final Channel channel = conn.createChannel(false);

        try {
            channel.basicQos(1);// 均衡投递
            //设置队列listener
            channel.basicConsume(queueName, false,
                new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag,
                                               Envelope envelope,
                                               AMQP.BasicProperties properties,
                                               byte[] body)
                            throws IOException
                    {
                        long deliveryTag = envelope.getDeliveryTag();

                        try {
                            IMessage message = RabbitMQPropertiesConverter.toMessage(properties , body , envelope);
                            AbstractRabbitMessageListener.this.handle(message);
                        } catch (Exception e) {
                            if (envelope.isRedeliver()){
                                if (logger.isDebugEnabled()){
                                    logger.debug("deliveryTag:"+deliveryTag + " isRedeliver,reject now!");
                                }
                                channel.basicReject(deliveryTag, false);
                                return;
                            }
                            logger.warn("message handler error,requeue it.", e);
                            channel.basicNack(deliveryTag, false, true);
                            return;
                        }

                        channel.basicAck(deliveryTag , false);
                    }
                });
        } catch (IOException e) {
            logger.warn("[MessageListener init error]topic="+topic,e);
        }

    }

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
        queueName = groupId + "." + messageType;
        if (StringUtils.isBlank(topic) || StringUtils.isBlank(messageType) || StringUtils.isBlank(groupId)){
            throw new Exception("missing parameters in your messageListener config");
        }

        Connection conn = connectionFactory.createConnection();
        final Channel channel = conn.createChannel(true);
        //给监听的队列声明一个死信队列
        channel.queueDeclare(queueName + ".DLQ", true, false, true, null);
        channel.queueBind(queueName + ".DLQ", topic, queueName + ".DLQ");

        //定义监听队列。
        Map<String , Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-routing-key" , groupId + ".DLQ");
        arguments.put("x-dead-letter-exchange" , topic);
        channel.queueDeclare(queueName, true, false, true, arguments);
        //设置监听队列
        channel.queueBind(queueName, topic, messageType);
    }
}
