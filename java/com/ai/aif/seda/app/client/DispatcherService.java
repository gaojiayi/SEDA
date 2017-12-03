package com.ai.aif.seda.app.client;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.beanutils.BeanUtils;

import com.ai.aif.seda.InvokeCallBack;
import com.ai.aif.seda.app.LoadBalancFactory;
import com.ai.aif.seda.app.interceptor.RpcMessageLog;
import com.ai.aif.seda.app.processor.ClientProcessor;
import com.ai.aif.seda.app.report.ReportProcessor;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.ConfigType;
import com.ai.aif.seda.common.Priority;
import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.common.SedaThreadFactory;
import com.ai.aif.seda.config.Referer;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.config.client.QueueNode;
import com.ai.aif.seda.config.client.ServerNode;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.NoAvailableServicesException;
import com.ai.aif.seda.exception.QueueFullException;
import com.ai.aif.seda.exception.RemotingConnectException;
import com.ai.aif.seda.exception.RemotingException;
import com.ai.aif.seda.exception.RemotingSendRequestException;
import com.ai.aif.seda.exception.RemotingTimeoutException;
import com.ai.aif.seda.exception.RemotingTooMuchRequestException;
import com.ai.aif.seda.heartbeat.IHeartbeat;
import com.ai.aif.seda.loadbalancing.ILoadBalancStrategy;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.CustomMessageHeader;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.message.MessageResponse;
import com.ai.aif.seda.message.NettyConfigByClient;
import com.ai.aif.seda.message.ResponseFuture;
import com.ai.aif.seda.utils.LogUtils;
import com.ai.aif.seda.utils.StringMatchUtils;
import com.ai.aif.seda.utils.SystemUtils;

/**
 * @Title: 用于消息装配、分发
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月13日
 * @Version: 1.0
 */
public class DispatcherService
{
	// private static final Logger log = LogManager.getLogger(DispatcherService.class);
	private static final ILog<?> log = LogUtils.getILog(DispatcherService.class);

	/** Queue分组组 */
	private static final ConcurrentHashMap<String/* Queue id */, LinkedBlockingQueue<CustomMessageHeader>> QUEUES = new ConcurrentHashMap<String, LinkedBlockingQueue<CustomMessageHeader>>();

	/** 消费线程 */
	private static final ConcurrentHashMap<String/* Queue id */, Map<ExecutorService, Integer>> CONSUMER_THREAD = new ConcurrentHashMap<String, Map<ExecutorService, Integer>>();

	/** 记录Queue的消费数 */
	private static final ConcurrentHashMap<String/* Queue id */, AtomicLong> CONSUMER_COUNT = new ConcurrentHashMap<String, AtomicLong>();

	private static final DispatcherService SERVICE = new DispatcherService();

	private static final String DEFAULT_QUEUE_ID = "DEFAULT_ID";

	// 缓存所有对外请求
	protected final ConcurrentHashMap<String, ResponseFuture> responseTable = new ConcurrentHashMap<String, ResponseFuture>();

	private ClientConfig cfg = SedaConfigSource.locadClientCfg();

	private ILoadBalancStrategy<ServerNode> load = LoadBalancFactory.newInstance();

	private ReportProcessor report = new ReportProcessor(this);

	private ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1, new SedaThreadFactory(
			"Seda_Scheduled"));

	private IHeartbeat<Referer<ServerNode>> heartbeat;

	private SedaClient sedaClient;

	// 业务超时
	private long srvSendTimeout;

	private DispatcherService()
	{
		if (SystemUtils.isService())
		{
			return;
		}

		queuePacket();

		ceonsumerQueue();

		initial();

		try
		{
			heartbeat = newHeartBeatInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		startMonitor();

	}

	@SuppressWarnings("unchecked")
	private IHeartbeat<Referer<ServerNode>> newHeartBeatInstance() throws Exception
	{
		String calssName = cfg.getHeartbeatClassName();

		Constructor<?> con = Class.forName(calssName).getConstructor(SedaClient.class);

		return (IHeartbeat<Referer<ServerNode>>) con.newInstance(sedaClient);
	}

	/*** 执行任务 */
	private void startMonitor()
	{
		//心跳，1秒一次
		schedule.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					heartbeat.ping(cfg.getServers(), cfg.getDefaultConnectTimeout());
				}
				catch (Throwable e)
				{
					log.error("", e);
				}
			}
		}, 1000, 1000, TimeUnit.MILLISECONDS);
		// 30秒一次
		schedule.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					reloadConfig();
				}
				catch (Throwable e)
				{
					log.error("", e);
				}
			}
		}, 1000 * 4, 30000, TimeUnit.MILLISECONDS);
		// 打印资源
		schedule.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					pringlnResource();
				}
				catch (Throwable e)
				{
					log.error("", e);
				}
			}
		}, 1000 * 5, 60000, TimeUnit.MILLISECONDS);
		//上报数据
		schedule.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				report.report();
			}
		}, 2000, 30000, TimeUnit.MILLISECONDS);
	}

	/** 初始Netty 通道 */
	private void initial()
	{
		NettyConfigByClient cfg = getConfig();

		sedaClient = new SedaClient(cfg);

		// 暂时用不到
		sedaClient.registerProcessor(EventType.CALL_THIRD_PARTY_SERVICE, new ClientProcessor(), null);
		sedaClient.registerInterceptor(new RpcMessageLog(this.cfg));
		sedaClient.startup();
	}

	/** 队列分组 */
	private void queuePacket()
	{
		Set<QueueNode> nodes = cfg.getQueues();

		// 分组类型未匹配上，落入default
		QueueNode defaultQueue = new QueueNode();
		defaultQueue.setQueueId(DEFAULT_QUEUE_ID);
		defaultQueue.setPr(Priority.M);

		Set<QueueNode> tempSet = new HashSet<QueueNode>();
		tempSet.add(defaultQueue);
		tempSet.addAll(nodes);

		for (QueueNode node : tempSet)
		{
			int size = node.getSize();
			if (size == 0)
			{
				size = cfg.getDefaultQueueSize();
			}
			LinkedBlockingQueue<CustomMessageHeader> queue = new LinkedBlockingQueue<CustomMessageHeader>(size);
			QUEUES.putIfAbsent(node.getQueueId(), queue);
			CONSUMER_COUNT.putIfAbsent(node.getQueueId(), new AtomicLong(0));
			newCeonsumerThreads(node);
		}

		log.info("Queue group success(group size:{}, {})", QUEUES.size(), QUEUES.keySet());

	}

	/** 创建消费线程 */
	private void newCeonsumerThreads(QueueNode node)
	{
		float priority = cfg.getRatioMap().get(node.getPr());

		// 线程个数
		int count = (int) (priority * cfg.getDefaultConsumerThreads());

		ExecutorService executor = Executors.newFixedThreadPool(count, new SedaThreadFactory(node.getQueueId()
				+ "_consumer_thread_"));

		Map<ExecutorService, Integer> temp = new HashMap<ExecutorService, Integer>();
		temp.put(executor, count);

		CONSUMER_THREAD.putIfAbsent(node.getQueueId(), temp);

		log.info("Consumer({}) threads({}) create success.", node.getQueueId(), count);
	}

	/**
	 * 创建消费队列
	 */
	private void ceonsumerQueue()
	{
		for (Map.Entry<String, Map<ExecutorService, Integer>> en : CONSUMER_THREAD.entrySet())
		{
			//根据queue id  获取指定消费队列
			LinkedBlockingQueue<CustomMessageHeader> queue = QUEUES.get(en.getKey());

			for (Map.Entry<ExecutorService, Integer> exn : en.getValue().entrySet())
			{
				//队列ID    线程数         线程池     队列
				start(en.getKey(), exn.getValue(), exn.getKey(), queue);
			}
		}
	}

	/**
	 * @param id Queue ID
	 * @param count 线程个数
	 * @param exec 线程组
	 * @param queue Queue
	 */
	private void start(final String id, int count, ExecutorService exec,
			final LinkedBlockingQueue<CustomMessageHeader> queue)
	{
		for (int i = 0; i < count; i++)
		{
			//每个线程批量消费
			exec.submit(new Runnable()
			{
				@Override
				public void run()
				{
					for (;;)
					{
						List<CustomMessageHeader> list = new ArrayList<CustomMessageHeader>();
						ServerNode server = null;
						try
						{
							list.add(queue.take());

							// 拿服务
							server = load.algorithm(cfg.getServers());

							if (null != server)
							{
								// 获取信号量
								Semaphore asyncHore = sedaClient.semaphoreAsyncMap.get(getAddr(server));
								// 取可用信号量、批量数之间的最小值
								int minValue = Math.min(cfg.getSendBatchSize(), asyncHore.availablePermits());

								if (minValue > 1)
								{
									 //最多从此队列中移除给定数量的可用元素，并将这些元素添加到给定 list 中。
									//minValue - 1 是因为queue.take()已经获取到一个
									queue.drainTo(list, minValue - 1);
								}

								// 记录调用数   
								server.increment(list.size());
								//记录每个队列消费数
								CONSUMER_COUNT.get(id).addAndGet(list.size());
							}
							sendMessage(server, list);
						}
						catch (Throwable e)
						{
							// 异常时释放
							if (null != server)
							{
								//回滚激励
								server.decrement(list.size());
								CONSUMER_COUNT.get(id).addAndGet(0 - list.size());
							}
							try
							{
								handleExectption(e, queue, list);
							}
							catch (Throwable e1)
							{
								log.error("", e1);
							}
						}
					}
				}
			});

		}
	}

	protected void sendMessage(final ServerNode server, List<CustomMessageHeader> header)
			throws RemotingSendRequestException, RemotingConnectException, RemotingTooMuchRequestException,
			InterruptedException, NoAvailableServicesException
	{
		if (null == server)
		{
			throw new NoAvailableServicesException("No available services");
		}
		final String addr = getAddr(server);

		CustomMessageHeader[] array = new CustomMessageHeader[header.size()];

		header.toArray(array);

		final SedaCommand request = SedaCommand.createRequestCommand(EventType.CALL_THIRD_PARTY_SERVICE, array);

		log.debug("Send Request: {}\nInclude {} requests : {}", request, header.size(), array);

		// 批量发送
		// cfg.getSendTimeout()
		sedaClient.invokeAsync(addr, request, getSrvSendTimeout(), new InvokeCallBack()
		{
			@Override
			public void operationComplete(ResponseFuture responseFuture)
			{
				//服务并发量减一
				server.decrement();

				ResponseFuture respf = responseTable.get(responseFuture.getOpaque());

				if (null == respf)
				{
					log.error("receive response, but not matched any request, {}\n", responseFuture);
					return;
				}
				respf.setRemoteAddr(responseFuture.getRemoteAddr());

				try
				{
					SedaCommand respCommand = responseFuture.getRespCommand();

					//超时
					if (null == respCommand)
					{
						log.error("request timeout.");
						setExResp(respf, null, SedaConstants.TIMEOUT,
								"Request timeout. unique code:<" + respf.getOpaque() + ">");
						return;
					}
					CustomMessageHeader[] msgHeader = respCommand.readCommandHeader(MessageResponse.class);
					//获取响应 但是结果为空
					if (null == msgHeader)
					{
						log.error("receive response, but not result is empty, {}\n", responseFuture);
						setExResp(respf, respCommand, SedaConstants.CALL_EXCEPTION,
								"receive response, but not result is empty. unique code:<" + respf.getOpaque() + ">");
						return;
					}
					respCommand.setMessageHeader(msgHeader);

					outLog(responseFuture);

				}
				catch (Throwable e)
				{
					log.error("", e);
				}
			}

			/** 设置异常响应 */
			private void setExResp(ResponseFuture respFuture, SedaCommand respCommand, String returnCode, String msg)
			{
				MessageResponse resp = new MessageResponse(respFuture.getOpaque());
				resp.setReturnCode(returnCode);
				resp.setReturnMsg(msg);

				if (null == respCommand)
				{
					respCommand = SedaCommand.createResponseCommand(request.getEventType(), resp);
				}
				// 这里不考虑respFuture为空的情况，上面已经过虑
				respFuture.putResponse(respCommand);
			}

			/** 记录日志 */
			private void outLog(ResponseFuture responseFuture)
			{
				log.debug("Receive response : {}", responseFuture);
			}
		});
	}

	private String getAddr(ServerNode node)
	{
		return node.getIp() + ":" + node.getPort();
	}

	/**
	 * put请求消息
	 * @param header
	 */
	public void putMessage(MessageRequest header) throws NullPointerException,//
			QueueFullException, InterruptedException
	{
		if (null == header)
		{
			throw new NullPointerException("CustomMessageHeader object is empty!");
		}
		String queueid = searchQueueId(header);
		if (null == queueid)
		{
			log.warn("Did not match to the queue, go by default.");
			queueid = DEFAULT_QUEUE_ID;
		}

		LinkedBlockingQueue<CustomMessageHeader> queue = QUEUES.get(queueid);

		boolean flag = queue.offer(header, 3, TimeUnit.SECONDS);

		if (!flag)
		{
			throw new QueueFullException("Queue is full. current size = " + queue.size());
		}
	}

	private String searchQueueId(MessageRequest header)
	{
		StringBuilder sb = new StringBuilder("Requested data object:");
		for (Map.Entry<Object, Object> en : header.getData().entrySet())
		{
			sb.append("\n ").append(en.getKey()).append("=").append(en.getValue());
		}
		log.debug(sb);

		String queueid = null;
		for (QueueNode node : cfg.getQueues())
		{
			boolean flag = StringMatchUtils.search(header.getData(), node.getCondition());

			log.debug("matching {} {}", node.getQueueId(), flag ? "successfully" : "failed");

			if (flag)
			{
				queueid = node.getQueueId();
				break;
			}
		}
		return queueid;
	}

	public static DispatcherService newInstance()
	{
		return SERVICE;
	}

	public SedaClient getClient()
	{
		return sedaClient;
	}

	/*** 重新加载 */
	public void reloadConfig()
	{
		File file = SedaConfigSource.getConfigFile(ConfigType.CLIENT);

		// 文件有更新
		if (file.lastModified() > SedaConfigSource.getInitTime())
		{
			log.info("begin - reload Config.");

			ClientConfig config = (ClientConfig) SedaConfigSource.reloadConfig(ConfigType.CLIENT);

			try
			{
				BeanUtils.copyProperties(cfg, config);
				queuePacket();
			}
			catch (Exception e)
			{
				log.error("", e);
			}

			log.info("end - reload Config.");
		}
	}

	/**
	 * 配置
	 */
	private NettyConfigByClient getConfig()
	{
		NettyConfigByClient config = new NettyConfigByClient();
		config.setClientWorkerThreads(cfg.getWorkerThreads());
		config.setClientCallbackExecutorThreads(cfg.getCallBackThreads());
		config.setClientChannelMaxIdleTimeSeconds(cfg.getChannelMaxIdleTimeSeconds());
		config.setConnectTimeoutMillis(cfg.getDefaultConnectTimeout());
		config.setClientSocketRcvBufSize(cfg.getRcvbufSize());
		config.setClientSocketSndBufSize(cfg.getSndbufSize());

		for (Referer<ServerNode> server : cfg.getServers())
		{
			//初始化信号量
			config.addAsyncSemaplhore(getAddr(server.getReferer()), server.getReferer().getConcurrentNumber());
			config.addOnewaySemaplhore(getAddr(server.getReferer()), server.getReferer().getConcurrentNumber());
		}

		return config;
	}

	/** 处理异常， */
	private void handleExectption(Throwable e, LinkedBlockingQueue<CustomMessageHeader> queue,
			List<CustomMessageHeader> list)
	{
		log.error("", e);

		// 没可用服务，直接丢弃消息
		if (e instanceof NoAvailableServicesException || e instanceof RemotingTimeoutException)
		{
			// begin add by yougw at 2016/8/23 for OSPRD-752
			for (CustomMessageHeader reqHeader : list)
			{
				// 通知上层等待的线程
				MessageResponse respCommand = new MessageResponse();
				MessageRequest req = (MessageRequest) reqHeader;
				ResponseFuture resp = responseTable.get(req.getUniqueCode());
				if (null == resp)
				{
					return;
				}
				SedaCommand command = SedaCommand
						.createResponseCommand(EventType.CALL_THIRD_PARTY_SERVICE, respCommand);
				command.setOpaque(req.getUniqueCode());
				RemotingException ex = (RemotingException) e;
				respCommand.setReturnCode(ex.getReturnCode());
				respCommand.setReturnMsg(ex.getLocalizedMessage() + ". unique code:<" + req.getUniqueCode() + ">");
				resp.setRespCommand(command);
			}
			return;
			// end add by yougw at 2016/8/23 for OSPRD-752
		}

		// 除此之外，直接将消息重新放置队列中
		if (e instanceof RemotingTooMuchRequestException)
		{
			log.error("semaphore use end. {} thread sleep 2000ms", Thread.currentThread().getName());
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e1)
			{
				log.error("", e1);
			}
			log.warn("{} thread already activated.", Thread.currentThread().getName());
		}

		// 除此之外，都考虑重新放入队列
		if (e instanceof RemotingConnectException || //
				e instanceof RemotingSendRequestException || //
				e instanceof RemotingTooMuchRequestException)
		{
			log.warn("{} .Re join the consumer queue. message number: {}", e.getLocalizedMessage(), list.size());
			// 将消息，重新放置队列中
			queue.addAll(list);
		}
	}

	/**
	 * @param srvSendTimeout the srvSendTimeout to set
	 */
	public void setSrvSendTimeout(long srvSendTimeout)
	{
		if (srvSendTimeout < 0)
		{
			return;
		}
		this.srvSendTimeout = srvSendTimeout;
	}

	private long getSrvSendTimeout()
	{
		if (this.srvSendTimeout <= 0)
		{
			return cfg.getSendTimeout();
		}
		return this.srvSendTimeout;
	}

	/**
	 * 队列消费的历史数
	 * @return
	 */
	public Map<String, AtomicLong> getConsumerCount()
	{
		return CONSUMER_COUNT;
	}

	/**
	 * 队列资源
	 * @return
	 */
	public Map<String, LinkedBlockingQueue<CustomMessageHeader>> getQueues()
	{
		return QUEUES;
	}

	/**
	 * 消息费线程
	 * @return
	 */
	public Map<String, Map<ExecutorService, Integer>> getConsumerThreads()
	{
		return CONSUMER_THREAD;
	}

	/***
	 * 当前并发数
	 * @param key
	 * @return
	 */
	public Semaphore getAsyncSemaphore(String key)
	{
		return getClient().semaphoreAsyncMap.get(key);
	}

	/** 打印资源 */
	private void pringlnResource()
	{
		StringBuilder sb = new StringBuilder("Resource data:\n");

		for (Map.Entry<String, LinkedBlockingQueue<CustomMessageHeader>> en : QUEUES.entrySet())
		{
			sb.append("Consume Queue(" + en.getKey() + ") size = ").append(en.getValue().size());
			sb.append("\n");
		}

		for (Map.Entry<String, Map<ExecutorService, Integer>> en : CONSUMER_THREAD.entrySet())
		{
			for (Map.Entry<ExecutorService, Integer> exn : en.getValue().entrySet())
			{
				ThreadPoolExecutor pool = (ThreadPoolExecutor) exn.getKey();
				sb.append("Consume thread (queue id = " + en.getKey() + ") :");
				sb.append("\n -- Current pool size = ").append(pool.getPoolSize());
				sb.append("\n -- Max pool size     = ").append(pool.getMaximumPoolSize());
				sb.append("\n -- Core pool size    = ").append(pool.getCorePoolSize());
				sb.append("\n -- Active count      = ").append(pool.getActiveCount());
				sb.append("\n -- Task queue size   = ").append(pool.getQueue().size());
				// sb.append("\n -- IsTerminating     = ").append(pool.isTerminating());
				// sb.append("\n -- IsTerminated      = ").append(pool.isTerminated());
				// sb.append("\n -- IsShutdown        = ").append(pool.isShutdown());
				sb.append("\n");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		log.info(sb.toString());
	}
}
