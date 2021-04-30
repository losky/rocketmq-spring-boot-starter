package com.eastedu.boot.rocketmq.endpoint;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.filter.ExpressionType;
import com.eastedu.boot.rocketmq.adaptor.MethodParameterHolder;
import com.eastedu.boot.rocketmq.annotation.MessageMode;
import com.eastedu.boot.rocketmq.annotation.MessageType;
import com.eastedu.boot.rocketmq.config.RocketMessageQueueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.util.Assert;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置信息注册器
 *
 * @author luozhenzhong
 */
public class RocketMQListenerEndpointRegistry implements BeanFactoryAware, InitializingBean, BeanDefinitionRegistryPostProcessor {

    private final List<RocketMQListenerEndpoint> endpoints = new ArrayList<>();
    private boolean startImmediately = false;
    private BeanFactory beanFactory;
    private BeanDefinitionRegistry registry;
    private RocketMessageQueueProperties properties;

    public void registerEndpoint(RocketMQListenerEndpoint endpoint) {
        Assert.notNull(endpoint, "Endpoint must be set");
        Assert.hasText(endpoint.getId(), "Endpoint id must be set");
        // Factory may be null, we defer the resolution right before actually creating the container
        synchronized (this.endpoints) {
            if (this.startImmediately) {
                // Register and start immediately
//                this.endpointRegistry.registerListenerContainer(descriptor.endpoint,
//                        resolveContainerFactory(descriptor), true);
            } else {
                this.endpoints.add(endpoint);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        this.properties = this.beanFactory.getBean(RocketMessageQueueProperties.class);
        registerAllEndpoints();
    }

    protected void registerAllEndpoints() {
        synchronized (this.endpoints) {
            Map<ConsumerProperties, List<RocketMQListenerEndpoint>> map = this.endpoints.stream()
                    .collect(Collectors.groupingBy(rocketMQListenerEndpoint -> {
                        ConsumerProperties consumerProperties = new ConsumerProperties();
                        consumerProperties.setGroup(rocketMQListenerEndpoint.getGroupId());
                        consumerProperties.setMessageType(rocketMQListenerEndpoint.getMessageType());
                        consumerProperties.setMessageMode(rocketMQListenerEndpoint.getMessageModel());
                        return consumerProperties;
                    }));
            for (Map.Entry<ConsumerProperties, List<RocketMQListenerEndpoint>> entry : map.entrySet()) {
                registerListener(entry.getKey(), entry.getValue());
            }
            // trigger immediate startup
            this.startImmediately = true;
        }
    }

    private void registerListener(ConsumerProperties consumerProperties, List<RocketMQListenerEndpoint> endpoints) {
        MessageType messageType = consumerProperties.getMessageType();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(messageType.getConsumerClass());
        builder.addPropertyValue("properties", getProperties(consumerProperties));

        Map<Subscription, ?> subscriptionTable = createSubscriptions(endpoints, messageType.getMessageListenerClass(), messageType.isBatch());

        builder.addPropertyValue("subscriptionTable", subscriptionTable);
        builder.setInitMethodName("start");
        builder.setDestroyMethodName("shutdown");
        builder.setLazyInit(false);

        String beanName = messageType.getConsumerClass().getSimpleName();
        this.registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        this.beanFactory.getBean(beanName);
    }

    private <T> Map<Subscription, T> createSubscriptions(List<RocketMQListenerEndpoint> endpoints, Class<T> messageListenerClass, boolean batch) {
        Map<Subscription, T> subscriptionTable = new HashMap<>(2);
        for (RocketMQListenerEndpoint endpoint : endpoints) {
            Subscription subscription = new Subscription();
            subscription.setTopic(endpoint.getTopic());
            subscription.setType(ExpressionType.TAG);
            subscription.setExpression(endpoint.getTags());

            MethodRocketMQListenerEndpoint listenerEndpoint = (MethodRocketMQListenerEndpoint) endpoint;
            Object instance = ProxyFactory.getInstance(messageListenerClass, new RocketMQMessageListenerProxy(listenerEndpoint.getBean(), listenerEndpoint.getMethod(), endpoint.isAutoCommit(), batch));
            subscriptionTable.put(subscription, (T) instance);
        }
        return subscriptionTable;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private Properties getProperties(ConsumerProperties consumerProperties) {
        Properties properties = consumerProperties.getProperties();
        properties.putAll(this.properties.getConsumerProperties());
        return properties;
    }

    static class ConsumerProperties {
        private MessageType messageType;
        private MessageMode messageMode;
        private String group;

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public MessageMode getMessageMode() {
            return messageMode;
        }

        public void setMessageMode(MessageMode messageMode) {
            this.messageMode = messageMode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConsumerProperties that = (ConsumerProperties) o;
            return getMessageType() == that.getMessageType() &&
                    Objects.equals(getGroup(), that.getGroup());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMessageType(), getGroup());
        }

        public Properties getProperties() {
            Properties properties = new Properties();
            properties.setProperty(PropertyKeyConst.GROUP_ID, this.group);
            properties.setProperty(PropertyKeyConst.MessageModel, this.messageMode.getMode());
            return properties;
        }
    }

    static class RocketMQMessageListenerProxy implements InvocationHandler {
        private final Logger logger = LoggerFactory.getLogger(RocketMQMessageListenerProxy.class);
        private final Object bean;
        private final Method method;
        private final boolean autoCommit;
        private final int parameterCount;
        private MethodParameterHolder parameterHolder;

        private Class returnType;


        RocketMQMessageListenerProxy(Object bean, Method method, boolean autoCommit, boolean batch) {
            this.bean = bean;
            this.method = method;
            int parameterCount = this.method.getParameterCount();
            if (parameterCount == 0) {
                throw new RuntimeException("接受消息的Method '" + bean.getClass().getName() + "." + method.getName() + "' 参数个数必须大于0");
            }

            this.parameterCount = parameterCount;
            Type[] parameterTypes = this.method.getGenericParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Type parameterType = parameterTypes[i];
                if (parameterType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) parameterType;
                    Type[] arguments = parameterizedType.getActualTypeArguments();
                    if (parameterizedType.getRawType().getTypeName().equals(List.class.getName())
                            && arguments.length == 1
                            && arguments[0].getTypeName().equals(Message.class.getName())) {
                        parameterHolder = new MethodParameterHolder(((ParameterizedType) parameterType).getRawType(), i);
                        parameterHolder.addTypedParameter(arguments[0]);
                    }
                } else if (parameterType.getTypeName().equals(Message.class.getName())) {
                    parameterHolder = new MethodParameterHolder(parameterType, i);
                }
            }

            if (parameterHolder == null || !parameterHolder.check(batch)) {
                throw getMethodParameterErrorException(method, batch);
            }

            this.autoCommit = autoCommit;
            if (!autoCommit) {
                Class returnType = getReturnType(this.method);
                if (!returnType.isAssignableFrom(boolean.class)
                        || !returnType.isAssignableFrom(Boolean.class)) {
                    throw new RuntimeException("设置消息为手动提交时，Method '" + method.getName() + "' 的返回值类型必须为boolean");
                }
            }
        }

        private RuntimeException getMethodParameterErrorException(Method method, boolean batch) {
            String s = batch ? "List<com.aliyun.openservices.ons.api.Message>" : "com.aliyun.openservices.ons.api.Message";
            return new RuntimeException("接受消息的Method '" + bean.getClass().getName() + "." + method.getName() + "' 必须包含" + s + "类型的参数");
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")
                    || method.getName().equals("equals")
                    || method.getName().equals("hashCode")) {
                return method.invoke(bean, args);
            } else {
                /**
                 * 阿里云RocketMQ监听器方法的返回值都是枚举值，这里直接获取枚举
                 */
                Object[] returnValues = getReturnType(method).getEnumConstants();
                boolean result = invoke(args[0]);
                if (result) {
                    return returnValues[0];
                } else {
                    return returnValues[1];
                }
            }

        }

        private boolean invoke(Object arg) {
            try {
                Object result;
                int parameterIndex = parameterHolder.getParameterIndex();
                /**
                 * 定位正确参数位置
                 */
                if (parameterCount > 1) {
                    Object[] args = new Object[parameterCount];
                    for (int i = 0; i < parameterCount; i++) {
                        if (i == parameterIndex) {
                            args[i] = arg;
                        } else {
                            args[i] = null;
                        }
                    }
                    result = method.invoke(bean, args);
                } else {
                    result = method.invoke(bean, arg);
                }
                return this.autoCommit ? true : (boolean) result;
            } catch (Exception e) {
                logger.error("消费消息失败: {}", e);
                return false;
            }
        }

        private Class getReturnType(Method method) {
            if (this.returnType == null) {
                this.returnType = method.getReturnType();
            }
            return this.returnType;
        }
    }

    static class ProxyFactory {

        public static final <T> Object getInstance(Class<T> clazz, InvocationHandler handler) {
            return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);

        }
    }
}
