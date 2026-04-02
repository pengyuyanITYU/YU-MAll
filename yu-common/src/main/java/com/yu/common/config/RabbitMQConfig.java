package com.yu.common.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 自定义配置。
 * 无 MQ 依赖的服务也会扫描到该配置类，因此这里不能直接引用 AMQP 类型。
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    @ConditionalOnClass(name = {
            "org.springframework.amqp.rabbit.core.RabbitTemplate",
            "org.springframework.amqp.support.converter.MessageConverter",
            "org.springframework.amqp.support.converter.Jackson2JsonMessageConverter"
    })
    public static BeanDefinitionRegistryPostProcessor rabbitMessageConverterRegistrar() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                if (registry.containsBeanDefinition("messageConverter")) {
                    return;
                }
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClassName(
                        "org.springframework.amqp.support.converter.Jackson2JsonMessageConverter");
                beanDefinition.getPropertyValues().add("createMessageIds", true);
                registry.registerBeanDefinition("messageConverter", beanDefinition);
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                // no-op
            }
        };
    }
}
