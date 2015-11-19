package com.mdiaf.notify.store;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Eason on 15/11/13.
 */
public class ProducerMessageStroe extends AbstractMessageStore  {

    private final String tableName = "producer";

    public ProducerMessageStroe(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    String getTableName() {
        return tableName;
    }
}
