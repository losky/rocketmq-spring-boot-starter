# 版本说明

## 1.1.0

* 修复手动提交失败的问题
* 修复多个类中的方法应用@RocketMQListener时出现Bean冲突的问题
* 新增配置enabled开关

## 1.0.0

* 对阿里云consumer进行封装，对外暴露一个@RocketMQListener注解类，通过注解实现消息消费。
    * 支持TAG模式的expression，
    * topic和groupId支持占位符参数配置
    * 注解需要遵从订阅关系一致
    * 支持手动和自动提交
    * 支持普通消息、顺序消息、批量消息，暂不支持事务消息
    * 应用注解的方法参数类型中必须存在com.aliyun.openservices.ons.api.Message
* 阿里云的producer封装成了RocketMQTemplate， 目前支持发送普通消息、顺序消息，暂不支持事务消息