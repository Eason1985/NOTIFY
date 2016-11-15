package com.mdiaf.notify.store.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import com.mdiaf.notify.conf.Configuration;
import com.mdiaf.notify.store.IDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Eason on 16/5/20.
 */
public class MysqlDataSource implements IDataSource {

    private DataSource dataSource;

    public MysqlDataSource(Configuration configuration) throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(configuration.getUrl());
        druidDataSource.init();
        this.dataSource = druidDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void release(Connection connection) {
        //dataSource connection pool can do this
    }

    @Override
    public void close() {
        //todo
    }
}
