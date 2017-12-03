package com.ai.aif.seda.service;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ai.aif.seda.InvokeCallBack;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.command.SedaDecoder;
import com.ai.aif.seda.command.SedaEncoder;
import com.ai.aif.seda.common.Pair;
import com.ai.aif.seda.common.SedaThreadFactory;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.RemotingSendRequestException;
import com.ai.aif.seda.exception.RemotingTimeoutException;
import com.ai.aif.seda.exception.RemotingTooMuchRequestException;
import com.ai.aif.seda.interceptor.RpcInterceptor;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.NettyConfig;
import com.ai.aif.seda.processor.ISedaProcessor;
import com.ai.aif.seda.service.handler.ConnetManageHandler;
import com.ai.aif.seda.service.handler.SedaServerHandler;
import com.ai.aif.seda.utils.LogUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * @Title: 通信服务接口的实现
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class SedaRemotingServiceImpl extends AbstractSedaService implements ISedaService
{
	// private static final Logger log = LogManager.getLogger(SedaRemotingServiceImpl.class);
	private static final ILog<?> log = LogUtils.getILog(SedaRemotingServiceImpl.class);

	// 定时器
	private final Timer timer = new Timer("ServerHouseKeepingService", true);

	private final ServerBootstrap serverBootstrap;
	private final EventLoopGroup eventLoopGroupWorker;
	private final EventLoopGroup eventLoopGroupBoss;
	// 处理Callback应答器
	private final ExecutorService publicExecutor;
	// 配置消息
	private final NettyConfig config;

	private final SedaRemotingServiceImpl serviceImpl = this;

	private DefaultEventExecutorGroup defaultEventExecutorGroup;

	private RpcInterceptor rpc;

	// 本地server绑定的端口
	private int port = 0;

	/**
	 * Service Constructor
	 * 
	 * @param config
	 *            配置
	 */
	public SedaRemotingServiceImpl(NettyConfig config)
	{
		super(config.getsOnewaySemaphoreValue(), config.getsAsyncSemaphoreValue());

		this.config = config;
		int pThreadNumber = config.getsCallbackExecutorThreads();
		if (pThreadNumber <= 0)
		{
			// 默认4个
			pThreadNumber = 4;
		}
		this.serverBootstrap = new ServerBootstrap();
		this.eventLoopGroupBoss = new NioEventLoopGroup(1, new SedaThreadFactory("SedaBossSelector_"));// BOSS
																										// 只需要一个线程就可以满足
		this.eventLoopGroupWorker = new NioEventLoopGroup(config.getsSelectorThreads(), //
				new SedaThreadFactory("SedaServerSelector_" + config.getsSelectorThreads()));
		// 处理异步回调线程组
		this.publicExecutor = Executors.newFixedThreadPool(pThreadNumber,
				new SedaThreadFactory("ServerPublicExecutor_"));
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaRemotingService#startup()
	 */
	public void startup()
	{
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(config.getsWorkerThreads(),
				new SedaThreadFactory("SedaServerWorkerThread_"));

		ServerBootstrap childHandler = this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupWorker);

		childHandler.channel(NioServerSocketChannel.class);
		childHandler.option(ChannelOption.SO_BACKLOG, 1024);
		childHandler.option(ChannelOption.SO_REUSEADDR, true);
		childHandler.option(ChannelOption.SO_KEEPALIVE, true);
		childHandler.childOption(ChannelOption.TCP_NODELAY, true);
		childHandler.option(ChannelOption.SO_SNDBUF, config.getsSocketSndBufSize());
		childHandler.option(ChannelOption.SO_RCVBUF, config.getsSocketRcvBufSize());
		childHandler.localAddress(new InetSocketAddress(this.config.getListenPort()));
		// childHandler.handler(new LoggingHandler(LogLevel.DEBUG));
		childHandler.childHandler(new ChannelInitializer<SocketChannel>()
		{
			@Override
			protected void initChannel(SocketChannel ch) throws Exception
			{
				ChannelPipeline pip = ch.pipeline();
				pip.addLast(defaultEventExecutorGroup, //
						new SedaEncoder(), //
						new SedaDecoder(), //
						new IdleStateHandler(0, 0, config.getsChannelMaxIdleTimeSeconds()), //
						new SedaServerHandler(serviceImpl), //
						new ConnetManageHandler());
			}
		});

		if (config.issPooledByteBufAllocatorEnable())
		{
			// 这个选项有可能会占用大量堆外内存，暂时不使用。
			childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		}

		try
		{
			//启动监听
			ChannelFuture sync = serverBootstrap.bind().sync();
			InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();

			this.port = addr.getPort();

			if (sync.isSuccess())
			{
				log.info("Service start complete. listening Port:{}", port);
			}

			// sync.channel().closeFuture().sync();
		}
		catch (InterruptedException e1)
		{
			throw new RuntimeException("serverBootstrap.bind().sync() InterruptedException", e1);
		}

		startTimer();
	}

	/** 定时器启动 */
	private void startTimer()
	{
		// 每隔1秒扫描下异步调用超时情况
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
			if (this.timer != null)
			{
				this.timer.cancel();
			}

			this.eventLoopGroupBoss.shutdownGracefully();

			this.eventLoopGroupWorker.shutdownGracefully();

			if (this.defaultEventExecutorGroup != null)
			{
				this.defaultEventExecutorGroup.shutdownGracefully();
			}
		}
		catch (Exception e)
		{
			log.error("NettyRemotingServer shutdown exception, ", e);
		}

		if (this.publicExecutor != null)
		{
			try
			{
				this.publicExecutor.shutdown();
			}
			catch (Exception e)
			{
				log.error("NettyRemotingServer shutdown exception, ", e);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaService#registerProcessor(com.ai.aif.seda.event.EventType,
	 * com.ai.aif.seda.processor.ISedaProcessor, java.util.concurrent.ExecutorService)
	 */
	public void registerProcessor(EventType eventType, ISedaProcessor processor, ExecutorService exService)
	{
		ExecutorService tempService = exService;
		// 如果未指定Executor，侧使公共的Executor
		if (null == tempService)
		{
			tempService = publicExecutor;
		}

		processors.put(eventType, new Pair<ISedaProcessor, ExecutorService>(processor, tempService));

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaService#registerDefaultProcessor(com.ai.aif.seda.processor.ISedaProcessor,
	 * java.util.concurrent.ExecutorService)
	 */
	public void registerDefaultProcessor(ISedaProcessor processor, ExecutorService exService)
	{
		this.defaultReqProcessor = new Pair<ISedaProcessor, ExecutorService>(processor, exService);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaService#getProcessorPair(com.ai.aif.seda.event.EventType)
	 */
	public Pair<ISedaProcessor, ExecutorService> getProcessorPair(EventType type)
	{
		return processors.get(type);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaService#invokeSync(io.netty.channel.Channel,
	 * com.ai.aif.seda.command.SedaCommand, long)
	 */
	public SedaCommand invokeSync(Channel channel, SedaCommand request, long timeoutMillis)
			throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException
	{
		return super.invokeSync(channel, request, timeoutMillis);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaService#invokeAsync(io.netty.channel.Channel,
	 * com.ai.aif.seda.command.SedaCommand, long, com.ai.aif.seda.InvokeCallBack)
	 */
	public void invokeAsync(Channel channel, SedaCommand request, long timeoutMillis, InvokeCallBack invokeCallback)
			throws InterruptedException, RemotingTooMuchRequestException, RemotingSendRequestException
	{
		super.invokeAsync(channel, request, timeoutMillis, invokeCallback);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.service.ISedaService#invokeOneway(io.netty.channel.Channel,
	 * com.ai.aif.seda.command.SedaCommand, long)
	 */
	public void invokeOneway(Channel channel, SedaCommand request, long timeoutMillis)
			throws InterruptedException, RemotingTooMuchRequestException, RemotingSendRequestException
	{
		super.invokeOneway(channel, request, timeoutMillis);
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
	 * @see com.ai.aif.seda.service.ISedaService#getLocalListenPort()
	 */
	public int getLocalListenPort()
	{
		return this.port;
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
		return rpc;
	}
}
