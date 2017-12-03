package com.ai.aif.seda.exception;

/**
 * @Title: 通信层异常父类
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class RemotingException extends Exception
{
	private static final long serialVersionUID = -5690687334570505110L;

	private String returnCode;

	/**
	 * @return the returnCode
	 */
	public String getReturnCode()
	{
		return returnCode;
	}

	/**
	 * @param returnCode the returnCode to set
	 */
	public void setReturnCode(String returnCode)
	{
		this.returnCode = returnCode;
	}

	/**
	 * set exception message
	 * @param message
	 */
	public RemotingException(String message)
	{
		super(message);
	}

	/**
	 * set exception message 
	 * @param returnCode
	 * @param message
	 */
	public RemotingException(String returnCode, String message)
	{
		super(message);
		this.returnCode = returnCode;
	}

	/**
	 * set exception message
	 * @param message message
	 * @param cause throwable
	 */
	public RemotingException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/***
	 * @param returnCode
	 * @param message
	 * @param cause
	 */
	public RemotingException(String returnCode, String message, Throwable cause)
	{
		super(message, cause);
		this.returnCode = returnCode;
	}
}
