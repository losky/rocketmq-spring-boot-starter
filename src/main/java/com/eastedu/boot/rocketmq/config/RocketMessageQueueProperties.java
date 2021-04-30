package com.eastedu.boot.rocketmq.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Properties;

/**
 * @author luozhenzhong
 */
@ConfigurationProperties(prefix = RocketMessageQueueProperties.prefix)
public class RocketMessageQueueProperties {
    public static final String prefix = "spring.cloud.rocketmq";
    private MessageQueueProperties producer;
    private MessageQueueProperties consumer;

    public MessageQueueProperties getProducer() {
        return producer;
    }

    public void setProducer(MessageQueueProperties producer) {
        this.producer = producer;
    }

    public MessageQueueProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(MessageQueueProperties consumer) {
        this.consumer = consumer;
    }

    public Properties getProducerProperties() {
        Assert.hasText(producer.accessKey, "producer的accessKey不能为空");
        Assert.hasText(producer.secretKey, "producer的secretKey不能为空");
        Assert.hasText(producer.nameServer, "producer的nameServer不能为空");
        return createProperties(producer);
    }

    public Properties getConsumerProperties() {
        Assert.hasText(this.consumer.accessKey, "consumer的accessKey不能为空");
        Assert.hasText(this.consumer.secretKey, "consumer的secretKey不能为空");
        Assert.hasText(this.consumer.nameServer, "consumer的nameServer不能为空");
        return createProperties(consumer);
    }

    private Properties createProperties(MessageQueueProperties messageQueueProperties) {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, messageQueueProperties.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, messageQueueProperties.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, messageQueueProperties.nameServer);
        if (!CollectionUtils.isEmpty(messageQueueProperties.getProperties())) {
            for (Map.Entry<String, Object> entry : messageQueueProperties.getProperties().entrySet()) {
                String key = entry.getKey();
                properties.put(upperCaseFirst(key), entry.getValue());
            }
        }
        return properties;
    }

    private String upperCaseFirst(String key) {
        char[] chars = key.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') {
            chars[0] = (char) (chars[0] - 32);
        }
        return new String(chars);
    }

    public static class MessageQueueProperties {
        /**
         * 阿里云消息队里服务地址
         */
        protected String nameServer;
        /**
         * 阿里云消息队列访问key
         */
        protected String accessKey;
        /**
         * 阿里云消息队列访问秘钥
         */
        protected String secretKey;
        /**
         * 扩展配置，线程配置，超时配置，重试配置等，请参照阿里云官方文档
         */
        private Map<String, Object> properties;


        public String getNameServer() {
            return nameServer;
        }

        public void setNameServer(String nameServer) {
            this.nameServer = nameServer;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }


        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        @Override
        public String toString() {
            return "MessageQueueProperties{" +
                    "nameServer='" + nameServer + '\'' +
                    ", accessKey='" + accessKey + '\'' +
                    ", secretKey='" + secretKey + '\'' +
                    ", properties='" + properties + '\'' +
                    '}';
        }
    }


}
