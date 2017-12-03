package com.ai.aif.seda.common;


/**
 * 该与common下类一样，原因是；为了轻量，不依赖common，common同时也依赖了其它jar
 * @Title: 
 * @Description: 
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public enum ReportEvent
{
	/** 注册 */
	REGISTER,

	/** Queue */
	PUT_QUEUE,

	/** 线程组 */
	PUT_THREADGROUP,

	/** 查询服务列表 */
	QUERY_SERVER,

	/** 查询队列 */
	QUERY_QUEUE,

	/** 检查线程 */
	QUERY_THREAD,

	/** 消息流水 */
	QUERY_MESSAGE_FLOW,

	/** 更新消息流水 */
	PUT_MESSAGE_FLOW;

	/**
	 * 匹配上传类型
	 * @param type
	 * @return ReportEvent
	 */
	public static ReportEvent toReportEvent(String type)
	{
		if (REGISTER.name().equalsIgnoreCase(type))
		{
			return REGISTER;
		}
		else if (PUT_QUEUE.name().equalsIgnoreCase(type))
		{
			return PUT_QUEUE;
		}
		else if (PUT_THREADGROUP.name().equalsIgnoreCase(type))
		{
			return PUT_THREADGROUP;
		}
		else if (QUERY_SERVER.name().equalsIgnoreCase(type))
		{
			return QUERY_SERVER;
		}
		else if (QUERY_THREAD.name().equalsIgnoreCase(type))
		{
			return QUERY_THREAD;
		}
		else if (QUERY_QUEUE.name().equalsIgnoreCase(type))
		{
			return QUERY_QUEUE;
		}
		else if (QUERY_MESSAGE_FLOW.name().equalsIgnoreCase(type))
		{
			return QUERY_MESSAGE_FLOW;
		}
		else if (PUT_MESSAGE_FLOW.name().equalsIgnoreCase(type))
		{
			return PUT_MESSAGE_FLOW;
		}
		else
		{
			return null;
		}
	}
}
