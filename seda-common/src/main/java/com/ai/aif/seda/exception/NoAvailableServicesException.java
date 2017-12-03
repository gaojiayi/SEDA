package com.ai.aif.seda.exception;

import com.ai.aif.seda.common.SedaConstants;

/**
 * @Title: 没可用服务
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class NoAvailableServicesException extends RemotingException
{
	private static final long serialVersionUID = 2727075053568544260L;

	/**
	 * @param message
	 */
	public NoAvailableServicesException(String message)
	{
		super(SedaConstants.NOT_AVAILABLE_SERVICES_EXCEPTION, "send request failed, case:" + message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoAvailableServicesException(String message, Throwable cause)
	{
		super(SedaConstants.NOT_AVAILABLE_SERVICES_EXCEPTION, "send request failed, case:" + message, cause);
	}

}
