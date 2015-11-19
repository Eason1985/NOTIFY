package com.mdiaf.notify.store;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * use the SQLite
 * Created by Eason on 15/11/14.
 */
public enum JDBCTemplateFactory {

    LOCAL("LOCAL"), REMOTE("REMOTE");

    private DataSource target;

    private JdbcTemplate jdbcTemplate;

    /**
     * get jdbcTemplate witch by mode.
     */
    private final String mode;

    private static final String STORE_LOCAL_PATH = "/Users/Eason/workspace/SQLite";
    private static final String STORE_LOCAL_DRIVER_CLASS_NAME = "org.sqlite.JDBC";
    private static final String STORE_LOCAL_DB = "notify.db";

    JDBCTemplateFactory(String mode) {
        this.mode = mode;
    }

    public synchronized JdbcTemplate getJdbcTemplate(){
        if (jdbcTemplate == null) {
            if (target == null) {
                initDataSource();
            }
            jdbcTemplate = new JdbcTemplate(target);
        }
        return jdbcTemplate;
    }

    public DataSource getTarget() {
        return target;
    }

    public String getMode() {
        return mode;
    }

    private void initDataSource() {
        checkOrCreatePath();

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(STORE_LOCAL_DRIVER_CLASS_NAME);
        druidDataSource.setUrl("jdbc:sqlite:"+STORE_LOCAL_PATH+"/"+STORE_LOCAL_DB);
        try {
            druidDataSource.init();
            target = druidDataSource;
        } catch (SQLException e) {
            throw new RuntimeException("[SQLite]datasource init fault!", e);
        }
    }

    private void checkOrCreatePath() {
        File file = new File(STORE_LOCAL_PATH);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
    }

}
