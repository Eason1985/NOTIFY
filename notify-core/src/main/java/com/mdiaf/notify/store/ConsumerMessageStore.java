package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Created by Eason on 15/11/13.
 */
public class ConsumerMessageStore extends AbstractMessageStore {

    private final String tableName = "consumer";

    public ConsumerMessageStore(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    String getTableName() {
        return tableName;
    }
}
