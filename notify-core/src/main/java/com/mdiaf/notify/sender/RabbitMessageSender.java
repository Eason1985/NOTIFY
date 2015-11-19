package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eason on 15/9/14.
 */
public class RabbitMessageSender implements IMessageSender {

    private final static Logger logger = LoggerFactory.getLogger(IMessageSender.class);
    //todo create it by myself one day
    private ConnectionFactory connectionFactory;

    private Channel channel;

    private volatile ReturnListener returnListener = new DefaultReturnListener();

    @Override
    public void send(IMessage message, String topic, String messageType) throws IOException {

        channel.basicPublish(topic, messageType, true, RabbitMQPropertiesConverter.fromMessage(message), message.toBytes());
        try {
            channel.waitForConfirmsOrDie();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("send channel:"+channel.getChannelNumber() + " seqNo:" + channel.getNextPublishSeqNo());
    }

    @Override
    public void expireSend(IMessage message, String topic, String messageType, long delay) throws IOException {
        Connection conn = connectionFactory.createConnection();
        Channel channel = conn.createChannel(false);

        String delayMessageType = messageType + ".delay."+delay;
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl" , delay);
        args.put("x-dead-letter-exchange" , topic);
        args.put("x-dead-letter-routing-key", messageType);
        //定义一个延迟queue,auto delete 如果消费者都不订阅了，queue还有没有存在的必要？ todo
        channel.queueDeclare(delayMessageType, true, false, true, args);
        channel.basicPublish("" , delayMessageType , true, RabbitMQPropertiesConverter.fromMessage(message), message.toBytes()); //发送消息到延迟queue
        channel.addReturnListener(new InternalListener(returnListener));
    }

    @Override
    public void setReturnListener(ReturnListener returnListener) {
        this.returnListener = returnListener;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        Connection conn = connectionFactory.createConnection();
        channel = conn.createChannel(false);
        try {
            channel.confirmSelect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        channel.addReturnListener(new InternalListener(returnListener));
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                logger.debug("confirm deliveryTag:" + deliveryTag);
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {

            }
        });
    }


}
