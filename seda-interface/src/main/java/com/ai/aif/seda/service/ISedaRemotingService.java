package com.ai.aif.seda.service;

import com.ai.aif.seda.interceptor.RpcInterceptor;

/**
 * @Title:通信服务接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface ISedaRemotingService
{
	/**
	 * 启动服务
	 */
	void startup();

	/**
	 * 关闭服务
	 */
	void shutdown();

	/**
	 * 注册拦截器
	 * @param rpc
	 */
	void registerInterceptor(RpcInterceptor rpc);

}
