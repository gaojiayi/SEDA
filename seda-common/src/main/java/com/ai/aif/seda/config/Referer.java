package com.ai.aif.seda.config;

/**
 * @Title: 引用状态
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface Referer<T>
{
	/**
	 * 被调用数
	 * 
	 * @return
	 */
	int activeRefererCount();

	/**
	 * 是否可用
	 * 
	 * @return
	 */
	boolean isAvailable();
	
	/**
	 * 引用
	 * @return
	 */
	T getReferer();
}
