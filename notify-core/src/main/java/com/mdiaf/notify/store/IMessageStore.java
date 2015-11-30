package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Eason on 15/11/13.
 */
public interface IMessageStore {

    void saveOrUpdate(IMessage message) throws SQLException;

    void deleteByUniqueId(String uniqueId) throws SQLException;

    /**
     * find out some message which be send sometime before.
     * @param seconds
     * @return
     * @throws SQLException
     */
    List<IMessage> findMomentBefore(long seconds) throws SQLException;

}
