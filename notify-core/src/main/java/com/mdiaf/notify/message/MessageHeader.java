package com.mdiaf.notify.message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Eason on 15/10/4.
 */
public class MessageHeader implements Serializable{

    private static final long serialVersionUID = 2885523576543965689L;

    private String topic;

    private String type;

    private String uniqueId;

    private boolean redeliver;

    private String messageId;

    private final Map<String, Object> properties = new HashMap<String, Object>();

    public MessageHeader() {
        this.uniqueId = UUID.randomUUID().toString();
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTopic() {
        return topic;
    }

    public String getType() {
        return type;
    }

    public void setProperty(String key , Object value){
        this.properties.put(key , value);
    }

    public void setProperties(Map properties){
        this.properties.putAll(properties);
    }

    public Object getProperty(String key){
        return properties.get(key);
    }

    public Map getProperties(){
        return properties;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isRedeliver() {
        return redeliver;
    }

    public void setRedeliver(boolean redeliver) {
        this.redeliver = redeliver;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "topic='" + topic + '\'' +
                ", type='" + type + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", redeliver=" + redeliver +
                ", properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MessageHeader that = (MessageHeader) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(redeliver, that.redeliver)
                .append(topic, that.topic)
                .append(type, that.type)
                .append(uniqueId, that.uniqueId)
                .append(properties, that.properties)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(topic)
                .append(type)
                .append(uniqueId)
                .append(redeliver)
                .append(properties)
                .toHashCode();
    }
}
