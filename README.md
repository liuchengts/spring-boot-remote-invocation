##  spring-boot-remote-invocation
基于springboot的一个远程rpc框架1.0

## 能做什么
* 能提供2种方式的远程rpc调用，主动和被动
* 能自定义功能，例如扩展分布式任务协调、分布式调度一致性等

## 与dubbo相比有什么不同
* 序列化协议上使用hessian（往后可以扩展为其他方式的序列化），dubbo也支持hessian，但是dubbo支持更多种协议
* 使用方式上与dubbo一致，监控体系目前远远不如dubbo完善
* 去中心化，相比dubbo没有注册中心，由服务完成自我发现

## 去中心化的网络通信
* 定义leaderPort角色：当服务启动时尝试自己在leaderPort上成为leader，一旦失败就降级为普通服务。只有当leaderPort相同的时候，才会在一个rpc组中，
也就是说，允许同一个网络中出现不同组的leader，不同组的服务相互独立，无法访问与通信
* leader的职责：理论上每台物理机上只允许出现一个固定端口的leader，服务都会向本机leader注册，本机leader收到注册后会向当前所有服务（包括非本机服务）
  进行广播
* leader的机制：leader与连接的服务之间有心跳连接保持，一旦leader失去连接，会由当前机器上的所有服务对leader角色进行竞争（如果leader是假死或者双方出现脑裂，
  那么leader资源在未释放前，本机的其他服务是无法竞争到的），服务只能竞争自己本机的leader，无权竞争远程leader，
  竞争完成后会向所有远程leader及本机服务发送消息同步指令，获取最新的服务信息进行广播
* 消息发送机制：在消息发送时，如果失败会放入消息待发送队列，发送队列会在通讯连接成功后自动启动单独的线程执行。另外心跳连接不受此机制影响
* 路由规则：路由服务由消息处理器进行触发，携带服务版本号进行更新校验，只更新最新的版本，同时进行远程服务注册，如当前服务需要此路由服务的话会
  进行远程服务资源注入。路由唯一key为 ip:port
* rpc服务的可用性检测：由单独的线程进行rpc服务资源的通讯检测，并不直接调用服务。一旦发现通讯异常会将当前服务移除路由缓存及rpc服务缓存，
  并且广播当前最新的路由信息
* rpc服务负载规则： rpc远程服务是有序的，先进先取的原则
* rpc服务的调用： rpc服务消费者直接找生产者进行通讯发生调用，此时并不需要leader存在，即使leader挂掉。
## 待完善功能
* 独立于当前项目，开发监控体系，当前服务作为底层支持，进行服务治理
* 升级leader网络层，增加一个可配置的代理网关，实现跨网络或者跨leader组的rpc调用