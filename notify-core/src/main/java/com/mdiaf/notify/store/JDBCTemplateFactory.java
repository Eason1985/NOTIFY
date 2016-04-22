package com.mdiaf.notify.store;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
* use the SQLite
* Created by Eason on 15/11/14.
*/
public enum JDBCTemplateFactory {

    LOCAL("LOCAL"), REMOTE("REMOTE");

    private final static Logger logger = LoggerFactory.getLogger(JDBCTemplateFactory.class);
    private static final Map<String, JdbcTemplate> templateMap = new HashMap<>();
    /**
     * get jdbcTemplate witch by mode.
     */
    private final String mode;

    private static final String STORE_LOCAL_PATH = System.getProperty("user.home");
    private static final String STORE_LOCAL_DRIVER_CLASS_NAME = "org.sqlite.JDBC";
    private static final String STORE_LOCAL_DB = "notify.db";

    JDBCTemplateFactory(String mode) {
        this.mode = mode;
    }

    public synchronized JdbcTemplate getJdbcTemplate(final String url){
        String path = StringUtils.isBlank(url) ? STORE_LOCAL_PATH : url;
        path += "/.SQLite";
        String key = mode + "_" + path;

        if (templateMap.containsKey(key)) {
            return templateMap.get(key);
        }

        DataSource dataSource = initDataSource(path);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        templateMap.put(key, jdbcTemplate);

        return jdbcTemplate;
    }

    public String getMode() {
        return mode;
    }

    //todo remote DataSource
    private DataSource initDataSource(String path) {
        checkOrCreatePath(path);

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(STORE_LOCAL_DRIVER_CLASS_NAME);

        if (logger.isInfoEnabled()) {
            logger.info("[NOTIFY]init DataSource in path:{}", path);
        }

        druidDataSource.setUrl("jdbc:sqlite:"+path+"/"+STORE_LOCAL_DB);
        try {
            druidDataSource.init();
            return druidDataSource;
        } catch (SQLException e) {
            throw new RuntimeException("[SQLite]datasource init fault!", e);
        }
    }

    private void checkOrCreatePath(String url) {
        String path = StringUtils.isBlank(url) ? STORE_LOCAL_PATH : url;
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }

}
