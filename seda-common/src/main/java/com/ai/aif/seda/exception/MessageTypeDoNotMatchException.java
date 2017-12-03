package com.ai.aif.seda.exception;

/**
 * @Title:消息类型不匹配
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class MessageTypeDoNotMatchException extends RemotingException
{
	private static final long serialVersionUID = 6918989182604968172L;

	/**
	 * @param message
	 */
	public MessageTypeDoNotMatchException(String message)
	{
		super(message);
	}

	/**
	 * @param returnCode
	 * @param message
	 * @param cause
	 */
	public MessageTypeDoNotMatchException(String returnCode, String message, Throwable cause)
	{
		super(returnCode, message, cause);
	}

}
