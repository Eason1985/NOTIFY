package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.utils.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
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
    public void saveOrUpdate(IMessage message) throws SQLException {
        MessageBean bean = new MessageBean(message.getHeader().getTopic(),
                message.getHeader().getType(), message.getHeader().getGroupId(),
                message.getHeader().getUniqueId(), message.toBytes());

        String select = bean.findByUniqueId(getTableName());
        List<MessageBean> messageBeanList = template.queryForList(select, MessageBean.class);
        if (messageBeanList.size() > 1){
            String inc = bean.incTimes(getTableName());
            template.execute(inc);
        }else {
            String inert = bean.insertSQL(getTableName());
            template.execute(inert);
        }
    }

    @Override
    public List<IMessage> findMomentBefore(long seconds) {
        String sql = String.format("select * from $s where createTime < %d", getTableName(),
                (System.currentTimeMillis()/1000 - seconds));
        List<MessageBean> messageBeanList = template.queryForList(sql, MessageBean.class);
        List<IMessage> result = new ArrayList<>();
        for (MessageBean bean : messageBeanList) {
            IMessage message = (IMessage) SerializationUtils.deserialize(bean.getMessage());
            MessageWrapper wrapper = new MessageWrapper(message, bean.getTimes(), bean.getCreateTime());
            result.add(wrapper);
        }
        return result;
    }

    @Override
    public void deleteByUniqueId(String uniqueId) throws SQLException {
        MessageBean bean = new MessageBean(null, null, null, uniqueId, null);
        String sql = bean.deleteByUniqueId(getTableName());
        template.execute(sql);
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
        private String topic;
        private String messageType;
        private String groupId;
        private String uniqueId;
        private byte[] message;
        private int times;
        private long createTime;
        private long modifyTime;

        public MessageBean(String topic, String messageType, String groupId, String uniqueId, byte[] message) {
            this.topic = topic;
            this.messageType = messageType;
            this.groupId = groupId;
            this.uniqueId = uniqueId;
            this.message = message;
        }

        public MessageBean() {

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

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
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

        public String getUniqueId() {
            return uniqueId;
        }

        public String createTable(String tableName) {
            Validate.notNull(tableName, "tableName is required!");
            return String.format("create TABLE if not EXISTS %s " +
                    "(uniqueId TEXT PRIMARY KEY ," +
                    "(topic TEXT, " +
                    "messageType TEXT , " +
                    "groupId TEXT , " +
                    "message BLOB , " +
                    "times INTEGER NOT NULL DEFAULT 1 , " +
                    "createTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')) , " +
                    "modifyTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')))" , tableName);
        }

        public String insertSQL(String tableName) {
            Validate.notNull(tableName, "tableName is required!");
            return String.format(
                    "insert into %s " +
                            "(uniqueId, topic, messageType, groupId, message, times)" +
                            "VALUES" +
                            "('%s', '%s', '%s', '%s', '%s', %d)",
                    tableName, uniqueId, topic, messageType, groupId, message, 1);
        }

        public String deleteByUniqueId(String tableName) {
            Validate.notNull(tableName, "tableName is required!");
            return String.format("delete from %s" +
                    "where uniqueId = '%s'",
                    tableName, uniqueId);
        }

        public String findAll(String tableName) {
            Validate.notNull(tableName, "tableName is required!");
            return String.format("select * from %s", tableName);
        }

        public String findByUniqueId(String tableName) {
            Validate.notNull(tableName, "tableName is required!");
            Validate.notNull(tableName, "uniqueId is required!");
            return String.format("select * from %s where uniqueId = %s", tableName, uniqueId);
        }

        public String incTimes(String tableName) {
            Validate.notNull(tableName, "tableName is required!");
            Validate.notNull(tableName, "uniqueId is required!");
            return String.format("update %s set times = times + 1 where uniqueId = %s", tableName, uniqueId);
        }
    }
}
