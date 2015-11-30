package com.mdiaf.notify.store;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Eason on 15/11/13.
 */
public class ProducerMessageStore extends AbstractMessageStore  {

    private final static String tableName = "producer";

    public ProducerMessageStore(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    String getTableName() {
        return tableName;
    }

}
