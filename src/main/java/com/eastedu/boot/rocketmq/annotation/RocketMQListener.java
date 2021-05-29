package com.eastedu.boot.rocketmq.annotation;


import com.eastedu.boot.rocketmq.error.DefaultMessageErrorHandler;
import com.eastedu.boot.rocketmq.error.MessageErrorHandler;

import java.lang.annotation.*;

/**
 * @author luozhenzhong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMQListener {

    String id() default "";

    /**
     * topic信息
     *
     * @return topic
     */
    String topic();

    /**
     * groupID，消费者集群ID
     *
     * @return groupID
     */
    String groupId();

    /**
     * 订阅的tag，默认为全部
     *
     * @return tags
     */
    String tags() default "*";

    /**
     * 自动提交
     *
     * @return autoCommit
     */
    boolean autoCommit() default true;


    /**
     * 异常处理器，未实现
     *
     * @return errorHandler
     */
    Class<? extends MessageErrorHandler> errorHandler() default DefaultMessageErrorHandler.class;

    /**
     * 消息类型
     *
     * @return MessageType
     */
    MessageType messageType() default MessageType.NORMAL;

    /**
     * 消息订阅方式，广播方式、集群方式。默认：集群方式
     *
     * @return messageModel
     */
    MessageMode messageModel() default MessageMode.CLUSTERING;

    /**
     * 消息拉取方式，PUSH、PULL。默认：PUSH
     * 未实现
     *
     * @return fetchType
     */
    FetchType fetchType() default FetchType.PUSH;
}
