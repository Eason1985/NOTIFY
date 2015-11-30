package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;

/**
 * Created by Eason on 15/11/28.
 */
public interface IMessageWrapper extends IMessage{

    int getCount();

    long getSendTimestamp();

}
