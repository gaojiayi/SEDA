package com.ai.aif.seda.exception;

import com.ai.aif.seda.common.SedaConstants;

/**
 * @Title: 异步调用或Oneway调用，堆积的请求超出信号量最大值
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月8日
 * @Version: 1.0
 */
public class RemotingTooMuchRequestException extends RemotingException
{
	private static final long serialVersionUID = 4326919581254519654L;

	public RemotingTooMuchRequestException(String message)
	{
		super(SedaConstants.TOO_MUCH_REQUEST_EXCEPTION, message, null);
	}
}
