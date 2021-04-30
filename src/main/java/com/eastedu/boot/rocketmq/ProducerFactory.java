package com.eastedu.boot.rocketmq;

import com.aliyun.openservices.ons.api.Admin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luozhenzhong
 */
public class ProducerFactory {
    private final List<String> producerNames = new ArrayList<>();
    private final List<Admin> producers;

    public ProducerFactory(List<Admin> producers) {
        this.producers = producers;
        for (Admin producer : producers) {
            this.producerNames.add(producer.getClass().getSimpleName());

        }
    }

    public <T extends Admin> T createProducer(Class<T> clazz) {
        String name = clazz.getSimpleName();
        if (producerNames.contains(name)) {
            int i = producerNames.indexOf(name);
            return (T) producers.get(i);
        }
        throw new RuntimeException("未主配置该类型'" + name + "'的Producer");
    }

}
