package com.mdiaf.notify.store;

import com.mdiaf.notify.conf.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eason on 16/3/22.
 */
public class MessageStoreManager {

    private static final Map<String, IMessageStore> messageStoreHolder = new HashMap<>();
    /**
     * local or remote
     */
    private final static String MODE_LOCAL = "local";
    private final static String MODE_REMOTE = "remote";

    public static IMessageStore getOrCreate(Configuration configuration, String tableName) {

        synchronized (messageStoreHolder) {
            if (messageStoreHolder.containsKey(tableName)) {
                return messageStoreHolder.get(tableName);
            }

            IMessageStore messageStore = create(configuration, tableName);
            messageStoreHolder.put(tableName, messageStore);
            return messageStore;
        }

    }

    private static IMessageStore create(Configuration configuration, String tableName) {

        if (StringUtils.isBlank(configuration.getMode()) || MODE_LOCAL.equalsIgnoreCase(configuration.getMode())) {
            return new DefaultMessageStore(JDBCTemplateFactory.LOCAL.getJdbcTemplate(configuration.getUrl()), tableName);
        }

        if (MODE_REMOTE.equalsIgnoreCase(configuration.getMode())) {
            new DefaultMessageStore(JDBCTemplateFactory.REMOTE.getJdbcTemplate(configuration.getUrl()),
                    tableName);
        }

        throw new RuntimeException("mode must in (local, remote)");
    }
}
