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
     * @return
     */
    String getId();

    /**
     * group id
     *
     * @return
     */
    String getGroupId();

    /**
     * topic
     *
     * @return
     */
    String getTopic();

    /**
     * message type
     *
     * @return
     */
    default MessageType getMessageType() {
        return MessageType.NORMAL;
    }

    /**
     * expression tag
     *
     * @return
     */
    default String getTags() {
        return "*";
    }

    /**
     * 是否自动提交
     *
     * @return
     */
    default boolean isAutoCommit() {
        return true;
    }

    /**
     * 订阅方式
     *
     * @return
     */
    MessageMode getMessageModel();

}
