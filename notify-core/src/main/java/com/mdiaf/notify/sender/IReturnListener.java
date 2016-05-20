package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;

/**
 * if the message is delivered fault,then the returnListener will be invoke. <p>
 * do nothing default.
 * Created by Eason on 15/9/26.
 */
public interface IReturnListener {

    void handleReturn(IMessage message);
}
