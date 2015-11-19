package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * create table {name}
 * Created by Eason on 15/11/14.
 */
public abstract class AbstractMessageStore implements IMessageStore {

    private final static Logger logger = LoggerFactory.getLogger(IMessageStore.class);

    private final JdbcTemplate template;

    public AbstractMessageStore(JdbcTemplate jdbcTemplate) {
        this.template = jdbcTemplate;
        init();
    }

    @Override
    public void save(IMessage message) throws SQLException {
        MessageBean bean = new MessageBean();
        bean.setDeliveredTag(message.getHeader().getDeliveredTag());
        bean.setGroupId(message.getHeader().getGroupId());
        bean.setMessage(message.toBytes());
        bean.setMessageId(message.getHeader().getMessageId());
        bean.setMessageType(message.getHeader().getType());
        bean.setTopic(message.getHeader().getTopic());

        String sql = bean.insertSQL(getTableName());

        Statement statement = template.getDataSource().getConnection().createStatement();
        statement.executeUpdate(sql);

    }

    @Override
    public void delete(IMessage message) {
        MessageBean bean = new MessageBean();
        bean.setDeliveredTag(message.getHeader().getDeliveredTag());
        bean.setGroupId(message.getHeader().getGroupId());
        bean.setMessage(message.toBytes());
        bean.setMessageId(message.getHeader().getMessageId());
        bean.setMessageType(message.getHeader().getType());
        bean.setTopic(message.getHeader().getTopic());

        String sql = bean.delete(getTableName());
        template.execute(sql);
    }

    @Override
    public List findALL() {
        MessageBean bean =  new MessageBean();
        String sql = bean.findAll(getTableName());

        return template.queryForList(sql, MessageBean.class);
    }

    private void init() {
        logger.info("[notify]messageStore init,tableName=%s", getTableName());
        checkOrCreateTable();
    }

    private void checkOrCreateTable() {
        MessageBean bean = new MessageBean();
        String sql = bean.createTable(getTableName());
        template.execute(sql);
    }

    abstract String getTableName();

    private class MessageBean implements Serializable{
        private static final long serialVersionUID = 4836496186954452826L;
        private String messageId;
        private String topic;
        private String messageType;
        private String groupId;
        private byte[] message;
        private int count;
        private long createTime;
        private long modifyTime;
        private long deliveredTag;

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public void setMessage(byte[] message) {
            this.message = message;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getModifyTime() {
            return modifyTime;
        }

        public void setModifyTime(long modifyTime) {
            this.modifyTime = modifyTime;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setDeliveredTag(long deliveredTag) {
            this.deliveredTag = deliveredTag;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getTopic() {
            return topic;
        }

        public String getMessageType() {
            return messageType;
        }

        public String getGroupId() {
            return groupId;
        }

        public byte[] getMessage() {
            return message;
        }

        public long getDeliveredTag() {
            return deliveredTag;
        }

        public String createTable(String tableName) {
            Validate.notNull(tableName, "tableName is required!");

            return String.format("create TABLE if not EXISTS %s " +
                    "(messageId TEXT ," +
                    "topic TEXT, " +
                    "messageType TEXT , " +
                    "groupId TEXT , " +
                    "message BLOB , " +
                    "deliveredTag INTEGER NOT NULL, " +
                    "count INTEGER NOT NULL DEFAULT 1 , " +
                    "createTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')) , " +
                    "modifyTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')))" , tableName);
        }

        public String insertSQL(String tableName) {
            Validate.notNull(tableName, "tableName is required!");

            return String.format(
                    "insert into %s " +
                            "(messageId, topic, messageType, groupId, message, deliveredTag)" +
                            "VALUES" +
                            "('%s', '%s', '%s', '%s', '%s', %d)",
                    tableName, messageId, topic, messageType, groupId, message, deliveredTag);
        }

        public String delete(String tableName) {
            Validate.notNull(tableName, "tableName is required!");

            return String.format("delete from %s" +
                    "where topic = '%s' and messageType = '%s' and deliveredTag = %d",
                    tableName, topic, messageType, deliveredTag);
        }

        public String findAll(String tableName) {
            Validate.notNull(tableName, "tableName is required!");

            return String.format("select * from %s", tableName);
        }
    }
}
