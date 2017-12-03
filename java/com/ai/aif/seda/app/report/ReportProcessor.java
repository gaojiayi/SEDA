package com.ai.aif.seda.app.report;

import java.util.List;
import java.util.Map;

import com.ai.aif.seda.app.client.DispatcherService;
import com.ai.aif.seda.common.ReportEvent;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.report.IReportWarning;
import com.ai.aif.seda.utils.LogUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class ReportProcessor
{
	private static final ILog<?> log = LogUtils.getILog(HttpClientUtils.class);

	// private ClientConfig cfg = SedaConfigSource.locadClientCfg();

	// private final AtomicBoolean isRegister = new AtomicBoolean(false);

	private IReportWarning report;

	public ReportProcessor(DispatcherService dispatcher)
	{
		this.report = new ReportResourcesImpl(dispatcher);
	}

	public void report()
	{
		try
		{
			ClientConfig cfg = SedaConfigSource.locadClientCfg();

			if (!cfg.isOpen() || !cfg.isReport())
			{
				return;
			}

			Map<String, List<Map<String, Object>>> data = report.register();

			String vdata = JSONObject.toJSONString(data);

			boolean flag = HttpClientUtils.sendPost(cfg.getUrl(), vdata, ReportEvent.REGISTER);

			if (!flag)
			{
				log.error("to {} report failed.", cfg.getUrl());
				return;
			}

			Map<String, Map<String, Object>> queue = report.getSedaQueues();

			vdata = JSON.toJSONString(queue);

			HttpClientUtils.sendPost(cfg.getUrl(), vdata, ReportEvent.PUT_QUEUE);

			Map<String, Map<String, Map<String, Object>>> thread = report.getSedaThreadGroup();

			vdata = JSON.toJSONString(thread);

			HttpClientUtils.sendPost(cfg.getUrl(), vdata, ReportEvent.PUT_THREADGROUP);
		}
		catch (Throwable e)
		{
			log.error("", e);
		}
	}
}
