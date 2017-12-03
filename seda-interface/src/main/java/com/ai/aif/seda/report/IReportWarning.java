package com.ai.aif.seda.report;

import java.util.List;
import java.util.Map;

/**
 * @Title: 上报告警接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface IReportWarning
{
	/***
	 * 获取SEDA的系统资源<br>
	 * 1、线程 、队列<br>
	 * @return
	 */
	public Map<String/* queue id */, Map<String, Object>> getSedaQueues();

	/**
	 * 获取SEDA的线程资源
	 * @return
	 */
	public Map<String/* queue id */, Map<String, Map<String, Object>>> getSedaThreadGroup();

	/***
	 * 注册接口
	 * @return
	 */
	public Map<String /* ip:port */, List<Map<String, Object>/* Servers info */>> register();

}
