package com.ai.aif.seda.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.ai.aif.seda.InvokeCallBack;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.CommandType;
import com.ai.aif.seda.common.Helper;
import com.ai.aif.seda.common.Pair;
import com.ai.aif.seda.common.ReleaseSemaphoreOnlyOnce;
import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.AopException;
import com.ai.aif.seda.exception.RemotingSendRequestException;
import com.ai.aif.seda.exception.RemotingTimeoutException;
import com.ai.aif.seda.exception.RemotingTooMuchRequestException;
import com.ai.aif.seda.interceptor.RpcInterceptor;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.CustomMessageHeader;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.message.MessageResponse;
import com.ai.aif.seda.message.ResponseFuture;
import com.ai.aif.seda.processor.ISedaProcessor;
import com.ai.aif.seda.utils.LogUtils;
//import com.asiainfo.openplatform.common.exception.AopException;

/**
 * @Title: 服务端与客户端的抽象类
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月16日
 * @Version: 1.0
 */
public abstract class AbstractSedaService
{
	// private static final Logger log = LogManager.getLogger(AbstractSedaService.class);
	private static final ILog<?> log = LogUtils.getILog(AbstractSedaService.class);

	// 缓存所有对外请求
	protected final ConcurrentHashMap<String, ResponseFuture> responseTable = new ConcurrentHashMap<String, ResponseFuture>(
			256);

	// 注册的各个RPC处理器
	protected final HashMap<EventType/* request code */, Pair<ISedaProcessor, ExecutorService>> processors = new HashMap<EventType, Pair<ISedaProcessor, ExecutorService>>(
			64);

	// 信号量，异步调用情况会使用，防止本地Netty缓存请求过多
	public final ConcurrentHashMap<String/* ip:port */, Semaphore> semaphoreAsyncMap = new ConcurrentHashMap<String, Semaphore>();

	// 信号量，Oneway情况会使用，防止本地Netty缓存请求过多
	protected final ConcurrentHashMap<String/* ip:port */, Semaphore> semaphoreOnewayMap = new ConcurrentHashMap<String, Semaphore>();

	// 默认请求代码处理器
	protected Pair<ISedaProcessor, ExecutorService> defaultReqProcessor;

	// 信号量，Oneway情况会使用，防止本地Netty缓存请求过多
	protected Semaphore semaphoreOneway;

	// 信号量，异步调用情况会使用，防止本地Netty缓存请求过多
	protected Semaphore semaphoreAsync;

	private boolean isOnewayReq = false;

	/**
	 * 执行回调函数的线程
	 * @return ExecutorService
	 */
	public abstract ExecutorService getCallbackExecutor();

	/***
	 * 拿拦截器
	 * @return
	 */
	public abstract RpcInterceptor getRpcInterceptor();

	/**
	 * 构造函数
	 * @param onewaPermits 单向许可数
	 * @param asyncPermits 异步许可数
	 */
	public AbstractSedaService(final Map<String, Semaphore> onewaPermits, final Map<String, Semaphore> asyncPermits)
	{
		semaphoreOnewayMap.putAll(onewaPermits);
		semaphoreAsyncMap.putAll(asyncPermits);
	}

	public AbstractSedaService(final int onewaPermits, final int asyncPermits)
	{
		this.semaphoreOneway = new Semaphore(onewaPermits, true);
		this.semaphoreAsync = new Semaphore(asyncPermits, true);
	}

	/**
	 * 同步处理机制 不支持批量提交
	 * @param channel 通道
	 * @param request 交互对象
	 * @param timeoutMillis 超时时间
	 * @return SedaCommand
	 * @throws InterruptedException
	 * @throws RemotingSendRequestException
	 * @throws RemotingTimeoutException
	 */
	public SedaCommand invokeSync(final Channel channel, final SedaCommand request, long timeoutMillis)
			throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException
	{
		try
		{
			final ResponseFuture resp = new ResponseFuture(request.getOpaque(), timeoutMillis, null, null);

			responseTable.put(request.getOpaque(), resp);

			channel.writeAndFlush(request).addListener(new ChannelFutureListener()
			{
				public void operationComplete(ChannelFuture future) throws Exception
				{
					if (future.isSuccess())
					{
						resp.setSendRequestOK(true);
					}
					else
					{
						// 这里与下面的remove有时间的差异，这里是否需要去掉，到不影响，还是选择放这里吧。
						responseTable.remove(request.getOpaque());
						resp.setSendRequestOK(false);
						resp.setCause(future.cause());
						resp.putResponse(null);
						log.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
						log.warn(request.toString());
					}
				}
			});

			// 获取响应，在指定timeoutMillis时间内，没返回，视为超时。
			SedaCommand respCommand = resp.awaitResponse(timeoutMillis);

			if (null != respCommand)
			{
				return respCommand;
			}
			String addr = Helper.parseChannelRemoteAddr(channel);

			if (resp.isSendRequestOK())
			{
				throw new RemotingTimeoutException(addr, timeoutMillis, resp.getCause());
			}
			else
			{
				throw new RemotingSendRequestException(addr, resp.getCause());
			}
		}
		finally
		{
			responseTable.remove(request.getOpaque());
		}
	}

	/**
	 * 异步处理机制
	 * @param channel Channel
	 * @param request SedaCommand
	 * @param timeoutMillis timeout
	 * @param invokeCallback 回函调
	 * @throws InterruptedException
	 * @throws RemotingTooMuchRequestException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public void invokeAsync(final Channel channel, final SedaCommand request, long timeoutMillis,
			InvokeCallBack invokeCallback) throws InterruptedException, RemotingTooMuchRequestException,
			RemotingSendRequestException
	{
		// 获取信号量
		int arg = getPermits(request);

		Semaphore semaphoreAsync = semaphoreAsyncMap.get(Helper.parseChannelRemoteAddr(channel));

		if (null == semaphoreAsync)
		{
			semaphoreAsync = this.semaphoreAsync;
		}

		boolean acquired = semaphoreAsync.tryAcquire(arg, timeoutMillis, TimeUnit.MILLISECONDS);

		if (acquired)
		{
			ReleaseSemaphoreOnlyOnce once = new ReleaseSemaphoreOnlyOnce(semaphoreAsync);

			final ResponseFuture resp = new ResponseFuture(request.getOpaque(), timeoutMillis, invokeCallback, once);
			// 缓存所有请求
			batchSetResponse(request, timeoutMillis, invokeCallback, semaphoreAsync, resp);

			try
			{
				channel.writeAndFlush(request).addListener(new ChannelFutureListener()
				{
					public void operationComplete(ChannelFuture future) throws Exception
					{
						if (null != request.readCustomHanders())
						{
							for (CustomMessageHeader msg : request.readCustomHanders())
							{
								MessageRequest mReq = (MessageRequest) msg;
								ResponseFuture respFuture = responseTable.get(mReq.getUniqueCode());
								setResult(channel, request, future, respFuture);
							}
						}
						else
						{
							setResult(channel, request, future, resp);
						}
					}

					/** 设置结果 */
					private void setResult(final Channel channel, final SedaCommand request, ChannelFuture future,
							ResponseFuture resp)
					{
						if (future.isSuccess())
						{
							resp.setSendRequestOK(true);
							return;
						}
						resp.setCause(future.cause());
						resp.putResponse(null);
						responseTable.remove(request.getOpaque());

						try
						{
							resp.executeInvokeCallback();
						}
						finally
						{
							resp.release();
						}
						log.warn("send a request command to channel <" + Helper.parseChannelRemoteAddr(channel)
								+ "> failed.");
						log.warn(request.toString());
					}
				});

			}
			catch (Exception e)
			{
				batchRelease(request, resp);
				String addr = Helper.parseChannelRemoteAddr(channel);
				log.warn("send a request command to channel <" + addr + "> Exception", e);
				throw new RemotingSendRequestException(addr, e);

			}
		}
		else
		{
			throwException(request, timeoutMillis, "invokeAsync", semaphoreAsync);
		}

	}

	/** 批量记录所有的请求消息 */
	private void batchSetResponse(final SedaCommand request, long timeoutMillis, InvokeCallBack invokeCallback,
			Semaphore semaphore, ResponseFuture resp)
	{
		if (null != request.readCustomHanders())
		{
			// 记录所有请求
			for (CustomMessageHeader msg : request.readCustomHanders())
			{
				MessageRequest mReq = (MessageRequest) msg;
				ReleaseSemaphoreOnlyOnce once = new ReleaseSemaphoreOnlyOnce(semaphore);
				ResponseFuture childrenResp = new ResponseFuture(mReq.getUniqueCode(), timeoutMillis, invokeCallback,
						once);
				this.responseTable.put(mReq.getUniqueCode(), childrenResp);
			}
		}
		else
		{
			this.responseTable.put(request.getOpaque(), resp);
		}
	}

	/** 释放信号量 */
	private void batchRelease(final SedaCommand request, ResponseFuture resp)
	{
		if (null != request.readCustomHanders())
		{
			// 记录所有请求
			for (CustomMessageHeader msg : request.readCustomHanders())
			{
				MessageRequest mReq = (MessageRequest) msg;
				ResponseFuture respFuture = responseTable.get(mReq.getUniqueCode());
				respFuture.release();
			}
		}
		else
		{
			resp.release();
		}
	}

	/**
	 * 单向请求 暂时没有实现，批量提交
	 * @param channel Channel
	 * @param request SedaCommand
	 * @param timeoutMillis timeout
	 * @throws InterruptedException
	 * @throws RemotingTooMuchRequestException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 */
	public void invokeOneway(final Channel channel, final SedaCommand request, long timeoutMillis)
			throws InterruptedException, RemotingTooMuchRequestException, RemotingSendRequestException
	{
		// 设置请求的类型
		request.setCommandType(CommandType.ONEWAY_REQUEST);
		// 获取信号量
		int arg = getPermits(request);

		Semaphore semaphoreOneway = semaphoreOnewayMap.get(Helper.parseChannelRemoteAddr(channel));

		if (null == semaphoreOneway)
		{
			semaphoreOneway = this.semaphoreOneway;
		}

		boolean acquired = semaphoreOneway.tryAcquire(arg, timeoutMillis, TimeUnit.MILLISECONDS);

		if (acquired)
		{
			final ReleaseSemaphoreOnlyOnce once = new ReleaseSemaphoreOnlyOnce(semaphoreOneway);
			try
			{
				channel.writeAndFlush(request).addListener(new ChannelFutureListener()
				{
					public void operationComplete(ChannelFuture f)
					{
						once.release();

						if (!f.isSuccess())
						{
							log.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
							log.warn(request.toString());
						}
					}
				});
			}
			catch (Exception e)
			{
				once.release();
				log.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
				throw new RemotingSendRequestException(Helper.parseChannelRemoteAddr(channel), e);
			}
		}
		else
		{
			//请求锁超时
			throwException(request, timeoutMillis, "invokeOneway", semaphoreOneway);
		}

	}

	/** 获取信号量 */
	private int getPermits(final SedaCommand request)
	{
		int arg = 1;
		if (null != request.readCustomHanders())
		{
			arg = request.readCustomHanders().length;
		}
		return arg;
	}

	/**
	 * 处理响应
	 * @param ctx ChannelHandlerContext
	 * @param cmd SedaCommand
	 */
	public void processResponse(ChannelHandlerContext ctx, SedaCommand cmd)
	{
		final ResponseFuture respFuture = responseTable.get(cmd.getOpaque());

		// 判断回调线程是否启动
		boolean isRunByCallbackThread = false;

		try
		{
			if (null == respFuture)
			{
				log.warn("receive response, but not matched any request, "
						+ Helper.parseChannelRemoteAddr(ctx.channel()));
				log.warn(cmd.toString());
				return;
			}
			respFuture.setRemoteAddr(Helper.parseChannelRemoteAddr(ctx.channel()));

			InvokeCallBack call = respFuture.getInvokeCallback();

			respFuture.setRespCommand(cmd);
			// 异步
			respFuture.release();

			if (null == call)
			{
				// 请求为同步机制
				respFuture.putResponse(cmd);
			}
			else
			{
				ExecutorService executor = this.getCallbackExecutor();
				if (null == executor)
				{
					isRunByCallbackThread = true;
				}
				else
				{
					try
					{
						executor.submit(new Runnable()
						{
							public void run()
							{
								respFuture.executeInvokeCallback();
							}
						});
					}
					catch (Exception e)
					{
						isRunByCallbackThread = true;
						log.warn("excute callback in executor exception, maybe executor busy", e);
					}
				}
			}
		}
		finally
		{
			if (isRunByCallbackThread)
			{
				respFuture.executeInvokeCallback();
			}
			responseTable.remove(cmd.getOpaque());
		}
	}

	/**
	 * 处理请求核心
	 * @param ctx 通道的上下文
	 * @param command 交互对象
	 */
	public void processRequest(final ChannelHandlerContext ctx, final SedaCommand command)
	{
		final Pair<ISedaProcessor, ExecutorService> matched = this.processors.get(command.getEventType());

		final Pair<ISedaProcessor, ExecutorService> pair = (null == matched) ? this.defaultReqProcessor : matched;

		if (null == pair)
		{
			String error = " request type " + command.getEventType().toString() + " not supported";

			final SedaCommand response = SedaCommand
					.createResponseCommand(EventType.REQUEST_EVENT_NOT_SUPPORTED, error);

			response.setOpaque(command.getOpaque());

			ctx.writeAndFlush(response);

			log.error(Helper.parseChannelRemoteAddr(ctx.channel()) + error);

			return;
		}
		Runnable run = new Runnable()
		{
			public void run()
			{
				// 这里可以考虑在做任务之前：需要做的事情；之后还需做的事情。
				try
				{
					beforeRequest(ctx.channel(), command);

					SedaCommand resp = pair.getObject1().processRequest(ctx, command);

					setResponse(ctx, command, resp);
				}
				catch (Throwable e)
				{
					log.error("process request exception", e);
					log.error(command.toString());
					if (!isOnewayReq)
					{
						MessageResponse mResp = new MessageResponse(command.getOpaque());
						final SedaCommand response = SedaCommand.createResponseCommand(command.getEventType(), mResp);//
						response.setDescribe(Helper.exceptionSimpleDesc(e));
						response.setOpaque(command.getOpaque());
						
						String returnCode = SedaConstants.CALL_EXCEPTION;
						String msg = "<" + mResp.getUniqueCode() + ">:" + response.getDescribe();
						if (e instanceof AopException)
						{
							AopException newex = (AopException) e;
							//returnCode = String.valueOf(newex.getMsgId());
							msg = newex.getMessage();
						}
						mResp.setReturnMsg(msg);
						mResp.setReturnCode(returnCode);

						// 记录响应日志
						afterResponse(ctx.channel(), command, response);

						ctx.writeAndFlush(response);
					}
				}

			}

			/**
			 * 设置 Responses
			 * @param ctx
			 * @param command
			 * @param resp
			 */
			private void setResponse(final ChannelHandlerContext ctx, final SedaCommand command, SedaCommand resp)
			{
				afterResponse(ctx.channel(), command, resp);
				switch (command.getCommandType())
				{
					case ASYNC_REQUEST:
					case SYNC_REQUEST:
						writeResp(ctx, command, resp);
						break;
					case ONEWAY_REQUEST:
						setOnewayReq(true);
						break;
					default:
						writeResp(ctx, command, resp);
						break;
				}
			}

			/**
			 * @param ctx
			 * @param command
			 * @param resp
			 */
			private void writeResp(final ChannelHandlerContext ctx, final SedaCommand command, SedaCommand resp)
			{
				setOnewayReq(false);
				if (null != resp)
				{
					resp.setCommandType(CommandType.RESPONSES);
					resp.setOpaque(command.getOpaque());
					try
					{
						// 回响应
						ctx.writeAndFlush(resp);
					}
					catch (Exception e)
					{
						log.error("process request over, but response failed", e);
						log.error(command.toString());
						log.error(resp.toString());
					}
				}
				// resp 为空的情况，可能是在processRequest中处理过了，所以类似情况忽略
			}

		};

		try
		{
			// 这里需要做流控，要求线程池对应的队列必须是有大小限制的
			pair.getObject2().submit(run);
		}
		catch (RejectedExecutionException e)
		{
			// 每个线程10s打印一次
			if ((System.currentTimeMillis() % 10000) == 0)
			{
				log.warn(Helper.parseChannelRemoteAddr(ctx.channel()) //
						+ ", too many requests and system thread pool busy, RejectedExecutionException " //
						+ pair.getObject2().toString() //
						+ " request event: " + command.getEventType());
			}

			if (isOnewayReq())
			{
				final SedaCommand response = SedaCommand.createResponseCommand(EventType.SYSTEM_BUSY,
						"too many requests and system thread pool busy, please try another server");
				response.setOpaque(command.getOpaque());
				ctx.writeAndFlush(response);
			}
		}

	}

	/**
	 * 设置异常信息
	 * @param request request
	 * @param timeoutMillis timeout
	 * @throws RemotingTooMuchRequestException
	 * @throws RemotingTimeoutException
	 */
	private void throwException(final SedaCommand request, long timeoutMillis, String methodName, Semaphore semaphore)
			throws RemotingTooMuchRequestException
	{
		if (timeoutMillis <= 0)
		{
			throw new RemotingTooMuchRequestException(methodName + " invoke too fast");
		}
		else
		{
			String info = String.format(methodName
					+ " tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreValue: %d", //
					timeoutMillis, //
					semaphore.getQueueLength(), //
					semaphore.availablePermits()//
					);
			log.warn(info);
			log.warn(request.toString());
			throw new RemotingTooMuchRequestException(info);
		}
	}

	/**
	 * @return the isOnewayReq
	 */
	private boolean isOnewayReq()
	{
		return isOnewayReq;
	}

	private void setOnewayReq(boolean isOnewayReq)
	{
		this.isOnewayReq = isOnewayReq;
	}

	/**
	 * 处理超时请求
	 */
	public void scanResponseTable()
	{
		Iterator<Entry<String, ResponseFuture>> it = this.responseTable.entrySet().iterator();

		while (it.hasNext())
		{
			Entry<String, ResponseFuture> next = it.next();

			ResponseFuture rep = next.getValue();

			if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 600) <= System.currentTimeMillis())
			{
				// 清楚超时请求
				it.remove();
				try
				{
					// 并返回响应
					rep.executeInvokeCallback();
				}
				catch (Throwable e)
				{
					log.warn("scanResponseTable, operationComplete Exception", e);
				}
				finally
				{
					rep.release();
				}

				log.warn("remove timeout request, " + rep);
			}
		}
	}

	private void beforeRequest(Channel channel, SedaCommand request)
	{
		if (null != this.getRpcInterceptor())
		{
			getRpcInterceptor().beforeRequest(request);
		}
	}

	private void afterResponse(Channel channel, SedaCommand request, SedaCommand response)
	{
		if (null != this.getRpcInterceptor())
		{
			String addr = Helper.parseChannelRemoteAddr(channel);
			getRpcInterceptor().afterResponse(addr, request, response);
		}
	}
}
