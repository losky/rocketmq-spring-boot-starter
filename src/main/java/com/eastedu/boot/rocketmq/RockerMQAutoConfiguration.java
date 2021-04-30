package com.eastedu.boot.rocketmq;


import com.aliyun.openservices.ons.api.Admin;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.eastedu.boot.rocketmq.annotation.EnableRocketMQ;
import com.eastedu.boot.rocketmq.config.RocketMQListenerConfigUtils;
import com.eastedu.boot.rocketmq.config.RocketMessageQueueProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author luozhenzhong
 */
@Configuration()
@ConditionalOnClass(Admin.class)
public class RockerMQAutoConfiguration {

    @Configuration
    @EnableConfigurationProperties(RocketMessageQueueProperties.class)
    @ConditionalOnProperty(prefix = "spring.cloud.rocketmq.producer", value = "name-server")
    public static class ProducerConfiguration {
        private final RocketMessageQueueProperties properties;

        public ProducerConfiguration(RocketMessageQueueProperties properties) {
            this.properties = properties;
        }

        @Bean
        public RocketMQTemplate rocketMqTemplate(List<Admin> producers) {
            ProducerFactory producerFactory = new ProducerFactory(producers);
            return new RocketMQTemplate(producerFactory);
        }


        @Bean(initMethod = "start", destroyMethod = "shutdown")
        @ConditionalOnMissingBean(Producer.class)
        public Producer producer() {
            ProducerBean producer = new ProducerBean();
            producer.setProperties(properties.getProducerProperties());
            return producer;
        }

        @Bean(initMethod = "start", destroyMethod = "shutdown")
        @ConditionalOnMissingBean(OrderProducer.class)
        public OrderProducer orderedProducer() {
            OrderProducerBean producer = new OrderProducerBean();
            producer.setProperties(properties.getProducerProperties());
            return producer;
        }

        //    @Bean(initMethod = "start", destroyMethod = "shutdown")
//    public TransactionProducerBean transactionProducer() {
//        TransactionProducerBean producer = new TransactionProducerBean();
//        producer.setProperties(properties.getProducerProperties());
//        return producer;
//    }
    }

    @Configuration()
    @EnableRocketMQ()
    @ConditionalOnMissingBean(name = RocketMQListenerConfigUtils.ROCKET_MQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    static class EnableRocketMQConfiguration {

    }
}
