package com.mdiaf.notify.store;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Eason on 16/3/25.
 */
public interface IDataSource {

    Connection getConnection() throws SQLException;

    void release(Connection connection);

    void close();
}
