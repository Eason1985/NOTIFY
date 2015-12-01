package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.utils.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        MessageBean messageBean = bean.findByUniqueId(getTableName(), template);
        if (messageBean != null){
            bean.incTimes(getTableName(), template);
        }else {
            bean.insert(getTableName(), template);
        }
    }

    @Override
    public List<IMessage> findMomentBefore(long seconds) {
        MessageBean bean = new MessageBean();
        List<IMessage> messages = new ArrayList<>();
        List<MessageBean> messageBeanList = bean.findMomentBefore(getTableName(), seconds, template);
        for (MessageBean messageBean : messageBeanList) {
            messages.add((IMessage) SerializationUtils.deserialize(messageBean.getMessage()));
        }
        return messages;
    }

    @Override
    public void deleteByUniqueId(String uniqueId) throws SQLException {
        MessageBean bean = new MessageBean(null, null, null, uniqueId, null);
        bean.deleteByUniqueId(getTableName(), template);
    }

    private void init() {
        logger.info("[notify]messageStore init,tableName={}", getTableName());
        checkOrCreateTable();
    }

    private void checkOrCreateTable() {
        MessageBean bean = new MessageBean();
        bean.createTable(getTableName(), template);
    }

    abstract String getTableName();

    private class MessageBean implements Serializable{
        private static final long serialVersionUID = 4836496186954452826L;
        private String topic;
        private String messageType;
        private String groupId;
        private String uniqueId;
        private byte[] message;
        private Integer times;
        private Integer createTime;
        private Integer modifyTime;

        public MessageBean(String topic, String messageType, String groupId, String uniqueId, byte[] message) {
            this.topic = topic;
            this.messageType = messageType;
            this.groupId = groupId;
            this.uniqueId = uniqueId;
            this.message = message;
        }

        public MessageBean(Map map) {
            this.topic = (String) map.get("topic");
            this.messageType = (String) map.get("messageType");
            this.groupId = (String) map.get("groupId");
            this.uniqueId = (String) map.get("uniqueId");
            this.message = (byte[]) map.get("message");
            this.times = (Integer) map.get("times");
            this.createTime = (Integer) map.get("createTime");
            this.modifyTime = (Integer) map.get("modifyTime");
        }

        public MessageBean() {

        }

        public long getCreateTime() {
            return createTime;
        }

        public long getModifyTime() {
            return modifyTime;
        }

        public int getTimes() {
            return times;
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

        public void createTable(String tableName, JdbcTemplate template) {
            Validate.notNull(tableName, "tableName is required!");

            String sql = String.format("create TABLE if not EXISTS %s " +
                    "(uniqueId TEXT PRIMARY KEY ," +
                    "topic TEXT, " +
                    "messageType TEXT , " +
                    "groupId TEXT , " +
                    "message BLOB , " +
                    "times INTEGER NOT NULL DEFAULT 1 , " +
                    "createTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')) , " +
                    "modifyTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')))" , tableName);
            template.execute(sql);
        }

        public Integer insert(String tableName, JdbcTemplate template) {
            Validate.notNull(tableName, "tableName is required!");

            String sql = String.format(
                    "insert into %s " +
                            "(uniqueId, topic, messageType, groupId, message, times)" +
                            "VALUES" +
                            "(? , ? , ? , ? , ? , ? )", tableName);


            final LobHandler lobHandler=new DefaultLobHandler();
            return template.execute(sql, new AbstractLobCreatingPreparedStatementCallback(lobHandler){
                @Override
                protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException, DataAccessException {
                    ps.setString(1, uniqueId);
                    ps.setString(2, topic);
                    ps.setString(3, messageType);
                    ps.setString(4, groupId);
                    ps.setBytes(5, message);
                    ps.setInt(6, 1);
                }
            });
        }

        public void deleteByUniqueId(String tableName, JdbcTemplate template) {
            Validate.notNull(tableName, "tableName is required!");

            String sql = String.format("delete from %s " +
                            "where uniqueId = '%s'",
                    tableName, uniqueId);
            template.execute(sql);
        }

        public MessageBean findByUniqueId(String tableName, JdbcTemplate template) {
            Validate.notNull(tableName, "tableName is required!");
            Validate.notNull(tableName, "uniqueId is required!");

            String sql = String.format("select * from %s where uniqueId = '%s'", tableName, uniqueId);
            List<Map<String, Object>> mapList = template.queryForList(sql);
            if (mapList.size() > 0) {
                return new MessageBean(mapList.get(0));
            }
            return null;
        }

        public void incTimes(String tableName, JdbcTemplate template) {
            Validate.notNull(tableName, "tableName is required!");
            Validate.notNull(tableName, "uniqueId is required!");

            String sql =  String.format("update %s " +
                    "set times = times + 1, " +
                    "modifyTime = strftime('%%s','now') " +
                    "where uniqueId = '%s'", tableName, uniqueId);
            template.execute(sql);
        }

        public List<MessageBean> findMomentBefore(String tableName, long seconds, JdbcTemplate template) {
            String sql = String.format("select * from %s where createTime <= ?", tableName);
            List<Map<String, Object>> mapList =  template.queryForList(sql, System.currentTimeMillis() / 1000 - seconds);
            List<MessageBean> messageBeanList = new ArrayList<>();
            for (Map map : mapList) {
                messageBeanList.add(new MessageBean(map));
            }
            return messageBeanList;
        }
    }

}
