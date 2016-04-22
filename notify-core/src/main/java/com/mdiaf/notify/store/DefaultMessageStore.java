package com.mdiaf.notify.store;

import com.mdiaf.notify.message.IMessage;
import com.mdiaf.notify.utils.SerializationUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * create table {name}
 * Created by Eason on 15/11/14.
 */
public class DefaultMessageStore implements IMessageStore {

    private final static Logger logger = LoggerFactory.getLogger(IMessageStore.class);

    private final IDataSource dataSource;
    private final String tableName;

    public DefaultMessageStore(IDataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        init();
    }

    @Override
    public void saveOrUpdate(IMessage message) throws SQLException {
        MessageBean bean = new MessageBean(message.getHeader().getTopic(),
                message.getHeader().getType(), message.getHeader().getGroupId(),
                message.getHeader().getUniqueId(), message.toBytes());
        bean.setTableName(tableName);

        MessageBean messageBean = bean.findByUniqueId(dataSource);
        if (messageBean != null) {
            bean.incTimes(dataSource);
        } else {
            bean.insert(dataSource);
        }
    }

    @Override
    public List<IMessage> findMomentBefore(long seconds) throws SQLException {
        MessageBean bean = new MessageBean();
        bean.setTableName(tableName);
        List<IMessage> messages = new ArrayList<>();
        List<MessageBean> messageBeanList = bean.findMomentBefore(seconds, dataSource);
        for (MessageBean messageBean : messageBeanList) {
            IMessage message = (IMessage) SerializationUtil.deserialize(messageBean.getMessage());
            MessageWrapper wrapper = new MessageWrapper(message, messageBean.getTimes(), messageBean.getCreateTime());
            messages.add(wrapper);
        }
        return messages;
    }

    @Override
    public List<IMessage> findMessages(String topic, String msgType, String groupId) throws SQLException {
        MessageBean bean = new MessageBean(topic, msgType, groupId, null, null);
        bean.setTableName(tableName);
        List<IMessage> messages = new ArrayList<>();
        List<MessageBean> messageBeanList = bean.findMessages(dataSource);
        for (MessageBean messageBean : messageBeanList) {
            IMessage message = (IMessage) SerializationUtil.deserialize(messageBean.getMessage());
            MessageWrapper wrapper = new MessageWrapper(message, messageBean.getTimes(), messageBean.getCreateTime());
            messages.add(wrapper);
        }
        return messages;
    }

    @Override
    public void deleteByUniqueId(String uniqueId) throws SQLException {
        MessageBean bean = new MessageBean(null, null, null, uniqueId, null);
        bean.setTableName(tableName);
        bean.deleteByUniqueId(dataSource);
    }

    private void init() {
        logger.info("[NOTIFY]messageStore init,tableName={}", tableName);
        try {
            checkOrCreateTable();
        } catch (SQLException e) {
            throw new RuntimeException("init local store error.", e);
        }
    }

    private void checkOrCreateTable() throws SQLException {
        MessageBean bean = new MessageBean();
        bean.setTableName(tableName);
        bean.createTable(dataSource);
    }

    private class MessageBean implements Serializable {
        private static final long serialVersionUID = 4836496186954452826L;
        private String topic;
        private String messageType;
        private String groupId;
        private String uniqueId;
        private long deliveryTag;
        private byte[] message;
        private Integer times;
        private Integer createTime;
        private Integer modifyTime;

        private String tableName;

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

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void createTable(IDataSource dataSource) throws SQLException {
            Validate.notNull(tableName, "tableName is required!");

            String sql = String.format("create TABLE if not EXISTS %s " +
                    "(uniqueId TEXT PRIMARY KEY ," +
                    "topic TEXT, " +
                    "messageType TEXT , " +
                    "groupId TEXT , " +
                    "message BLOB , " +
                    "times INTEGER NOT NULL DEFAULT 1 , " +
                    "createTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')) , " +
                    "modifyTime INTEGER NOT NULL DEFAULT (strftime('%%s','now')))", tableName);
            execute(sql, dataSource);
        }

        public Integer insert(IDataSource dataSource) throws SQLException {
            Validate.notNull(tableName, "tableName is required!");

            String sql = String.format(
                    "insert into %s " +
                            "(uniqueId, topic, messageType, groupId, message, times)" +
                            "VALUES" +
                            "(? , ? , ? , ? , ? , ? )", tableName);
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uniqueId);
            preparedStatement.setString(2, topic);
            preparedStatement.setString(3, messageType);
            preparedStatement.setString(4, groupId);
            preparedStatement.setBytes(5, message);
            preparedStatement.setInt(6, 1);
            int re = preparedStatement.executeUpdate();
            dataSource.release(connection);
            return re;
        }

        public void deleteByUniqueId(IDataSource dataSource) throws SQLException {
            Validate.notNull(tableName, "tableName is required!");

            String sql = String.format("delete from %s " +
                            "where uniqueId = '%s'",
                    tableName, uniqueId);
            execute(sql, dataSource);
        }

        public MessageBean findByUniqueId(IDataSource dataSource) throws SQLException {
            Validate.notNull(tableName, "tableName is required!");
            Validate.notNull(tableName, "uniqueId is required!");

            String sql = String.format("select * from %s where uniqueId = '%s'", tableName, uniqueId);
            List<MessageBean> list = queryForList(sql, dataSource);
            return list.size() > 0 ? list.get(0) : null;
        }

        public void incTimes(IDataSource dataSource) throws SQLException {
            Validate.notNull(tableName, "tableName is required!");
            Validate.notNull(tableName, "uniqueId is required!");

            String sql = String.format("update %s " +
                    "set times = times + 1, " +
                    "modifyTime = strftime('%%s','now') " +
                    "where uniqueId = '%s'", tableName, uniqueId);
            execute(sql, dataSource);
        }

        public List<MessageBean> findMomentBefore(long seconds, IDataSource dataSource) throws SQLException {
            String sql = String.format("select * from %s where createTime <= %d", tableName, seconds);
            return queryForList(sql, dataSource);
        }

        public List<MessageBean> findMessages(IDataSource dataSource) throws SQLException {
            if (StringUtils.isBlank(tableName) || StringUtils.isBlank(topic) || StringUtils.isBlank(messageType)
                    || StringUtils.isBlank(groupId)) {
                throw new SQLException("tableName, topic, messageType, groupId cat not be null.");
            }

            String sql = String.format("select * from %s where topic = '%s' and messageType = '%s' and groupId = '%s'",
                    tableName, topic, messageType, groupId);
            return queryForList(sql, dataSource);
        }

        private void execute(String sql, IDataSource dataSource) throws SQLException {
            Connection connection = dataSource.getConnection();
            connection.createStatement().execute(sql);
            dataSource.release(connection);
        }

        private List<MessageBean> queryForList(String sql, IDataSource dataSource) throws SQLException {
            Connection connection = dataSource.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            dataSource.release(connection);

            List<MessageBean> messageBeanList = new ArrayList<>();

            while (resultSet.next()) {
                MessageBean bean = new MessageBean(resultSet.getNString("topic"),
                        resultSet.getString("messageType"),
                        resultSet.getString("groupId"),
                        resultSet.getString("uniqueId"),
                        resultSet.getBytes("message"));
                messageBeanList.add(bean);
            }
            return messageBeanList;
        }
    }

}
