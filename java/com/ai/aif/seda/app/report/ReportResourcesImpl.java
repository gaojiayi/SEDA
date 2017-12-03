package com.ai.aif.seda.app.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import com.ai.aif.seda.app.client.DispatcherService;
import com.ai.aif.seda.config.Referer;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.config.client.ServerNode;
import com.ai.aif.seda.message.CustomMessageHeader;
import com.ai.aif.seda.report.IReportWarning;
import com.ai.aif.seda.utils.MessageUtils;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class ReportResourcesImpl implements IReportWarning
{
	private ClientConfig cfg = SedaConfigSource.locadClientCfg();

	private DispatcherService dispatcher;

	private String localAddres;

	public ReportResourcesImpl(DispatcherService dispatcher)
	{
		this.dispatcher = dispatcher;
		this.localAddres = MessageUtils.getLocalIp();
	}

	@Override
	public Map<String, Map<String, Object>> getSedaQueues()
	{
		Map<String, Object> sysRes = new HashMap<String, Object>();
		Map<String, Map<String, Object>> resources = new HashMap<String, Map<String, Object>>();
		resources.put(localAddres, sysRes);

		for (Map.Entry<String, LinkedBlockingQueue<CustomMessageHeader>> en : dispatcher.getQueues().entrySet())
		{
			AtomicLong count = dispatcher.getConsumerCount().get(en.getKey());
			Map<String, Object> temp = new HashMap<String, Object>();
			temp.put("size", en.getValue().size());
			temp.put("consumerCount", null != count ? count.get() : 0);
			sysRes.put(en.getKey(), temp);
		}
		return resources;
	}

	@Override
	public Map<String, List<Map<String, Object>>> register()
	{
		List<Referer<ServerNode>> servers = cfg.getServers();

		List<Map<String, Object>> serverList = new ArrayList<Map<String, Object>>();

		Map<String, List<Map<String, Object>>> data = new HashMap<String, List<Map<String, Object>>>();
		data.put(localAddres, serverList);

		if (null != servers)
		{
			for (Referer<ServerNode> ref : servers)
			{
				Map<String, Object> map = new HashMap<String, Object>();
				ServerNode node = ref.getReferer();
				String addr = node.getIp() + ":" + node.getPort();
				map.put("address", addr);
				map.put("concurrentNumber", node.getConcurrentNumber());
				map.put("refererCount", node.getSelectCount());// 选用次数，不区分成功失败
				// map.put("refererCount", node.activeRefererCount());
				map.put("state", node.getState());
				map.put("currConcurrency", (node.getConcurrentNumber() - dispatcher.getAsyncSemaphore(addr).availablePermits()));//当前并发
				serverList.add(map);
			}
		}
		return data;
	}

	@Override
	public Map<String, Map<String, Map<String, Object>>> getSedaThreadGroup()
	{
		Map<String, Map<String, Object>> sysRes = new HashMap<String, Map<String, Object>>();
		Map<String, Map<String, Map<String, Object>>> resources = new HashMap<String, Map<String, Map<String, Object>>>();
		resources.put(localAddres, sysRes);

		for (Map.Entry<String, Map<ExecutorService, Integer>> en : dispatcher.getConsumerThreads().entrySet())
		{
			Map<String, Object> threads = new HashMap<String, Object>();
			sysRes.put(en.getKey(), threads);

			for (Map.Entry<ExecutorService, Integer> exn : en.getValue().entrySet())
			{
				ThreadPoolExecutor pool = (ThreadPoolExecutor) exn.getKey();
				threads.put("CurrentPoolSize", pool.getPoolSize());
				threads.put("MaxPoolSize", pool.getMaximumPoolSize());
				threads.put("CorePoolSize", pool.getCorePoolSize());
				threads.put("ActiveCount", pool.getActiveCount());
				threads.put("TaskQueueSize", pool.getQueue().size());
			}
		}
		return resources;
	}
}
