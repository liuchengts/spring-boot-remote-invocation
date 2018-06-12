##  spring-boot-remote-invocation
基于springboot的一个远程rpc框架1.0

#能做什么
* 能提供2种方式的远程rpc调用，主动和被动
* 能自定义功能，例如扩展分布式任务协调

#与dubbo相比有什么不同
* 序列化协议上使用hessian（往后可以扩展为其他方式的序列化），dubbo也支持hessian，但是dubbo支持更多种协议
* 使用方式上与dubbo一致，监控体系目前远远不如dubbo完善
* 去中心化，相比dubbo没有注册中心是，由服务完成自我发现

#去中心化的网络通信
* 定义leaderPort角色：当服务启动时尝试自己在leaderPort上成为leader，一旦失败就降级为普通服务。
* leader的职责：理论上每台物理机上只允许出现一个leader，服务都会向本机leader注册，本机leader收到注册后会向当前所有服务（包括非本机服务）
  进行广播
* leader的机制：leader与连接的服务之间有心跳连接保持，心跳失败超过30s，会尝试由某个服务成为leader，服务只能竞争自己本机的leader，无权竞争
  远程leader，避免本机出现脑裂
* 消息发送机制：在消息发送时，如果失败会放入消息待发送队列，发送队列会在通讯连接成功后自动启动单独的线程执行。另外心跳连接不受此机制影响，为
  单独的固定发送
* 路由规则：路由服务由消息处理器进行触发，携带服务版本号进行更新校验，只更新最新的版本，同时进行远程服务注册，如当前服务需要此路由服务的话会
  进行远程服务资源注入。路由唯一key为 ip:port
  
#待完善功能
* 网络层代码优化，加快服务暴露发现的时间
* 开发监控相关模块，进行服务治理
* 进行内部代码优化