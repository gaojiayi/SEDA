package com.ai.aif.seda.exception;

import com.ai.aif.seda.common.SedaConstants;

/**
 * @Title: 队列已满
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class QueueFullException extends RemotingException
{
	private static final long serialVersionUID = -1857495110647453958L;

	/**
	 * @param message
	 */
	public QueueFullException(String message)
	{
		super(SedaConstants.QUEUE_FULL_EXCEPTION, message);
	}

}
