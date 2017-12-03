package com.ai.aif.seda.exception;

import com.ai.aif.seda.common.SedaConstants;

/**
 * @Title: RPC调用中，客户端发送请求失败，抛出此异常
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月8日
 * @Version: 1.0
 */
public class RemotingSendRequestException extends RemotingException
{
	private static final long serialVersionUID = 5391285827332471674L;

	public RemotingSendRequestException(String addr)
	{
		this(addr, null);
	}

	public RemotingSendRequestException(String addr, Throwable cause)
	{
		super(SedaConstants.SEND_REQUEST_EXCEPTION, "send request to <" + addr + "> failed", cause);
	}
}
