package com.ai.aif.seda.heartbeat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳分两个层面：1、协议层 2、业务层
 * 
 * @Title: 心跳接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface IHeartbeat<T>
{
	static Map<String /* ip:prot */, AtomicInteger> RUN_STATE = new HashMap<String, AtomicInteger>();

	/**
	 * 对端发送心跳
	 * 
	 * @param addr address
	 * @param request Request
	 * @param timeoutMillis timeoutMillis
	 * @return SedaCommand
	 */
	void ping(List<T> addrs, long timeoutMillis);

}
