package com.ai.aif.seda.service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ai.aif.seda.app.interceptor.RpcMessageLog;
import com.ai.aif.seda.common.SedaThreadFactory;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.server.ServerConfig;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.NettyConfig;
import com.ai.aif.seda.utils.LogUtils;

/**
 * @Title: Seda service 启动入口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月5日
 * @Version: 1.0
 */
public class SedaService
{
	// private static final Logger log = LogManager.getLogger(SedaService.class);
	private static final ILog<?> log = LogUtils.getILog(SedaService.class);

	private static ServerConfig cfg = SedaConfigSource.locadServerCfg();;

	private ExecutorService callServerExecutor;

	private final BlockingQueue<Runnable> callServerPoolQueue;

	public SedaService()
	{
		callServerPoolQueue = new LinkedBlockingQueue<Runnable>(cfg.getCallServerThreadPoolQueueCapacity());

		this.callServerExecutor = new ThreadPoolExecutor(//
				cfg.getCallServerThreadPoolNums(), // core  100
				cfg.getCallServerThreadPoolNums(), // max
				1000 * 60, //
				TimeUnit.MILLISECONDS, //
				callServerPoolQueue, //
				new SedaThreadFactory("ExecutiveServiceThread_"));

		startMonitor();
	}

	private void startMonitor()
	{
		TimerTask printTask = new TimerTask()
		{
			@Override
			public void run()
			{
				outSource();
			}
		};
		Timer timer = new Timer("Monitor_Timer");
		// 打印资源
		timer.scheduleAtFixedRate(printTask, 1000 * 4, 60000);
	}

	private void outSource()
	{
		ThreadPoolExecutor pool = (ThreadPoolExecutor) callServerExecutor;
		StringBuilder sb = new StringBuilder();
		sb.append("Executive worker thread pool：");
		sb.append("\nCurrent pool size = ").append(pool.getPoolSize());
		sb.append("\nMax pool size     = ").append(pool.getMaximumPoolSize());
		sb.append("\nCore pool size    = ").append(pool.getCorePoolSize());
		sb.append("\nActive count      = ").append(pool.getActiveCount());
		sb.append("\nTask queue size   = ").append(pool.getQueue().size());
		log.info(sb.toString());
	}

	/**
	 *   注册事件处理器
	 */
	private void startup()
	{
		ISedaService service = new SedaRemotingServiceImpl(prepareConfig());
		service.registerProcessor(EventType.CALL_THIRD_PARTY_SERVICE, new CallServiceProcessor(),
				this.callServerExecutor);
		service.registerProcessor(EventType.HEARTBEAT, new HeartbeatProcessor(), this.callServerExecutor);
		service.registerProcessor(EventType.TEST, new TestProcessor(), this.callServerExecutor);
		service.registerDefaultProcessor(new CallServiceProcessor(), this.callServerExecutor);

		service.registerInterceptor(new RpcMessageLog(cfg));
		service.startup();
	}

	/**
	 * 准备配置
	 * 
	 * @return
	 */
	private NettyConfig prepareConfig()
	{

		NettyConfig config = new NettyConfig();
		config.setListenPort(cfg.getPort());
		config.setsWorkerThreads(cfg.getWorkerThreads());
		config.setsCallbackExecutorThreads(cfg.getCallBackThreads());
		config.setsSelectorThreads(cfg.getSelectorThreads());
		config.setsAsyncSemaphoreValue(cfg.getAsyncConcurrentNumber());
		config.setsOnewaySemaphoreValue(cfg.getOnewayConcurrentNumber());
		config.setsSocketRcvBufSize(cfg.getRcvbufSize());
		config.setsSocketSndBufSize(cfg.getSndbufSize());
		return config;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SedaService service = new SedaService();

		service.startup();
	}

}
