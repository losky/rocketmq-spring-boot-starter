package com.eastedu.boot.rocketmq.endpoint;

import com.eastedu.boot.rocketmq.annotation.MessageMode;
import com.eastedu.boot.rocketmq.annotation.MessageType;

/**
 * 配置信息
 *
 * @author luozhenzhong
 */
public interface RocketMQListenerEndpoint {

    /**
     * ID
     *
     * @return id
     */
    String getId();

    /**
     * group id
     *
     * @return GroupId
     */
    String getGroupId();

    /**
     * topic
     *
     * @return topic
     */
    String getTopic();

    /**
     * message type
     *
     * @return MessageType
     */
    default MessageType getMessageType() {
        return MessageType.NORMAL;
    }

    /**
     * expression tag
     *
     * @return tag
     */
    default String getTags() {
        return "*";
    }

    /**
     * 是否自动提交
     *
     * @return AutoCommit
     */
    default boolean isAutoCommit() {
        return true;
    }

    /**
     * 订阅方式
     *
     * @return MessageModel
     */
    MessageMode getMessageModel();

    /**
     * bean nam
     *
     * @return BeanName
     */
    String getBeanName();

}
