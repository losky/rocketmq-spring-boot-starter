package com.eastedu.boot.rocketmq.endpoint;

import com.eastedu.boot.rocketmq.annotation.MessageMode;
import com.eastedu.boot.rocketmq.annotation.MessageType;
import com.eastedu.boot.rocketmq.error.DefaultMessageErrorHandler;
import com.eastedu.boot.rocketmq.error.MessageErrorHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 抽象
 *
 * @author luozhenzhong
 */
public abstract class AbstractRocketMQListenerEndpoint implements RocketMQListenerEndpoint {
    private String id;

    /**
     * topic信息
     *
     * @return
     */
    private String topic;

    /**
     * groupID，消费者集群ID
     *
     * @return
     */
    private String groupId;

    /**
     * 订阅的tag，默认为全部
     *
     * @return
     */
    private String tags;

    /**
     * 自动提交
     *
     * @return
     */
    private boolean autoCommit;

    /**
     * 消息类型
     *
     * @return
     */
    private MessageType messageType;

    /**
     * 订阅方式
     *
     * @return
     */
    private MessageMode messageModel;

    /**
     * todo 未实现
     */
    private Class<? extends MessageErrorHandler> errorHandler;

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        Assert.hasText(groupId, "groupId cannot be empty");
        this.groupId = groupId;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        Assert.hasText(topic, "topic cannot be empty");
        this.topic = topic;
    }

    @Override
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            this.tags = "*";
        } else {
            this.tags = tags;
        }
    }

    @Override
    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public MessageMode getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageMode messageModel) {
        this.messageModel = messageModel;
    }

    public Class<? extends MessageErrorHandler> getErrorHandler() {
        if (errorHandler == null) {
            this.errorHandler = DefaultMessageErrorHandler.class;
        }
        return errorHandler;
    }

    public void setErrorHandler(Class<? extends MessageErrorHandler> errorHandler) {
        this.errorHandler = errorHandler;
    }
}
