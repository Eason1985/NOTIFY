package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Just provide one listener per Topic and multi messageTypes here.
 * Created by Eason on 15/10/6.
 */
public abstract class AbstractRabbitMultiMessageListener extends AbstractRabbitMessageListener {

    private List<String> messageTypes;

    private List<IMessageListener> holder = new ArrayList<>();

    public void setMessageTypes(List<String> messageTypes) {
        this.messageTypes = messageTypes;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (String messageType : messageTypes) {
            InternalListener listener = new InternalListener();
            listener.setTopic(super.topic);
            listener.setGroupId(super.groupId);
            listener.setMessageType(messageType);
            listener.setConfiguration(super.configuration);
            listener.setConnectionFactory(super.connectionFactory);
            listener.afterPropertiesSet();
            holder.add(listener);
        }
    }

    private class InternalListener extends AbstractRabbitMessageListener {

        @Override
        public void handle(IMessage message) throws Exception {
            AbstractRabbitMultiMessageListener.this.handle(message);
        }
    }
}
