package com.eastedu.boot.rocketmq.annotation;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.aliyun.openservices.ons.api.bean.BatchConsumerBean;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.OrderConsumerBean;
import com.aliyun.openservices.ons.api.bean.TransactionProducerBean;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;

/**
 * @author luozhenzhong
 */
public enum MessageType {
    /**
     *
     */
    NORMAL(ConsumerBean.class, MessageListener.class),
    ORDERED(OrderConsumerBean.class, MessageOrderListener.class),
    TRANSACTION(TransactionProducerBean.class, LocalTransactionChecker.class),
    Batch(BatchConsumerBean.class, BatchMessageListener.class),
    ;
    private final Class<?> consumerClass;
    private final Class<?> messageListenerClass;

    MessageType(Class<?> consumerClass, Class<?> messageListenerClass) {
        this.consumerClass = consumerClass;
        this.messageListenerClass = messageListenerClass;
    }

    public Class<?> getConsumerClass() {
        return consumerClass;
    }

    public Class<?> getMessageListenerClass() {
        return messageListenerClass;
    }

    public boolean isBatch() {
        return this == Batch;
    }
}
