package com.eastedu.boot.rocketmq.annotation;

import com.eastedu.boot.rocketmq.endpoint.MethodRocketMQListenerEndpoint;
import com.eastedu.boot.rocketmq.endpoint.RocketMQListenerEndpointRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.env.Environment;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author luozhenzhong
 */
public class RocketMQListenerAnnotationBeanPostProcessor implements BeanPostProcessor,
        Ordered, BeanFactoryAware, SmartInitializingSingleton,
        BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String endpointNamePrefix = "rocketListenerEndpoint-";
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private final RocketMQListenerEndpointRegistry endpointRegistry = new RocketMQListenerEndpointRegistry();
    private AtomicInteger counter = new AtomicInteger(0);
    private BeanFactory beanFactory;
    private Environment environment;
    private BeanDefinitionRegistry registry;
    private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();
    private BeanExpressionContext expressionContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!this.nonAnnotatedClasses.contains(bean.getClass())) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Map<Method, RocketMQListener> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<RocketMQListener>) method -> {
                        RocketMQListener listenerMethod = findListenerAnnotations(method);
                        return listenerMethod;
                    });

            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(bean.getClass());
                this.logger.trace("No @RocketMQListener annotations found on bean type: " + bean.getClass());
            } else {
                // Non-empty set of methods
                for (Map.Entry<Method, RocketMQListener> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    processListener(entry.getValue(), method, bean, beanName);
                }
                this.logger.debug(annotatedMethods.size() + " @RocketMQListener methods processed on bean '"
                        + beanName + "': " + annotatedMethods);
            }
        }

        return bean;
    }

    protected void processListener(RocketMQListener listener, Method method, Object bean, String beanName) {
        Method methodToUse = checkProxy(method, bean);
        MethodRocketMQListenerEndpoint endpoint = new MethodRocketMQListenerEndpoint();
        endpoint.setMethod(methodToUse);
        endpoint.setBean(bean);
        processListener(endpoint, listener, methodToUse, bean, beanName);
    }

    private void processListener(MethodRocketMQListenerEndpoint endpoint, RocketMQListener listener, Method method, Object bean, String beanName) {
        endpoint.setId(getEndpointId(listener));
        endpoint.setGroupId(resolvePlaceholderProperties(listener.groupId(), "groupId"));
        endpoint.setTopic(resolvePlaceholderProperties(listener.topic(), "topic"));
        endpoint.setTags(resolvePlaceholderProperties(listener.tags(), "tags"));
        endpoint.setAutoCommit(listener.autoCommit());
        endpoint.setMessageType(listener.messageType());
        endpoint.setMessageModel(listener.messageModel());
        endpoint.setErrorHandler(listener.errorHandler());
        endpointRegistry.registerEndpoint(endpoint);
        endpointRegistry.setBeanFactory(this.beanFactory);
        endpointRegistry.postProcessBeanDefinitionRegistry(registry);
    }

    private String getEndpointId(RocketMQListener listener) {
        String endpointId = endpointNamePrefix;
        if (StringUtils.hasText(listener.id())) {
            endpointId += listener.id() + "-";
        }
        return endpointId + counter.incrementAndGet();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.resolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
            this.expressionContext = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory,
                    null);
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.endpointRegistry.setBeanFactory(this.beanFactory);
        this.endpointRegistry.afterPropertiesSet();
    }

    private void addFormatters(FormatterRegistry registry) {
        for (Converter<?, ?> converter : getBeansOfType(Converter.class)) {
            registry.addConverter(converter);
        }
        for (GenericConverter converter : getBeansOfType(GenericConverter.class)) {
            registry.addConverter(converter);
        }
        for (org.springframework.format.Formatter<?> formatter : getBeansOfType(Formatter.class)) {
            registry.addFormatter(formatter);
        }
    }

    private <T> Collection<T> getBeansOfType(Class<T> type) {
        if (this.beanFactory instanceof ListableBeanFactory) {
            return ((ListableBeanFactory) this.beanFactory)
                    .getBeansOfType(type)
                    .values();
        } else {
            return Collections.emptySet();
        }
    }


    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    private String resolvePlaceholderProperties(String property, String attribute) {
        if (!StringUtils.hasText(property)) {
            throw new IllegalArgumentException("The [" + attribute + "] must not be null. ");
        }
        return resolveExpressionAsString(property, attribute);
    }


    private Method checkProxy(Method methodArg, Object bean) {
        Method method = methodArg;
        if (AopUtils.isJdkDynamicProxy(bean)) {
            try {
                // Found a @RocketMQListener method on the target class for this JDK proxy ->
                // is it also present on the proxy itself?
                method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
                Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
                for (Class<?> iface : proxiedInterfaces) {
                    try {
                        method = iface.getMethod(method.getName(), method.getParameterTypes());
                        break;
                    } catch (@SuppressWarnings("unused") NoSuchMethodException noMethod) {
                        // NOSONAR
                    }
                }
            } catch (SecurityException ex) {
                ReflectionUtils.handleReflectionException(ex);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(String.format(
                        "@RocketMQListener method '%s' found on bean target class '%s', " +
                                "but not found in any interface(s) for bean JDK proxy. Either " +
                                "pull the method up to an interface or switch to subclass (CGLIB) " +
                                "proxies by setting proxy-target-class/proxyTargetClass " +
                                "attribute to 'true'", method.getName(),
                        method.getDeclaringClass().getSimpleName()), ex);
            }
        }
        return method;
    }


    private String resolveExpressionAsString(String value, String attribute) {
        Object resolved = resolveExpression(value);
        if (resolved instanceof String) {
            return (String) resolved;
        } else if (resolved != null) {
            throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
                    + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
        }
        return null;
    }

    private Object resolveExpression(String value) {
        return this.resolver.evaluate(resolve(value), this.expressionContext);
    }

    /**
     * Resolve the specified value if possible.
     *
     * @param value the value to resolve
     *
     * @return the resolved value
     *
     * @see ConfigurableBeanFactory#resolveEmbeddedValue
     */
    private String resolve(String value) {
        if (this.beanFactory != null && this.beanFactory instanceof ConfigurableBeanFactory) {
            return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
        }
        return value;
    }

    private RocketMQListener findListenerAnnotations(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, RocketMQListener.class);
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

