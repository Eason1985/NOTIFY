package com.mdiaf.notify.store;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

/**
 * Created by Eason on 15/11/13.
 */
public class ConsumerMessageStore extends AbstractMessageStore {

    private final static String tableName = "consumer";

    public ConsumerMessageStore(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    String getTableName() {
        return tableName;
    }

}
