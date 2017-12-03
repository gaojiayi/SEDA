package com.ai.aif.seda.message;

/**
 * @Title: netty 相关配置
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月16日
 * @Version: 1.0
 */
public class NettyConfig
{
	/** 发送消息缓存大小 */
	public static final String SOCKET_SNDBUF_SIZE = "com.seda.service.socket.sndbuf.size";

	/** 接收消息缓存大小 */
	public static final String SOCKET_RCVBUF_SIZE = "com.seda.service.socket.rcvbuf.size";

	public static int SocketSndbufSize = //
	Integer.parseInt(System.getProperty(SOCKET_SNDBUF_SIZE, "65535"));

	public static int SocketRcvbufSize = //
	Integer.parseInt(System.getProperty(SOCKET_RCVBUF_SIZE, "65535"));

	// ====================== 服务端配置 ======================
	private int listenPort = 8888;
	// 工作者的线程个数
	private int sWorkerThreads = 8;
	private int sCallbackExecutorThreads = 0;
	private int sSelectorThreads = 3;
	private int sOnewaySemaphoreValue = 256;
	private int sAsyncSemaphoreValue = 64;
	private int sChannelMaxIdleTimeSeconds = 120;

	private int sSocketSndBufSize = SocketSndbufSize;
	private int sSocketRcvBufSize = SocketRcvbufSize;
	private boolean sPooledByteBufAllocatorEnable = false;

	// =======================================================

	/**
	 * @return the listenPort
	 */
	public int getListenPort()
	{
		return listenPort;
	}

	/**
	 * @param listenPort the listenPort to set
	 */
	public void setListenPort(int listenPort)
	{
		this.listenPort = listenPort;
	}

	/**
	 * @return the sWorkerThreads
	 */
	public int getsWorkerThreads()
	{
		return sWorkerThreads;
	}

	/**
	 * @param sWorkerThreads the sWorkerThreads to set
	 */
	public void setsWorkerThreads(int sWorkerThreads)
	{
		this.sWorkerThreads = sWorkerThreads;
	}

	/**
	 * @return the sCallbackExecutorThreads
	 */
	public int getsCallbackExecutorThreads()
	{
		return sCallbackExecutorThreads;
	}

	/**
	 * @param sCallbackExecutorThreads the sCallbackExecutorThreads to set
	 */
	public void setsCallbackExecutorThreads(int sCallbackExecutorThreads)
	{
		this.sCallbackExecutorThreads = sCallbackExecutorThreads;
	}

	/**
	 * @return the sSelectorThreads
	 */
	public int getsSelectorThreads()
	{
		return sSelectorThreads;
	}

	/**
	 * @param sSelectorThreads the sSelectorThreads to set
	 */
	public void setsSelectorThreads(int sSelectorThreads)
	{
		this.sSelectorThreads = sSelectorThreads;
	}

	/**
	 * @return the sOnewaySemaphoreValue
	 */
	public int getsOnewaySemaphoreValue()
	{
		return sOnewaySemaphoreValue;
	}

	/**
	 * @param sOnewaySemaphoreValue the sOnewaySemaphoreValue to set
	 */
	public void setsOnewaySemaphoreValue(int sOnewaySemaphoreValue)
	{
		this.sOnewaySemaphoreValue = sOnewaySemaphoreValue;
	}

	/**
	 * @return the sAsyncSemaphoreValue
	 */
	public int getsAsyncSemaphoreValue()
	{
		return sAsyncSemaphoreValue;
	}

	/**
	 * @param sAsyncSemaphoreValue the sAsyncSemaphoreValue to set
	 */
	public void setsAsyncSemaphoreValue(int sAsyncSemaphoreValue)
	{
		this.sAsyncSemaphoreValue = sAsyncSemaphoreValue;
	}

	/**
	 * @return the sChannelMaxIdleTimeSeconds
	 */
	public int getsChannelMaxIdleTimeSeconds()
	{
		return sChannelMaxIdleTimeSeconds;
	}

	/**
	 * @param sChannelMaxIdleTimeSeconds the sChannelMaxIdleTimeSeconds to set
	 */
	public void setsChannelMaxIdleTimeSeconds(int sChannelMaxIdleTimeSeconds)
	{
		this.sChannelMaxIdleTimeSeconds = sChannelMaxIdleTimeSeconds;
	}

	/**
	 * @return the sPooledByteBufAllocatorEnable
	 */
	public boolean issPooledByteBufAllocatorEnable()
	{
		return sPooledByteBufAllocatorEnable;
	}

	/**
	 * @param sPooledByteBufAllocatorEnable the sPooledByteBufAllocatorEnable to set
	 */
	public void setsPooledByteBufAllocatorEnable(boolean sPooledByteBufAllocatorEnable)
	{
		this.sPooledByteBufAllocatorEnable = sPooledByteBufAllocatorEnable;
	}

	/**
	 * @return the sSocketSndBufSize
	 */
	public int getsSocketSndBufSize()
	{
		return sSocketSndBufSize;
	}

	/**
	 * @param sSocketSndBufSize the sSocketSndBufSize to set
	 */
	public void setsSocketSndBufSize(int sSocketSndBufSize)
	{
		this.sSocketSndBufSize = sSocketSndBufSize;
	}

	/**
	 * @return the sSocketRcvBufSize
	 */
	public int getsSocketRcvBufSize()
	{
		return sSocketRcvBufSize;
	}

	/**
	 * @param sSocketRcvBufSize the sSocketRcvBufSize to set
	 */
	public void setsSocketRcvBufSize(int sSocketRcvBufSize)
	{
		this.sSocketRcvBufSize = sSocketRcvBufSize;
	}

}
