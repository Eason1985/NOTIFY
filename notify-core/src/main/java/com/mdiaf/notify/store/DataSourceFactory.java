package com.mdiaf.notify.store;

import com.mdiaf.notify.conf.Configuration;
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

    public IDataSource getOrCreate(String mode, String name) throws SQLException {
        synchronized (dataSourceHolder) {
            if (dataSourceHolder.containsKey(name)) {
                return dataSourceHolder.get(name);
            }

            IDataSource sqliteDataSource = create(mode, name);
            dataSourceHolder.put(mode + "_" + name, sqliteDataSource);
            return sqliteDataSource;
        }
    }

    private IDataSource create(String mode, String name) throws SQLException {
        if (StringUtils.isBlank(mode) || Configuration.MODE_LOCAL.equalsIgnoreCase(mode)) {
            checkOrCreatePath(STORE_LOCAL_PATH);
            String url = "jdbc:sqlite:"+STORE_LOCAL_PATH+"/"+name+".db";
            return new SqliteDataSource(url);
        }

        throw new RuntimeException("No support mode: "+mode+" yet");
    }

    private void checkOrCreatePath(String url) {
        String path = StringUtils.isBlank(url) ? STORE_LOCAL_PATH : url;
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }
}
