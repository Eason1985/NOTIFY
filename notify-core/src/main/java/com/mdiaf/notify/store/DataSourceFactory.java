package com.mdiaf.notify.store;

import com.mdiaf.notify.conf.Configuration;
import com.mdiaf.notify.store.mysql.MysqlDataSource;
import com.mdiaf.notify.store.sqlite.SqliteDataSource;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eason on 16/3/25.
 */
public enum DataSourceFactory {
    INSTANCE;

    private static final String STORE_LOCAL_PATH = System.getProperty("user.home") + "/.SQLITE";

    private final static Map<String, IDataSource> dataSourceHolder = new HashMap<>();

    public IDataSource getOrCreate(Configuration configuration, String name) throws SQLException {
        synchronized (dataSourceHolder) {
            if (dataSourceHolder.containsKey(name)) {
                return dataSourceHolder.get(name);
            }

            IDataSource sqliteDataSource = create(configuration, name);
            dataSourceHolder.put(configuration.getMode() + "_" + name, sqliteDataSource);
            return sqliteDataSource;
        }
    }

    private IDataSource create(Configuration configuration, String name) throws SQLException {
        if (StringUtils.isBlank(configuration.getMode()) || Configuration.MODE_LOCAL.equalsIgnoreCase(configuration.getMode())) {
            checkOrCreatePath(STORE_LOCAL_PATH);
            String url = "jdbc:sqlite:" + STORE_LOCAL_PATH + "/" + name + ".db";
            return new SqliteDataSource(url);
        }

        if (Configuration.MODE_REMOTE.equalsIgnoreCase(configuration.getMode())) {
            return new MysqlDataSource(configuration);
        }

        throw new RuntimeException("No support mode: " + configuration.getMode() + " yet");
    }

    private void checkOrCreatePath(String url) {
        String path = StringUtils.isBlank(url) ? STORE_LOCAL_PATH : url;
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }
}
