package com.ai.aif.seda.config.server;

import com.ai.aif.seda.config.AbstractConfig;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class ServerConfig extends AbstractConfig
{

	private int port;

	private int selectorThreads;

	// 执行服务对应线程池阻塞队列大小
	private int callServerThreadPoolQueueCapacity;

	// 执行服务对应的线程个数  
	private int callServerThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 4;

	/**
	 * @return the port
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @return the selectorThreads
	 */
	public int getSelectorThreads()
	{
		return selectorThreads;
	}

	/**
	 * @param selectorThreads the selectorThreads to set
	 */
	public void setSelectorThreads(int selectorThreads)
	{
		this.selectorThreads = selectorThreads;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @return the callServerThreadPoolQueueCapacity
	 */
	public int getCallServerThreadPoolQueueCapacity()
	{
		return callServerThreadPoolQueueCapacity;
	}

	/**
	 * @param callServerThreadPoolQueueCapacity the callServerThreadPoolQueueCapacity to set
	 */
	public void setCallServerThreadPoolQueueCapacity(int callServerThreadPoolQueueCapacity)
	{
		this.callServerThreadPoolQueueCapacity = callServerThreadPoolQueueCapacity;
	}

	/**
	 * @return the callServerThreadPoolNums
	 */
	public int getCallServerThreadPoolNums()
	{
		return callServerThreadPoolNums;
	}

	/**
	 * @param callServerThreadPoolNums the callServerThreadPoolNums to set
	 */
	public void setCallServerThreadPoolNums(int callServerThreadPoolNums)
	{
		if (callServerThreadPoolNums <= 0)
		{
			return;
		}

		this.callServerThreadPoolNums = callServerThreadPoolNums;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.config.Node#getCopyObject()
	 */
	@Override
	protected Object getCopyObject()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString());
		builder.append("\nServerConfig:{\n port=");
		builder.append(port);
		builder.append("\n selectorThreads=");
		builder.append(selectorThreads);
		builder.append("\n callServerThreadPoolQueueCapacity=");
		builder.append(callServerThreadPoolQueueCapacity);
		builder.append("\n callServerThreadPoolNums=");
		builder.append(callServerThreadPoolNums);
		builder.append("\n}");
		return builder.toString();
	}

}
