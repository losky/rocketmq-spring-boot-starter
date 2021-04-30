package com.eastedu.boot.rocketmq.endpoint;

import java.lang.reflect.Method;

/**
 * @author luozhenzhong
 */
public class MethodRocketMQListenerEndpoint extends AbstractRocketMQListenerEndpoint {

    private Object bean;

    private Method method;

    public MethodRocketMQListenerEndpoint() {
    }

    public MethodRocketMQListenerEndpoint(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
