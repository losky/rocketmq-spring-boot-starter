package com.eastedu.boot.rocketmq.config;

/**
 * Configuration constants for internal sharing across subpackages.
 *
 * @author zhang.zhi.shuai
 */
public abstract class RocketMQListenerConfigUtils {

    /**
     * The bean name of the internally managed rocketMQ listener annotation processor.
     */
    public static final String ROCKET_MQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.springframework.rocketmq.config.internalRocketMQListenerAnnotationProcessor";

}
