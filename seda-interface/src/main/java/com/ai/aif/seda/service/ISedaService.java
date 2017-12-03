package com.ai.aif.seda.service;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

import com.ai.aif.seda.InvokeCallBack;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.Pair;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.RemotingSendRequestException;
import com.ai.aif.seda.exception.RemotingTimeoutException;
import com.ai.aif.seda.exception.RemotingTooMuchRequestException;
import com.ai.aif.seda.processor.ISedaProcessor;

/**
 * @Title: 服务处理器
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface ISedaService extends ISedaRemotingService
{
	/**
	 * 注册事件处理器，每个事件处理器，对应一个ExecutorService有队列限制的阻塞队列，防止OOM
	 * 
	 * @param eventType 事件类型
	 * @param processor 处理器
	 * @param exService 线程组
	 */
	public void registerProcessor(final EventType eventType, final ISedaProcessor processor,
			final ExecutorService exService);

	/**
	 * 默认/内部处理器
	 * 
	 * @param processor 处理器
	 * @param exService 线程组
	 * @return
	 */
	public void registerDefaultProcessor(final ISedaProcessor processor, final ExecutorService exService);

	/**
	 * 获取事件的处理器
	 * 
	 * @param type 事件类型
	 * @return 处理器
	 */
	public Pair<ISedaProcessor, ExecutorService> getProcessorPair(final EventType type);

	/**
	 * 同步处理机制
	 * 
	 * @param channel 通讯管道
	 * @param request 交互对象
	 * @param timeoutMillis 超时时间，单位毫秒
	 * @return SedaCommand
	 * @throws InterruptedException
	 * @throws RemotingSendRequestException
	 * @throws RemotingTimeoutException
	 */
	public SedaCommand invokeSync(final Channel channel, final SedaCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException;

	/**
	 * 异步处理机制
	 * 
	 * @param channel 通讯管道
	 * @param request 交互对象
	 * @param timeoutMillis 超时时间 ，单位毫秒
	 * @param invokeCallback 异步回调接口
	 * @throws InterruptedException
	 * @throws RemotingTooMuchRequestException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public void invokeAsync(final Channel channel, final SedaCommand request, final long timeoutMillis,
			final InvokeCallBack invokeCallback) throws InterruptedException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException;

	/**
	 * 单向处理机制
	 * 
	 * @param channel 通讯管道
	 * @param request 交互对象
	 * @param timeoutMillis 超时时间，单位毫秒
	 * @throws InterruptedException
	 * @throws RemotingTooMuchRequestException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public void invokeOneway(final Channel channel, final SedaCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
			RemotingSendRequestException;
	
	/**
	 * 获取本地绑定的接口
	 * @return port
	 */
	public int getLocalListenPort();

}
