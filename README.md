SEDA优化与挑战：
	1）基于ZK，实现透明化路由；
	2）使用disruptor极速队列替代LinkedBlockingQueue；
	3）使用hession或google PB或JBOSS Marshing 替代java的序列化；
	4）优化负载，基于ZK，定时收性能KPI更新server Node权重，结合加权随机或轮询，实现动态负载；
	6）优化代码结构