package com.eastedu.boot.rocketmq;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.bean.TransactionProducerBean;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;

import java.util.concurrent.ExecutorService;

/**
 * @author luozhenzhong
 */
public class RocketMQTemplate {

    private final ProducerFactory producerFactory;

    public RocketMQTemplate(ProducerFactory producerFactory) {
        this.producerFactory = producerFactory;
    }

    /**
     * 发送顺序消息
     *
     * @param message     消息
     * @param shardingKey 顺序消息选择因子，发送方法基于shardingKey选择具体的消息队列
     *
     * @return {@link SendResult} 消息发送结果，含消息Id
     */
    public SendResult sendOrder(final Message message, final String shardingKey) {
        OrderProducer producer = producerFactory.createProducer(OrderProducerBean.class);
        return producer.send(message, shardingKey);
    }


    /**
     * 该方法用来发送一条事务型消息. 一条事务型消息发送分为三个步骤:
     * <ol>
     *     <li>本服务实现类首先发送一条半消息到到消息服务器;</li>
     *     <li>通过<code>executer</code>执行本地事务;</li>
     *     <li>根据上一步骤执行结果, 决定发送提交或者回滚第一步发送的半消息;</li>
     * </ol>
     *
     * @param message  要发送的事务型消息
     * @param executer 本地事务执行器
     * @param arg      应用自定义参数，该参数可以传入本地事务执行器
     *
     * @return 发送结果.
     */
    public SendResult sendTransaction(final Message message,
                                      final LocalTransactionExecuter executer,
                                      final Object arg) {
        TransactionProducer producer = producerFactory.createProducer(TransactionProducerBean.class);
        return producer.send(message, executer, arg);
    }


    /**
     * 同步发送消息，只要不抛异常就表示成功
     *
     * @param message 要发送的消息对象
     *
     * @return 发送结果，含消息Id, 消息主题
     */
    public SendResult send(final Message message) {
        Producer producer = producerFactory.createProducer(ProducerBean.class);
        return producer.send(message);
    }

    /**
     * 发送消息，Oneway形式，服务器不应答，无法保证消息是否成功到达服务器
     *
     * @param message 要发送的消息
     */
    public void sendOneway(final Message message) {
        Producer producer = producerFactory.createProducer(ProducerBean.class);
        producer.sendOneway(message);
    }

    /**
     * 发送消息，异步Callback形式
     *
     * @param message      要发送的消息
     * @param sendCallback 发送完成要执行的回调函数
     */
    public void sendAsync(final Message message, final SendCallback sendCallback) {
        Producer producer = producerFactory.createProducer(ProducerBean.class);
        producer.sendAsync(message, sendCallback);

    }

    /**
     * 设置异步发送消息执行Callback的目标线程池。
     * <p>
     * 如果不设置，将使用公共线程池，仅建议执行轻量级的Callback任务，避免阻塞公共线程池
     * 引起其它链路超时。
     *
     * @param callbackExecutor 执行Callback的线程池
     */
    public void setCallbackExecutor(final ExecutorService callbackExecutor) {
        Producer producer = producerFactory.createProducer(ProducerBean.class);
        producer.setCallbackExecutor(callbackExecutor);

    }
}
