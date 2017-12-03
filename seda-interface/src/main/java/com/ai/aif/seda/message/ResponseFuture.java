package com.ai.aif.seda.message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ai.aif.seda.InvokeCallBack;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.ReleaseSemaphoreOnlyOnce;

/**
 * @Title: 异步请求的应答
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月16日
 * @Version: 1.0
 */
public class ResponseFuture
{
	// 保证回调的callback方法至多至少只被执行一次
	private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);

	// 同步计数器
	private final CountDownLatch countDownLatch = new CountDownLatch(1);

	// 记录Response 被创建的时间
	private final long beginTimestamp = System.currentTimeMillis();

	// 保证信号量至多至少只被释放一次
	private final ReleaseSemaphoreOnlyOnce once;

	private final InvokeCallBack invokeCallback;

	private final long timeoutMillis;

	// private final EventType eventType;

	private final String opaque;
	
	//remote address/
	private volatile String remoteAddr;

	// 记录异常
	private volatile Throwable cause;

	private volatile SedaCommand respCommand;

	private volatile boolean sendRequestOK = false;

	public ResponseFuture(String opaque, long timeoutMillis, InvokeCallBack invokeCallback,
			ReleaseSemaphoreOnlyOnce once)
	{
		this.opaque = opaque;
		this.invokeCallback = invokeCallback;
		this.timeoutMillis = timeoutMillis;
		this.once = once;

	}

	public ResponseFuture(String opaque, long timeoutMillis)
	{
		this.opaque = opaque;
		this.timeoutMillis = timeoutMillis;
		this.invokeCallback = null;
		this.once = null;
	}

	/** 执行异步回调函数 */
	public void executeInvokeCallback()
	{
		if (invokeCallback != null)
		{
			if (this.executeCallbackOnlyOnce.compareAndSet(false, true))
			{
				invokeCallback.operationComplete(this);
			}
		}
	}

	/**
	 * @return the respCommand
	 */
	public SedaCommand getRespCommand()
	{
		return respCommand;
	}

	/**
	 * @param respCommand the respCommand to set
	 */
	public void setRespCommand(SedaCommand respCommand)
	{
		this.respCommand = respCommand;
	}

	/**
	 * @return the beginTimestamp
	 */
	public long getBeginTimestamp()
	{
		return beginTimestamp;
	}

	/**
	 * @return the timeoutMillis
	 */
	public long getTimeoutMillis()
	{
		return timeoutMillis;
	}

	/**
	 * @return the sendRequestOK
	 */
	public boolean isSendRequestOK()
	{
		return sendRequestOK;
	}

	/**
	 * @param sendRequestOK the sendRequestOK to set
	 */
	public void setSendRequestOK(boolean sendRequestOK)
	{
		this.sendRequestOK = sendRequestOK;
	}

	/***
	 * 是否超时
	 * 
	 * @return
	 */
	public boolean isTimeout()
	{
		long diff = System.currentTimeMillis() - this.beginTimestamp;
		return diff > this.timeoutMillis;
	}

	/**
	 * @return the cause
	 */
	public Throwable getCause()
	{
		return cause;
	}

	/**
	 * @return the opaque
	 */
	public String getOpaque()
	{
		return opaque;
	}

	/**
	 * @return the invokeCallback
	 */
	public InvokeCallBack getInvokeCallback()
	{
		return invokeCallback;
	}

	/**
	 * 同步机制使用
	 * 
	 * @return the respCommand
	 * @throws InterruptedException
	 */
	public SedaCommand awaitResponse(final long timeoutMillis) throws InterruptedException
	{
		// 等响应请求被填充
		this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
		return respCommand;
	}

	/**
	 * @param respCommand the respCommand to set
	 */
	public void putResponse(SedaCommand respCommand)
	{
		this.respCommand = respCommand;
		// 请求被填充后，释放等待的线程
		this.countDownLatch.countDown();
	}

	/**
	 * @return the remoteAddr
	 */
	public String getRemoteAddr()
	{
		return remoteAddr;
	}

	/**
	 * @param remoteAddr the remoteAddr to set
	 */
	public void setRemoteAddr(String remoteAddr)
	{
		this.remoteAddr = remoteAddr;
	}

	/**
	 * @param cause the cause to set
	 */
	public void setCause(Throwable cause)
	{
		this.cause = cause;
	}

	/** 释放信号量，保证只释放一次 */
	public void release()
	{
		if (null != once)
		{
			once.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ResponseFuture:{\n executeCallbackOnlyOnce=");
		builder.append(executeCallbackOnlyOnce);
		builder.append("\n countDownLatch=");
		builder.append(countDownLatch);
		builder.append("\n beginTimestamp=");
		builder.append(beginTimestamp);
		builder.append("\n once=");
		builder.append(once);
		builder.append("\n invokeCallback=");
		builder.append(invokeCallback);
		builder.append("\n timeoutMillis=");
		builder.append(timeoutMillis);
		builder.append("\n opaque=");
		builder.append(opaque);
		builder.append("\n cause=");
		builder.append(cause);
		builder.append("\n respCommand=");
		builder.append(respCommand);
		builder.append("\n sendRequestOK=");
		builder.append(sendRequestOK);
		builder.append("\n isTimeout=");
		builder.append(isTimeout());
		builder.append("\n}");
		return builder.toString();
	}
}
