package com.ai.aif.seda.message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @Title: 客户端配置对象
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月22日
 * @Version: 1.0
 */
public class NettyConfigByClient
{
	/** 发送消息缓存大小 */
	public static final String SOCKET_SNDBUF_SIZE = "com.seda.client.socket.sndbuf.size";

	/** 接收消息缓存大小 */
	public static final String SOCKET_RCVBUF_SIZE = "com.seda.client.socket.rcvbuf.size";

	public static int SocketSndbufSize = //
	Integer.parseInt(System.getProperty(SOCKET_SNDBUF_SIZE, "65535"));

	public static int SocketRcvbufSize = //
	Integer.parseInt(System.getProperty(SOCKET_RCVBUF_SIZE, "65535"));

	/**
	 * Worker thread number
	 */
	private int clientWorkerThreads = 4;
	private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();

	private Map<String, Semaphore> onewaySemaphore = new HashMap<String, Semaphore>();

	private Map<String, Semaphore> asyncSemaphore = new HashMap<String, Semaphore>();

	// private int clientOnewaySemaphoreValue = 2048;
	// private int clientAsyncSemaphoreValue = 2048;
	private long connectTimeoutMillis = 3000;

	/**
	 * IdleStateEvent will be triggered when neither read nor write was performed for the specified period of this time.
	 * Specify {@code 0} to disable
	 */
	private int clientChannelMaxIdleTimeSeconds = 120;

	private int clientSocketSndBufSize = SocketSndbufSize;
	private int clientSocketRcvBufSize = SocketRcvbufSize;

	public Map<String, Semaphore> getAsyncSemaphore()
	{
		return asyncSemaphore;
	}

	public Map<String, Semaphore> getOnewaySemaphore()
	{
		return onewaySemaphore;
	}

	public void addAsyncSemaplhore(String addr, int asyncSemaphoreValue)
	{
		Semaphore sphore = new Semaphore(asyncSemaphoreValue, true);
		asyncSemaphore.put(addr, sphore);
	}

	public void addOnewaySemaplhore(String addr, int onewaySemaphoreValue)
	{
		Semaphore sphore = new Semaphore(onewaySemaphoreValue, true);
		onewaySemaphore.put(addr, sphore);
	}

	/**
	 * @return the clientWorkerThreads
	 */
	public int getClientWorkerThreads()
	{
		return clientWorkerThreads;
	}

	/**
	 * @param clientWorkerThreads the clientWorkerThreads to set
	 */
	public void setClientWorkerThreads(int clientWorkerThreads)
	{
		this.clientWorkerThreads = clientWorkerThreads;
	}

	/**
	 * @return the clientCallbackExecutorThreads
	 */
	public int getClientCallbackExecutorThreads()
	{
		return clientCallbackExecutorThreads;
	}

	/**
	 * @param clientCallbackExecutorThreads the clientCallbackExecutorThreads to set
	 */
	public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads)
	{
		this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
	}

	/**
	 * @return the connectTimeoutMillis
	 */
	public long getConnectTimeoutMillis()
	{
		return connectTimeoutMillis;
	}

	/**
	 * @param connectTimeoutMillis the connectTimeoutMillis to set
	 */
	public void setConnectTimeoutMillis(long connectTimeoutMillis)
	{
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	/**
	 * @return the clientChannelMaxIdleTimeSeconds
	 */
	public int getClientChannelMaxIdleTimeSeconds()
	{
		return clientChannelMaxIdleTimeSeconds;
	}

	/**
	 * @param clientChannelMaxIdleTimeSeconds the clientChannelMaxIdleTimeSeconds to set
	 */
	public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds)
	{
		this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
	}

	/**
	 * @return the clientSocketSndBufSize
	 */
	public int getClientSocketSndBufSize()
	{
		return clientSocketSndBufSize;
	}

	/**
	 * @param clientSocketSndBufSize the clientSocketSndBufSize to set
	 */
	public void setClientSocketSndBufSize(int clientSocketSndBufSize)
	{
		this.clientSocketSndBufSize = clientSocketSndBufSize;
	}

	/**
	 * @return the clientSocketRcvBufSize
	 */
	public int getClientSocketRcvBufSize()
	{
		return clientSocketRcvBufSize;
	}

	/**
	 * @param clientSocketRcvBufSize the clientSocketRcvBufSize to set
	 */
	public void setClientSocketRcvBufSize(int clientSocketRcvBufSize)
	{
		this.clientSocketRcvBufSize = clientSocketRcvBufSize;
	}
}
