package com.eastedu.boot.rocketmq.annotation;

import com.eastedu.boot.rocketmq.config.RocketMQListenerConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * RocketMQ 启动配置类
 *
 * @author luozhenzhong
 */
public class RocketMQBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(
                RocketMQListenerConfigUtils.ROCKET_MQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)) {

            registry.registerBeanDefinition(RocketMQListenerConfigUtils.ROCKET_MQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
                    new RootBeanDefinition(RocketMQListenerAnnotationBeanPostProcessor.class));
        }
    }

}
