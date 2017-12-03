package com.ai.aif.seda.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ai.aif.seda.app.client.SedaClient;
import com.ai.aif.seda.common.SedaThreadFactory;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.message.NettyConfigByClient;
import com.ai.aif.seda.service.CallServiceProcessor;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月11日
 * @Version: 1.0
 */
public class Client
{

	private LinkedBlockingQueue<Runnable> callClientPoolQueue;

	private static ExecutorService callClientExecutor;

	private static ISedaClient client;

	public Client()
	{
		callClientPoolQueue = new LinkedBlockingQueue<Runnable>(10000);

		callClientExecutor = new ThreadPoolExecutor(//
				32,// core
				32,// max
				1000 * 60,//
				TimeUnit.MILLISECONDS,//
				callClientPoolQueue,//
				new SedaThreadFactory("ExecutiveClientThread_"));

	}

	public static void main(String[] args)
	{
	}

	public static void startup()
	{
		NettyConfigByClient config = new NettyConfigByClient();
		//config.setClientAsyncSemaphoreValue(5);

		client = new SedaClient(config);

		client.registerProcessor(EventType.CALL_THIRD_PARTY_SERVICE, new CallServiceProcessor(), callClientExecutor);
		client.startup();
	}

	public static ISedaClient getClient()
	{

		if (null == client)
		{
			startup();
		}
		return client;
	}

}
