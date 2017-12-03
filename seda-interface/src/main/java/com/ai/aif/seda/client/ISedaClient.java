package com.ai.aif.seda.client;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.ai.aif.seda.InvokeCallBack;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.RemotingConnectException;
import com.ai.aif.seda.exception.RemotingSendRequestException;
import com.ai.aif.seda.exception.RemotingTimeoutException;
import com.ai.aif.seda.exception.RemotingTooMuchRequestException;
import com.ai.aif.seda.processor.ISedaProcessor;
import com.ai.aif.seda.service.ISedaRemotingService;

/**
 * @Title: 客户端
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月22日
 * @Version: 1.0
 */
public interface ISedaClient extends ISedaRemotingService
{
	public void updateNameServerAddressList(final List<String> addrs);

	public List<String> getNameServerAddressList();

	/**
	 * 处理同步请求
	 * 
	 * @param addr address
	 * @param request SedaCommand
	 * @param timeoutMillis timeout
	 * @return SedaCommand
	 * @throws InterruptedException
	 * @throws RemotingConnectException
	 * @throws RemotingSendRequestException
	 * @throws RemotingTimeoutException
	 */
	public SedaCommand invokeSync(final String addr, final SedaCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingSendRequestException,
			RemotingTimeoutException;

	/**
	 * 处理异步请求
	 * 
	 * @param addr
	 * @param request
	 * @param timeoutMillis
	 * @param invokeCallback
	 * @throws InterruptedException
	 * @throws RemotingConnectException
	 * @throws RemotingTooMuchRequestException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public void invokeAsync(final String addr, final SedaCommand request, final long timeoutMillis,
			final InvokeCallBack invokeCallback) throws InterruptedException, RemotingConnectException,
			RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

	/**
	 * 处理单向请求
	 * 
	 * @param addr
	 * @param request
	 * @param timeoutMillis
	 * @throws InterruptedException
	 * @throws RemotingConnectException
	 * @throws RemotingTooMuchRequestException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public void invokeOneway(final String addr, final SedaCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException;

	/**
	 * 注册事件驱动
	 * 
	 * @param evnet 事件类型
	 * @param processor 处理器
	 * @param executor 线程
	 */
	public void registerProcessor(final EventType evnet, final ISedaProcessor processor, final ExecutorService executor);

	public boolean isChannelWriteable(final String addr);

}
