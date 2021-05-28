# 使用说明


## 依赖包导入

1. 添加依赖管理

```xml
<dependencies>
    <groupId>com.eastedu.boot</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependencies>
```
## 配置
主要配置producer和consumer的链接信息，其他的扩展配置可在properties中进行配置(扩展配置的信息请参照[阿里云官方参数说明](https://help.aliyun.com/product/29530.html?spm=a2c4g.11186623.6.540.34bb5a91haeVi3))

```yaml
spring:
  cloud:
    rocketmq:
      producer:
        name-server: a
        access-key: a
        secret-key: a
        properties:
          xxxx: xx
      consumer:
        name-server: a
        access-key: a
        secret-key: a
        properties:  
          SuspendTimeMillis: 100       
          MaxReconsumeTimes: 20                 


```

## 消息发送

阿里云的producer封装成了RocketMQTemplate， 目前支持发送普通消息、顺序消息，暂不支持事务消息

### 示例1
```java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.eastedu.boot.rocketmq.RocketMQTemplate;

@Service
public class ProducerDemo{
     
    private final RocketMQTemplate rocketMQTemplate;
    
    public ProducerDemo(RocketMQTemplate rocketMQTemplate){
        this.rocketMQTemplate=rocketMQTemplate;
    }
 
    public void send() throws InterruptedException {
        rocketMQTemplate.sendAsync(new Message("topic", "tga", "message".getBytes()), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("发送成功 ===》 " + sendResult.toString());
                }

                @Override
                public void onException(OnExceptionContext context) {
                    System.err.println("发送失败 ===》 " + context.toString());
                }
        });

    }
}
```

## 消息接收
对阿里云consumer进行封装，对外暴露一个@RocketMQListener注解类，通过注解实现消息消费。

| 配置名称     | 是否必填 | 描述                                                         |
| ------------ | -------- | ------------------------------------------------------------ |
| id           | 否       | 唯一标识，为空会自动生成                                     |
| **topic**    | **是**   | 消息队列主题ID，支持占位符参数                                               |
| **groupId**  | **是**   | 消费者集群ID，支持占位符参数                                                  |
| tags         | 否       | 路由作用，有SQL和TAG两种模式，目前统一使用TAG模式。默认值为全部（*），支持占位符参数  |
| autoCommit   | 否       | 是否自动提交，默认为true。 如果使用手动提交，接受消息的方法必须返回boolean类型 |
| messageType  | 否       | 消息类型，有普通消息、顺序消息、批量消息、事务消息（暂不支持）。 默认为普通消息 |
| messageModel | 否       | 订阅方式，有广播方式和集群方式，默认为集群方式。 广播方式弊端较多，详情见阿里云官网[集群消费和广播消费](https://help.aliyun.com/document_detail/43163.htm?spm=a2c4g.11186623.2.7.41cf5eaeWUfHUm#concept-2047071) |
| errorHandler | 否       | 异常处理器，发生异常统一处理逻辑。暂未实现                   |
| fetchType    | 否       | 消息拉取方式，有PUSH和PULL（暂未实现），默认为PUSH。         |

**注意：**
* 接受消息的方法参数必须存在com.aliyun.openservices.ons.api.Message类型的参数，否则无法接受消息。    
* 如果是批量模式，则方法参数必须包含List<com.aliyun.openservices.ons.api.Message>类型的参数，否则无法接受消息。  

### 示例1

```java

import java.util.List;
import com.aliyun.openservices.ons.api.Message;
import com.eastedu.boot.rocketmq.annotation.RocketMQListener;
import org.springframework.stereotype.Service;
import com.aliyun.openservices.ons.api.Message;

@Service
public class MessageConsumer {
     
    /**
    *
    * 批量消费
    **/
    @RocketMQListener(topic = "test_resource_center_202104281", groupId = "test_resource_center_202104281", messageType = MessageType.Batch)
    public void onMessage2(List<Message> message) {
        System.out.println("===============================");
        System.out.println(message);
    }

}
```

### 示例2

```java
import java.util.List;
import com.aliyun.openservices.ons.api.Message;
import com.eastedu.boot.rocketmq.annotation.RocketMQListener;
import org.springframework.stereotype.Service;
import com.aliyun.openservices.ons.api.Message;

@Service
public class MessageConsumer {
    
    /**
    * 普通消息
    * 手动提交
    **/
    @RocketMQListener(topic = "test_resource_center_202104281", groupId = "test_resource_center_202104281", batch = false)
    public boolean onMessage3(Message message, String a) {
        System.out.println("===============================");
        return true;
    }

}

```

### 示例3

```java
import java.util.List;
import com.aliyun.openservices.ons.api.Message;
import com.eastedu.boot.rocketmq.annotation.RocketMQListener;
import org.springframework.stereotype.Service;
import com.aliyun.openservices.ons.api.Message;

@Service
public class MessageConsumer {

    /**
    *
    * 顺序消息
    **/
    @RocketMQListener(topic = "test_resource_center_202104281", groupId = "test_resource_center_202104281", messageType = MessageType.Ordered)
    public void onMessage4(String b,Message message, String a) {
        System.out.println("===============================");
    }
}
```