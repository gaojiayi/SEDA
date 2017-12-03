package com.ai.aif.seda;

import com.ai.aif.seda.message.ResponseFuture;

/**
 * @Title: 异步调用应答接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月16日
 * @Version: 1.0
 */
public interface InvokeCallBack
{
	/**
	 * 操作完毕
	 * @param responseFuture
	 */
	public void operationComplete(final ResponseFuture responseFuture);
}
