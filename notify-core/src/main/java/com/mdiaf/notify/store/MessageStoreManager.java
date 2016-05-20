package com.mdiaf.notify.store;

import com.mdiaf.notify.conf.Configuration;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eason on 16/3/22.
 */
public class MessageStoreManager {

    private static final Map<String, IMessageStore> messageStoreHolder = new HashMap<>();

    public static IMessageStore getOrCreate(Configuration configuration, String name) {

        synchronized (messageStoreHolder) {
            if (messageStoreHolder.containsKey(name)) {
                return messageStoreHolder.get(name);
            }

            try {
                IMessageStore messageStore = create(configuration, name);
                messageStoreHolder.put(name, messageStore);
                return messageStore;
            } catch (SQLException e) {
                throw new RuntimeException("create messageStore error.", e);
            }
        }

    }

    private static IMessageStore create(Configuration configuration, String name) throws SQLException {
        IDataSource sqliteDataSource = DataSourceFactory.INSTANCE.getOrCreate(configuration.getMode(), name);
        return new DefaultMessageStore(sqliteDataSource, name);
    }
}
