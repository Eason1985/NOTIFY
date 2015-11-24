package com.mdiaf.notify.sender;

import com.mdiaf.notify.store.IMessageStore;

import java.io.IOException;

/**
 * Created by Eason on 15/11/19.
 */
public class LocalStoreConfirmListener implements ConfirmListener {

    private IChannel channel;

    private IMessageStore messageStore;


    @Override
    public void handleAck(String msgUnique) throws IOException {

    }

    @Override
    public void handleNack(String msgUnique) throws IOException {

    }
}
