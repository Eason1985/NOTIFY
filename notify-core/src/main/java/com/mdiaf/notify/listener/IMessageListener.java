package com.mdiaf.notify.listener;

import com.mdiaf.notify.message.IMessage;

/**
 * Created by Eason on 15/9/11.
 */
public interface IMessageListener {

    void handle(IMessage message) throws Exception;

}
