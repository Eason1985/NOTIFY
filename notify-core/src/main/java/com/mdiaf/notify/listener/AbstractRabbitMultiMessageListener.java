package com.mdiaf.notify.listener;

import java.util.List;

/**
 * Just provide one listener per Topic and multi messageTypes here.
 * Created by Eason on 15/10/6.
 */
public abstract class AbstractRabbitMultiMessageListener extends AbstractRabbitMessageListener{

    private List<String> messageTypes ;

    public void setMessageTypes(List<String> messageTypes) {
        this.messageTypes = messageTypes;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (String messageType : messageTypes){
            setMessageType(messageType);
            super.afterPropertiesSet();
        }
    }
}
