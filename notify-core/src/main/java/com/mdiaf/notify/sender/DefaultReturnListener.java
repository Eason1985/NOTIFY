package com.mdiaf.notify.sender;

import com.mdiaf.notify.message.IMessage;

import java.io.IOException;

/**
 *
 * Created by Eason on 15/10/4.
 */
public class DefaultReturnListener implements IReturnListener {
    @Override
    public void handleReturn(IMessage message) {
        // do nothing
        System.out.println(message.getBody());
    }
}
