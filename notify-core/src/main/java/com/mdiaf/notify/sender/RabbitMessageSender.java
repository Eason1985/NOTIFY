package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eason on 15/9/14.
 */
public class RabbitMessageSender implements IMessageSender {

    private ConnectionFactory connectionFactory;

    private volatile ReturnListener returnListener = new DefaultReturnListener();

    @Override
    public void send(IMessage message, String topic, String messageType) throws IOException {
        Connection conn = connectionFactory.createConnection();
        Channel channel = conn.createChannel(false);
        channel.basicPublish(topic, messageType, true, RabbitMQPropertiesConverter.fromMessage(message), message.toBytes());
        channel.addReturnListener(new InternalListener(returnListener));
//        channel.txCommit();
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
    }

    private class InternalListener implements com.rabbitmq.client.ReturnListener {

        private ReturnListener listener ;

        public InternalListener(ReturnListener listener) {
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
