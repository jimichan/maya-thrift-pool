# maya-thrift-pool
a thrift pool


## 功能列表

1. 指定多个服务地址
2. 负载均衡
3. 自动检测后段存活
5. 如果服务离线，自动检测如果恢复那么自动上线
6. 如果服务故障，自动剔除
7. 连接池配置
8. 使用javassist字节码生成实现iface类，性能比反射调用高。且iface的实现是多线程安全、只需要单例


```
ThriftConnectionPool pool = new ThriftConnectionPool(
				ThriftConnectionPool.config);
final Iface face = SimpleClientProxyFactory.
				makeClientFramedTransport(SegService.Iface.class, new SegService.Client.Factory(),
						"127.0.0.1:9091,127.0.0.1:9090", pool, new TBinaryProtocol.Factory());
						
face.dosomeing();
face.dosomeing2();
```