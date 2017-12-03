package com.ai.aif.seda.server;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ai.aif.seda.app.interceptor.RpcMessageLog;
import com.ai.aif.seda.common.SedaThreadFactory;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.message.NettyConfig;
import com.ai.aif.seda.server.processor.TestProcessor;
import com.ai.aif.seda.service.HeartbeatProcessor;
import com.ai.aif.seda.service.ISedaService;
import com.ai.aif.seda.service.SedaRemotingServiceImpl;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月11日
 * @Version: 1.0
 */
public class Service
{
	private static final Logger log = LogManager.getLogger(Service.class);

	private LinkedBlockingQueue<Runnable> callServerPoolQueue;

	private ExecutorService callServerExecutor;

	public Service()
	{
		callServerPoolQueue = new LinkedBlockingQueue<Runnable>(10000);

		this.callServerExecutor = new ThreadPoolExecutor(//
				50,// core
				50,// max
				1000 * 60,//
				TimeUnit.MILLISECONDS,//
				callServerPoolQueue,//
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
		timer.scheduleAtFixedRate(printTask, 1000 * 4, 10000);
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
		;
	}

	public static void main(String[] args)
	{
		new Service().startup();
	}

	/**
	 * 
	 */
	private void startup()
	{
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		NettyConfig config = new NettyConfig();
		config.setListenPort(8989);
		config.setsWorkerThreads(8);
		config.setsCallbackExecutorThreads(4);
		config.setsSelectorThreads(4);
		config.setsAsyncSemaphoreValue(18);
		config.setsOnewaySemaphoreValue(18);
		config.setsSocketRcvBufSize(2048);
		config.setsSocketSndBufSize(2048);

		ISedaService service = new SedaRemotingServiceImpl(config);
		service.registerProcessor(EventType.CALL_THIRD_PARTY_SERVICE, new TestProcessor(), this.callServerExecutor);
		service.registerProcessor(EventType.HEARTBEAT, new HeartbeatProcessor(), this.callServerExecutor);
		service.registerDefaultProcessor(new TestProcessor(), this.callServerExecutor);
		service.registerInterceptor(new RpcMessageLog(SedaConfigSource.locadServerCfg()));
		service.startup();

		// try
		// {
		// Thread.sleep(20000);
		// }
		// catch (InterruptedException e)
		// {
		// e.printStackTrace();
		// }

		// service.invokeSync(service., request, timeoutMillis)
	}

}
