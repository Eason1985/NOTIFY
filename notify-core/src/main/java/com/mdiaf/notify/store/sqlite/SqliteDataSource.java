package com.mdiaf.notify.store.sqlite;

import com.mdiaf.notify.store.IDataSource;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Eason on 16/3/25.
 */
public class SqliteDataSource implements IDataSource {

    private String url;
    private final SQLiteDataSource delegate;
    private LinkedBlockingQueue<Connection> connectionLinkedBlockingQueue = new LinkedBlockingQueue<>();
    private final Object lock = new Object();


    public SqliteDataSource(String url) throws SQLException {
        this.url = url;
        delegate = new SQLiteDataSource();
        delegate.setUrl(url);
        connectionLinkedBlockingQueue.offer(delegate.getConnection());
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection connection = connectionLinkedBlockingQueue.take();
            synchronized (lock) {
                if (connection.isClosed()) {
                    connection = delegate.getConnection();
                }
            }
            return connection;
        } catch (InterruptedException e) {
            throw new SQLException("getConnection error.");
        }
    }

    public void release(Connection connection) {
        connectionLinkedBlockingQueue.offer(connection);
    }

    @Override
    public void close() {
        //todo
    }

    public String getUrl() {
        return url;
    }
}
