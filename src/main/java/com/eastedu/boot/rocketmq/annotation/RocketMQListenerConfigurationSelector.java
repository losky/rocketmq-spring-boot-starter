package com.eastedu.boot.rocketmq.annotation;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author luozhenzhong
 */
@Order
public class RocketMQListenerConfigurationSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{RocketMQBootstrapConfiguration.class.getName()};
    }
}
