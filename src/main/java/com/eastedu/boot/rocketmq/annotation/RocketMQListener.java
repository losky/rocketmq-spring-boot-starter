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
     * @return
     */
    String topic();

    /**
     * groupID，消费者集群ID
     *
     * @return
     */
    String groupId();

    /**
     * 订阅的tag，默认为全部
     *
     * @return
     */
    String tags() default "*";

    /**
     * 自动提交
     *
     * @return
     */
    boolean autoCommit() default true;


    /**
     * 异常处理器，未实现
     *
     * @return
     */
    Class<? extends MessageErrorHandler> errorHandler() default DefaultMessageErrorHandler.class;

    /**
     * 消息类型
     *
     * @return
     */
    MessageType messageType() default MessageType.NORMAL;

    /**
     * 消息订阅方式，广播方式、集群方式。默认：集群方式
     */
        MessageMode messageModel() default MessageMode.CLUSTERING;

    /**
     * 消息拉取方式，PUSH、PULL。默认：PUSH
     * 未实现
     */
    FetchType fetchType() default FetchType.PUSH;
}
