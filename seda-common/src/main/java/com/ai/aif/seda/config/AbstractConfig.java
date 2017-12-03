package com.ai.aif.seda.config;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public abstract class AbstractConfig extends Node
{
	// 工作线程
	private int workerThreads = 4;

	// 执行回调接口线程数
	private int callBackThreads = 8;

	// 连接最大空闲时间
	private int channelMaxIdleTimeSeconds = 120;

	// 异步并发数
	private int asyncConcurrentNumber = 1024;

	// 单向并发数
	private int onewayConcurrentNumber = 1024;

	private int sndbufSize = 65531;

	private int rcvbufSize = 65531;

	private boolean recordRequestLog;
	
	// 消息大小超出该值，压缩
	private int mesgCompressionLimit;

	/**
	 * @return the mesgCompressionLimit
	 */
	public int getMesgCompressionLimit()
	{
		return mesgCompressionLimit;
	}

	/**
	 * @param mesgCompressionLimit the mesgCompressionLimit to set
	 */
	public void setMesgCompressionLimit(int mesgCompressionLimit)
	{
		this.mesgCompressionLimit = mesgCompressionLimit;
	}

	/**
	 * @return the channelMaxIdleTimeSeconds
	 */
	public int getChannelMaxIdleTimeSeconds()
	{
		return channelMaxIdleTimeSeconds;
	}

	/**
	 * @param channelMaxIdleTimeSeconds the channelMaxIdleTimeSeconds to set
	 */
	public void setChannelMaxIdleTimeSeconds(int channelMaxIdleTimeSeconds)
	{
		this.channelMaxIdleTimeSeconds = channelMaxIdleTimeSeconds;
	}

	/**
	 * @return the asyncConcurrentNumber
	 */
	public int getAsyncConcurrentNumber()
	{
		return asyncConcurrentNumber;
	}

	/**
	 * @param asyncConcurrentNumber the asyncConcurrentNumber to set
	 */
	public void setAsyncConcurrentNumber(int asyncConcurrentNumber)
	{
		this.asyncConcurrentNumber = asyncConcurrentNumber;
	}

	/**
	 * @return the onewayConcurrentNumber
	 */
	public int getOnewayConcurrentNumber()
	{
		return onewayConcurrentNumber;
	}

	/**
	 * @param onewayConcurrentNumber the onewayConcurrentNumber to set
	 */
	public void setOnewayConcurrentNumber(int onewayConcurrentNumber)
	{
		this.onewayConcurrentNumber = onewayConcurrentNumber;
	}

	/**
	 * @return the callBackThreads
	 */
	public int getCallBackThreads()
	{
		return callBackThreads;
	}

	/**
	 * @param callBackThreads the callBackThreads to set
	 */
	public void setCallBackThreads(int callBackThreads)
	{
		this.callBackThreads = callBackThreads;
	}

	/**
	 * @return the workerThreads
	 */
	public int getWorkerThreads()
	{
		return workerThreads;
	}

	/**
	 * @param workerThreads the workerThreads to set
	 */
	public void setWorkerThreads(int workerThreads)
	{
		this.workerThreads = workerThreads;
	}

	/**
	 * @return the sndbufSize
	 */
	public int getSndbufSize()
	{
		return sndbufSize;
	}

	/**
	 * @param sndbufSize the sndbufSize to set
	 */
	public void setSndbufSize(int sndbufSize)
	{
		this.sndbufSize = sndbufSize;
	}

	/**
	 * @return the rcvbufSize
	 */
	public int getRcvbufSize()
	{
		return rcvbufSize;
	}

	/**
	 * @param rcvbufSize the rcvbufSize to set
	 */
	public void setRcvbufSize(int rcvbufSize)
	{
		this.rcvbufSize = rcvbufSize;
	}

	/**
	 * @return the recordRequestLog
	 */
	public boolean isRecordRequestLog()
	{
		return recordRequestLog;
	}

	/**
	 * @param recordRequestLog the recordRequestLog to set
	 */
	public void setRecordRequestLog(boolean recordRequestLog)
	{
		this.recordRequestLog = recordRequestLog;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Basic configuration:{\n workerThreads=");
		builder.append(workerThreads);
		builder.append("\n callBackThreads=");
		builder.append(callBackThreads);
		builder.append("\n channelMaxIdleTimeSeconds=");
		builder.append(channelMaxIdleTimeSeconds);
		builder.append("\n asyncConcurrentNumber=");
		builder.append(asyncConcurrentNumber);
		builder.append("\n onewayConcurrentNumber=");
		builder.append(onewayConcurrentNumber);
		builder.append("\n rcvbufSize=");
		builder.append(rcvbufSize);
		builder.append("\n sndbufSize=");
		builder.append(sndbufSize);
		builder.append("\n recordRequestLog=");
		builder.append(recordRequestLog);
		builder.append("\n mesgCompressionLimit=");
		builder.append(mesgCompressionLimit);
		builder.append("\n}");
		return builder.toString();
	}

}
