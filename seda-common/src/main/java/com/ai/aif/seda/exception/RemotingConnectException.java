package com.ai.aif.seda.exception;

import com.ai.aif.seda.common.SedaConstants;

/**
 * @Title: Client连接Server失败，抛出此异常
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class RemotingConnectException extends RemotingException
{
	private static final long serialVersionUID = -5565366231695911316L;

	/**
	 * set exception
	 * @param addr address
	 */
	public RemotingConnectException(String addr)
	{
		this(addr, null);
	}

	/**
	 * set exception
	 * @param addr address
	 * @param cause throwable
	 */
	public RemotingConnectException(String addr, Throwable cause)
	{
		super(SedaConstants.CONNECT_EXCEPTION, "connect to <" + addr + "> failed", cause);
	}
}
