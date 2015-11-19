package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Eason on 15/11/13.
 */
public interface IMessageStore {

    void save(IMessage message) throws SQLException;

    void delete(IMessage message);

    List findALL();

}
