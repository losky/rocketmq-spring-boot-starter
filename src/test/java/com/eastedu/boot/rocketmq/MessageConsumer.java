package com.eastedu.boot.rocketmq;

import com.aliyun.openservices.ons.api.Message;
import com.eastedu.boot.rocketmq.annotation.MessageType;
import com.eastedu.boot.rocketmq.annotation.RocketMQListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageConsumer {

    @RocketMQListener(id = "12", topic = "test_resource_center_202104281", groupId = "test_resource_center_202104281", messageType = MessageType.Batch)
    public boolean onMessage2(List<Message> message) {
        System.out.println("===============================");
        System.out.println(message);
        return true;
    }

    @RocketMQListener(id = "12", topic = "test_resource_center_202104281", groupId = "test_resource_center_202104281", messageType = MessageType.Batch)
    public boolean onMessage3(List<Message> message, String a) {
        System.out.println("===============================");
        return true;
    }

    @RocketMQListener(id = "12", topic = "test_resource_center_202104281", groupId = "test_resource_center_202104281", messageType = MessageType.Batch)
    public boolean onMessage4(String b, List<Message> message, String a) {
        System.out.println("===============================");
        return true;
    }
}
