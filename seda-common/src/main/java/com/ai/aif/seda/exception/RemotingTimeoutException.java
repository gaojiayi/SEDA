package com.ai.aif.seda.exception;

import com.ai.aif.seda.common.SedaConstants;

/**
 * @Title: RPC调用超时异常
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月8日
 * @Version: 1.0
 */
public class RemotingTimeoutException extends RemotingException
{

	private static final long serialVersionUID = 4106899185095245979L;

	public RemotingTimeoutException(String message)
	{
		super(SedaConstants.TIMEOUT, message);
	}

	public RemotingTimeoutException(String addr, long timeoutMillis)
	{
		this(addr, timeoutMillis, null);
	}

	/**
	 * set exception
	 * @param addr address
	 * @param timeoutMillis 超时时间
	 * @param cause 异常对象
	 */
	public RemotingTimeoutException(String addr, long timeoutMillis, Throwable cause)
	{
		super(SedaConstants.TIMEOUT, "wait response on the channel <" + addr + "> timeout, " + timeoutMillis + "(ms)",
				cause);
	}
}
