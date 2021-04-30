package com.eastedu.boot.rocketmq.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author luozhenzhong
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RocketMQListenerConfigurationSelector.class)
public @interface EnableRocketMQ {
}
