package com.ai.aif.seda.app.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ai.aif.seda.InvokeCallBack;
import com.ai.aif.seda.client.ISedaClient;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.command.SedaDecoder;
import com.ai.aif.seda.command.SedaEncoder;
import com.ai.aif.seda.common.ChannelWrapper;
import com.ai.aif.seda.common.Helper;
import com.ai.aif.seda.common.Pair;
import com.ai.aif.seda.common.SedaThreadFactory;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.RemotingConnectException;
import com.ai.aif.seda.exception.RemotingSendRequestException;
import com.ai.aif.seda.exception.RemotingTimeoutException;
import com.ai.aif.seda.exception.RemotingTooMuchRequestException;
import com.ai.aif.seda.handler.CConnectManageHandler;
import com.ai.aif.seda.handler.SedaClientHandler;
import com.ai.aif.seda.interceptor.RpcInterceptor;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.NettyConfigByClient;
import com.ai.aif.seda.processor.ISedaProcessor;
import com.ai.aif.seda.service.AbstractSedaService;
import com.ai.aif.seda.utils.LogUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * @Title: Seda 客户端
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月22日
 * @Version: 1.0
 */
public class SedaClient extends AbstractSedaService implements ISedaClient
{
	// private static final Logger log = LogManager.getLogger(SedaClient.class);
	private static final ILog<?> log = LogUtils.getILog(SedaClient.class);

	/** 等待锁的最长时间 */
	private static final long LOCKTIMEOUTMILLIS = 3000;

	private final Lock channelLock = new ReentrantLock();

	private final Lock lockNamesrvChannel = new ReentrantLock();

	private final ConcurrentHashMap<String /* addr */, ChannelWrapper> channelMap = new ConcurrentHashMap<String, ChannelWrapper>();

	private final AtomicReference<List<String>> namesrvAddrList = new AtomicReference<List<String>>();

	private final AtomicReference<String> namesrvAddrChoosed = new AtomicReference<String>();

	private final AtomicInteger namesrvIndex = new AtomicInteger(initValueIndex());

	// 定时器
	private final Timer timer = new Timer("ClientHouseKeepingService", true);

	private NettyConfigByClient config;

	// 处理Callback应答器
	private final ExecutorService publicExecutor;

	private final Bootstrap bootstrap = new Bootstrap();
	private final EventLoopGroup eventLoopGroupWorker;
	private DefaultEventExecutorGroup defaultEventExecutorGroup;
	private SedaClient client = this;
	private RpcInterceptor rpc;

	/**
	 * @param onewaPermits
	 * @param asyncPermits
	 */
	public SedaClient(NettyConfigByClient config)
	{
		super(config.getOnewaySemaphore(), config.getAsyncSemaphore());

		this.config = config;

		int publicThreadNums = config.getClientCallbackExecutorThreads();

		if (publicThreadNums <= 0)
		{
			publicThreadNums = 4;
		}

		this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new SedaThreadFactory(
				"SedaClientPublicExecutor_"));

		eventLoopGroupWorker = new NioEventLoopGroup(1, new SedaThreadFactory("SedaClientSelector_"));

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.app.AbstractSedaService#getCallbackExecutor()
	 */
	@Override
	public ExecutorService getCallbackExecutor()
	{
		return this.publicExecutor;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.ISedaClient#updateNameServerAddressList(java.util.List)
	 */
	public void updateNameServerAddressList(List<String> addrs)
	{
		List<String> old = this.namesrvAddrList.get();

		boolean update = false;

		if (!addrs.isEmpty())
		{
			if (null == old)
			{
				update = true;
			}
			else if (addrs.size() != old.size())
			{
				update = true;
			}
			else
			{
				for (int i = 0; i < addrs.size() && !update; i++)
				{
					if (!old.contains(addrs.get(i)))
					{
						update = true;
					}
				}
			}

			if (update)
			{
				// 随机转换下    
				Collections.shuffle(addrs);

				this.namesrvAddrList.set(addrs);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.ISedaClient#getNameServerAddressList()
	 */
	public List<String> getNameServerAddressList()
	{
		return namesrvAddrList.get();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.ISedaClient#invokeSync(java.lang.String, com.ai.aif.seda.command.SedaCommand, long)
	 */
	public SedaCommand invokeSync(String addr, SedaCommand request, long timeoutMillis) throws InterruptedException,
			RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException
	{
		final Channel channel = getAndCreateChannel(addr);

		if (null != channel && channel.isActive())
		{
			beforeRequest(request);
			SedaCommand response = null;
			try
			{
				response = this.invokeSync(channel, request, timeoutMillis);
				// afterResponse(addr, request, response);
				return response;
			}
			catch (RemotingSendRequestException e)
			{
				closeChannel(addr, channel);
				log.warn("invokeSync: send request exception, so close the channel[" + addr + "]");
				throw e;
			}
			catch (RemotingTimeoutException e)
			{
				closeChannel(addr, channel);
				log.warn("invokeSync: wait response timeout exception, the channel[" + addr + "]");
				throw e;
			}
			finally
			{
				afterResponse(addr, request, response);
			}
		}
		else
		{
			closeChannel(addr, channel);
			throw new RemotingConnectException(addr);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.ISedaClient#invokeAsync(java.lang.String, com.ai.aif.seda.command.SedaCommand, long,
	 * com.ai.aif.seda.InvokeCallBack)
	 */
	public void invokeAsync(String addr, SedaCommand request, long timeoutMillis, InvokeCallBack invokeCallback)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingSendRequestException
	{
		final Channel channel = this.getAndCreateChannel(addr);
		if (channel != null && channel.isActive())
		{
			// beforeRequest(addr, request);
			try
			{
				this.invokeAsync(channel, request, timeoutMillis, invokeCallback);
			}
			catch (RemotingSendRequestException e)
			{
				log.warn("invokeAsync: send request exception, so close the channel[" + addr + "]");
				this.closeChannel(addr, channel);
				throw e;
			}
		}
		else
		{
			this.closeChannel(addr, channel);
			throw new RemotingConnectException(addr);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.ISedaClient#invokeOneway(java.lang.String, com.ai.aif.seda.command.SedaCommand, long)
	 */
	public void invokeOneway(String addr, SedaCommand request, long timeoutMillis) throws InterruptedException,
			RemotingConnectException, RemotingTooMuchRequestException, RemotingTimeoutException,
			RemotingSendRequestException
	{
		final Channel channel = this.getAndCreateChannel(addr);
		if (channel != null && channel.isActive())
		{
			beforeRequest(request);
			try
			{
				this.invokeOneway(channel, request, timeoutMillis);
			}
			catch (RemotingSendRequestException e)
			{
				log.warn("invokeOneway: send request exception, so close the channel[" + addr + "]");
				this.closeChannel(addr, channel);
				throw e;
			}
		}
		else
		{
			this.closeChannel(addr, channel);
			throw new RemotingConnectException(addr);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.ISedaClient#registerProcessor(com.ai.aif.seda.event.EventType,
	 * com.ai.aif.seda.processor.ISedaProcessor, java.util.concurrent.ExecutorService)
	 */
	public void registerProcessor(EventType evnet, ISedaProcessor processor, ExecutorService executor)
	{
		ExecutorService executorThis = executor;
		if (null == executor)
		{
			executorThis = this.publicExecutor;
		}
		this.processors.put(evnet, new Pair<ISedaProcessor, ExecutorService>(processor, executorThis));
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.ISedaClient#isChannelWriteable(java.lang.String)
	 */
	public boolean isChannelWriteable(String addr)
	{
		ChannelWrapper cw = this.channelMap.get(addr);

		if (cw != null && cw.isOK())
		{
			return cw.isWriteable();
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaRemotingService#startup()
	 */
	public void startup()
	{
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(config.getClientWorkerThreads(),
				new SedaThreadFactory("SedaClientWorkerThread_"));

		Bootstrap handler = this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class);
		handler.option(ChannelOption.TCP_NODELAY, true);
		handler.option(ChannelOption.SO_KEEPALIVE, true);
		handler.option(ChannelOption.SO_SNDBUF, config.getClientSocketSndBufSize());
		handler.option(ChannelOption.SO_RCVBUF, config.getClientSocketRcvBufSize());
		handler.handler(new LoggingHandler(LogLevel.DEBUG));
		handler.handler(new ChannelInitializer<SocketChannel>()
		{
			@Override
			public void initChannel(SocketChannel ch) throws Exception
			{
				ch.pipeline().addLast(//
						defaultEventExecutorGroup, //
						new SedaEncoder(), //
						new SedaDecoder(), //
						new IdleStateHandler(0, 0, config.getClientChannelMaxIdleTimeSeconds()), //
						new CConnectManageHandler(client), //
						new SedaClientHandler(client));
			}
		});

		this.timer.scheduleAtFixedRate(new TimerTask()
		{

			@Override
			public void run()
			{
				try
				{
					scanResponseTable();
				}
				catch (Exception e)
				{
					log.error("scanResponseTable exception", e);
				}
			}
		}, 1000 * 3, 1000);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaRemotingService#shutdown()
	 */
	public void shutdown()
	{
		try
		{
			this.timer.cancel();

			for (ChannelWrapper cw : this.channelMap.values())
			{
				this.closeChannel(null, cw.getChannel());
			}
			this.channelMap.clear();

			this.eventLoopGroupWorker.shutdownGracefully();

			if (this.defaultEventExecutorGroup != null)
			{
				this.defaultEventExecutorGroup.shutdownGracefully();
			}
		}
		catch (Exception e)
		{
			log.error("SedaClient shutdown exception, ", e);
		}

		if (this.publicExecutor != null)
		{
			try
			{
				this.publicExecutor.shutdown();
			}
			catch (Exception e)
			{
				log.error("SedaClient shutdown exception, ", e);
			}
		}

	}

	/**
	 * 关闭连接
	 * @param channel
	 */
	public void closeChannel(final Channel channel)
	{
		if (null == channel)
		{
			return;
		}

		try
		{
			if (this.channelLock.tryLock(LOCKTIMEOUTMILLIS, TimeUnit.MILLISECONDS))
			{
				try
				{
					boolean isRemove = true;
					ChannelWrapper prevCW = null;
					String addrRemote = null;

					for (Map.Entry<String, ChannelWrapper> en : channelMap.entrySet())
					{
						ChannelWrapper prev = en.getValue();
						if (prev.getChannel() != null)
						{
							if (prev.getChannel() == channel)
							{
								prevCW = prev;
								addrRemote = en.getKey();
								break;
							}
						}
					}

					if (null == prevCW)
					{
						log.info("eventCloseChannel: the channel[" + addrRemote
								+ "] has been removed from the channel table before");
						isRemove = false;
					}

					if (isRemove)
					{
						this.channelMap.remove(addrRemote);
						log.info("closeChannel: the channel[" + addrRemote + "] was removed from channel table");
						Helper.closeChannel(channel);
					}
				}
				catch (Exception e)
				{
					log.error("closeChannel: close the channel exception", e);
				}
				finally
				{
					this.channelLock.unlock();
				}
			}
			else
			{
				log.warn("closeChannel: try to lock channel table, but timeout, " + LOCKTIMEOUTMILLIS + "ms");
			}
		}
		catch (InterruptedException e)
		{
			log.error("closeChannel exception", e);
		}
	}

	/**
	 * 关闭连接
	 * @param addr 地址
	 * @param channel 通道
	 */
	public void closeChannel(final String addr, final Channel channel)
	{
		if (null == channel)
		{
			return;
		}
		final String addrRemote = (null == addr) ? Helper.parseChannelRemoteAddr(channel) : addr;
		try
		{
			if (this.channelLock.tryLock(LOCKTIMEOUTMILLIS, TimeUnit.MILLISECONDS))
			{
				try
				{
					boolean isRemove = true;

					final ChannelWrapper prevCW = this.channelMap.get(addrRemote);

					log.info("closeChannel: begin close the channel[" + addrRemote + "] Found: " + (prevCW != null));

					if (null == prevCW)
					{
						log.info("closeChannel: the channel[" + addrRemote
								+ "] has been removed from the channel table before");
						isRemove = false;
					}
					else if (prevCW.getChannel() != channel)
					{
						log.info("closeChannel: the channel[" + addrRemote
								+ "] has been closed before, and has been created again, nothing to do.");
						isRemove = false;
					}
					else
					{
						// no nothing
					}
					if (isRemove)
					{
						this.channelMap.remove(addrRemote);
						log.info("closeChannel: the channel[" + addrRemote + "] was removed from channel table");
					}

					Helper.closeChannel(channel);
				}
				catch (Exception e)
				{
					log.error("closeChannel: close the channel exception", e);
				}
				finally
				{
					this.channelLock.unlock();
				}
			}
			else
			{
				log.warn("closeChannel: try to lock channel table, but timeout, " + LOCKTIMEOUTMILLIS + "ms");
			}
		}
		catch (InterruptedException e)
		{
			log.error("closeChannel exception", e);
		}
	}

	/*** 创建连接 */
	private Channel getAndCreateChannel(final String addr) throws InterruptedException
	{
		// 该情况可以忽略，暂不考虑
		if (null == addr)
		{
			return getAndCreateNameserverChannel();
		}

		return this.createChannel(addr);
	}

	private Channel getAndCreateNameserverChannel() throws InterruptedException
	{
		Channel channel = choiceChannel();

		if (null != channel)
		{
			return channel;
		}

		final List<String> addrList = this.namesrvAddrList.get();

		if (this.lockNamesrvChannel.tryLock(LOCKTIMEOUTMILLIS, TimeUnit.MILLISECONDS))
		{
			try
			{
				channel = choiceChannel();

				if (null != channel)
				{
					return channel;
				}

				if (addrList != null && !addrList.isEmpty())
				{
					for (int i = 0; i < addrList.size(); i++)
					{
						int index = this.namesrvIndex.incrementAndGet();
						index = Math.abs(index);
						index = index % addrList.size();
						String newAddr = addrList.get(index);
						this.namesrvAddrChoosed.set(newAddr);
						Channel channelNew = this.createChannel(newAddr);
						if (channelNew != null)
						{
							return channelNew;
						}
					}
				}
			}
			catch (Exception e)
			{
				log.error("getAndCreateNameserverChannel: create name server channel exception", e);
			}
			finally
			{
				this.lockNamesrvChannel.unlock();
			}
		}
		else
		{
			log.warn("getAndCreateNameserverChannel: try to lock name server, but timeout, " + LOCKTIMEOUTMILLIS + "ms");
		}

		return null;
	}

	/**
	 * 获取通道
	 */
	private Channel choiceChannel()
	{
		String addr = this.namesrvAddrChoosed.get();
		if (addr != null)
		{
			ChannelWrapper cw = this.channelMap.get(addr);
			if (cw != null && cw.isOK())
			{
				return cw.getChannel();
			}
		}
		return null;
	}

	/*** 创建新的连接 */
	private Channel createChannel(String addr) throws InterruptedException
	{
		ChannelWrapper cw = this.channelMap.get(addr);

		if (null != cw && cw.isOK())
		{
			return cw.getChannel();
		}

		if (channelLock.tryLock(LOCKTIMEOUTMILLIS, TimeUnit.MILLISECONDS))
		{
			boolean createNewConn = false;
			try
			{
				cw = channelMap.get(addr);

				if (null != cw)
				{
					// channel 正常
					if (cw.isOK())
					{
						return cw.getChannel();
					}
					// 正常在连接，退出锁等待
					else if (!cw.getChannelFuture().isDone())
					{
						createNewConn = false;
					}
					else
					{
						// 连接不成功
						createNewConn = true;
						channelMap.remove(addr);
					}
				}
				else
				{
					createNewConn = true;
				}

				// 与服务端建立连接
				if (createNewConn)
				{
					ChannelFuture channelFuture = this.bootstrap.connect(Helper.string2SocketAddress(addr));

					log.info("createChannel: begin to connect remote host[{}] asynchronously", addr);
					cw = new ChannelWrapper(channelFuture);
					channelMap.put(addr, cw);
				}
			}
			catch (Exception e)
			{
				log.error("createChannel: create channel exception", e);
			}
			finally
			{
				channelLock.unlock();
			}
		}
		else
		{
			log.warn("createChannel: try to lock channel table, but timeout, {} ms", LOCKTIMEOUTMILLIS);
		}

		return getChannel(cw, addr);
	}

	private Channel getChannel(ChannelWrapper cw, String addr)
	{
		ChannelFuture cf = cw.getChannelFuture();

		long timeout = config.getConnectTimeoutMillis();

		//ChannelFuture可能未处理完操作
		if (cf.awaitUninterruptibly(timeout))
		{
			if (cw.isOK())
			{
				log.info("createChannel: connect remote host [{}] success, {}", addr, cf.toString());
				return cw.getChannel();
			}
			log.warn("createChannel: connect remote host[{}] failed, {}", addr, cf.toString());

		}
		else
		{
			log.warn("createChannel: connect remote host[{}] timeout {} ms, {}", addr, timeout, cf.toString());
		}

		return null;
	}

	private static int initValueIndex()
	{
		Random r = new Random();

		return Math.abs(r.nextInt() % 999) % 999;
	}

	/** 在请求前处理些动着 */
	protected void beforeRequest(SedaCommand request)
	{
		if (null != rpc)
		{
			rpc.beforeRequest(request);
		}
	}

	/** 响应后处理些动着 */
	protected void afterResponse(String addr, SedaCommand request, SedaCommand response)
	{
		if (null != rpc)
		{
			rpc.afterResponse(addr, request, response);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaRemotingService#registerInterceptor(com.ai.aif.seda.interceptor.RpcInterceptor)
	 */
	@Override
	public void registerInterceptor(RpcInterceptor rpc)
	{
		this.rpc = rpc;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.AbstractSedaService#getRpcInterceptor()
	 */
	@Override
	public RpcInterceptor getRpcInterceptor()
	{
		return this.rpc;
	}

}
