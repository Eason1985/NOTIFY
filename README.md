消息通信中间件

V1.0

概念：

1、messageType 消息名称。消息的最小分类属性，每个消息必须有一个可供订阅和发布的名称，并且在topic内唯一。 多个同类的messageType可以整合

2、topic
    某一类消息的集合。可以按照业务系统分类，也可以按照消息大类分类。
    一个topic可以包含多个messageType。
    topic必须申请拿到，不允许开发自行创建

3、groupId
    应用ID/集群ID。
    sub/pub模式，同一个group，同一消息只有一台机器能处理。
    广播模式，不同group，同一消息每个group都会有一台机器收到该消息。

4、死信队列
    每个messageType都会有一个DLQ队列，消息消费失败到一定次数怎会进入死信队列


组件： 

sender：支持普通消息发送、消息延迟发送。 

listener：支持单一消息接收，支持单一topic下所有消息接收。 

默认情况下，listener消费异常，MQ会重复发送X(可配置)次。如果消费不掉，则进入死信队列。支持死信重发

RabbitMQ 需安装如下plugin：
 rabbitmq_shovel 
 rabbitmq_shovel_management
 
V1.1

sender具备了message comfirm的能力，确保消息不会因为投递过程失败而丢失

新增了本地存储功能，增强了因为消费消息不成功导致消息丢失的情况

新增重发次数配置功能，现在可以自行配置消息在异常情况下的重发次数，如果还不成功，则进入死信，同时提供了回调接口，通知业务处理

现在的channel是可以复用的，修正了1.0版本中因为不停创建channel导致的资源浪费

增加了channel的心跳检查，确保channel的可用性

增加了listener检查，确保了唯一性。

 
 
