package com.mdiaf.notify.conf;

import com.mdiaf.notify.sender.IMessageSender;

/**
 * Created by Eason on 15/11/18.
 */
public interface IChannel extends IMessageSender {

    boolean isOpen();

    void free();

    void close();
}
