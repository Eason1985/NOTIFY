package com.mdiaf.notify.sender;

import com.mdiaf.notify.store.IMessageStore;

import java.io.IOException;

/**
 * Created by Eason on 15/11/19.
 */
public class LocalStoreConfirmListener implements com.rabbitmq.client.ConfirmListener {

    private IChannel channel;

    private IMessageStore messageStore;

    @Override
    public void handleAck(long deliveryTag, boolean multiple) throws IOException {

    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {

    }
}
