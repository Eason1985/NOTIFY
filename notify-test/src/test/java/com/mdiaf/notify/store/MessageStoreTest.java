package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.message.ObjectMessage;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Eason on 15/11/16.
 */
public class MessageStoreTest {

    private static JdbcTemplate jdbcTemplate = JDBCTemplateFactory.LOCAL.getJdbcTemplate();

    private IMessageStore messageStore;

    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {
        messageStore = new TestMessageStore(jdbcTemplate);
    }

    @org.testng.annotations.Test
    public void testSave() throws Exception {
        String msg = "hello world";
        IMessage message = new ObjectMessage(msg);
        messageStore.save(message);
    }

    @org.testng.annotations.Test
    public void testDelete() throws Exception {

    }

    @org.testng.annotations.Test
    public void testFindALL() throws Exception {

    }

    @org.testng.annotations.Test
    public void testGetTableName() throws Exception {

    }

    public class TestMessageStore extends AbstractMessageStore {

        public TestMessageStore(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        @Override
        String getTableName() {
            return "test";
        }
    }
}