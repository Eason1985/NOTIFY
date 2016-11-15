package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;

/**
 * Created by Eason on 2016/11/15.
 */
public interface IBlockMessageStore extends IMessageStore {

    /**
     * take message the time before some seconds.
     * @param seconds
     * @return
     */
    IMessage takeBeforeSeconds(long seconds);
}
