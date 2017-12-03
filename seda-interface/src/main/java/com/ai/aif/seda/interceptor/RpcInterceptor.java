package com.ai.aif.seda.interceptor;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.message.ResponseFuture;

/**
 * @Title:拦截器
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface RpcInterceptor
{
	/**
	 * 拦截请求,根据业务场景考虑是否异步执行（如果不影响主事件，可以不考虑异步）
	 * @param addr
	 * @param request
	 */
	public void beforeRequest(final SedaCommand request);

	/**
	 * 拦截响应
	 * @param addr
	 * @param request
	 * @param response
	 */
	public void afterResponse(String addr, final SedaCommand request, final SedaCommand response);

	/**
	 * 拦截响应
	 * @param request
	 * @param response
	 */
	public void afterResponse(final SedaCommand request, final ResponseFuture respFuture);

}
