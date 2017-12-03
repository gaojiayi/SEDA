package com.ai.aif.seda.exception;

/**
 * @Title: 命令解析自定义字段时，字段无效抛出该异常
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class RemotingCommandException extends RemotingException
{
	private static final long serialVersionUID = -6061365915274953096L;

	public RemotingCommandException(String message)
	{
		this(message, null);
	}

	public RemotingCommandException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
