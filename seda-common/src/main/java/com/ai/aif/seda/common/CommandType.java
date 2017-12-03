package com.ai.aif.seda.common;

/**
 * @Title: 请求类型
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月21日
 * @Version: 1.0
 */
public enum CommandType
{
	/**
	 * 同步请求
	 */
	SYNC_REQUEST,

	/**
	 * 异步请求
	 */
	ASYNC_REQUEST,

	/**
	 * 单向请求
	 */
	ONEWAY_REQUEST,

	/**
	 * 响应
	 */
	RESPONSES

}
