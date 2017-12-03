package com.ai.aif.seda.event;

/**
 * @Title: 事件类型
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月16日
 * @Version: 1.0
 */
public enum EventType
{
	/**成功标识*/
	SUCCESS,
	
	/**心跳*/
	HEARTBEAT,
	
	/** 未知事件，系统不处理 */
	REQUEST_EVENT_NOT_SUPPORTED,
	
	/**消息类型不支持，不受理*/
	REQUEST_MSG_NOT_SUPPORTED,

	/** 系统异常事件 */
	SYSTEM_ERROR,

	/** 系统忙 */
	SYSTEM_BUSY,

	/** 调用第三方服务，处理事件 */
	CALL_THIRD_PARTY_SERVICE,
	
	/**刷新数据*/
	REFRESH_DATA,
	
	/**测试用*/
	TEST

}
