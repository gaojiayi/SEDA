package com.ai.aif.seda.app.report;

import java.util.HashMap;
import java.util.Map;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.ReportEvent;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.utils.MessageUtils;
import com.alibaba.fastjson.JSON;

public class ReportMessageFlow
{
	public static void reportMsgFlow(final String serviceCode, SedaCommand request, MessageRequest req)
	{

		final Map<String, Object> msgFlow = new HashMap<String, Object>();
		msgFlow.put("Type", "Reqeust");
		msgFlow.put("ServerCode", serviceCode);
		msgFlow.put("UniqueCode", req.getUniqueCode());
		msgFlow.put("CreateTime", MessageUtils.formatTimeToString(req.getCreateTime()));
		msgFlow.put("CommandType", request.getCommandType());
		msgFlow.put("EvenType", request.getEventType());
		msgFlow.put("Message", req.getData().toString());

		report("Req", msgFlow);

	}

	private static void report(final String type, final Map<String, Object> msgFlow)
	{
		final ClientConfig cfg = SedaConfigSource.locadClientCfg();

		if (null == cfg || !cfg.isOpen() || !cfg.isReport())
		{
			return;
		}

		Thread run = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
				map.put(type, msgFlow);
				HttpClientUtils.sendPost(cfg.getUrl(), JSON.toJSONString(map), ReportEvent.PUT_MESSAGE_FLOW);
			}
		});
		run.start();
	}

	public static void reportMsgFlow(final String serviceCode, SedaCommand response, Map<String, Object> exMap)
	{
		final ClientConfig cfg = SedaConfigSource.locadClientCfg();

		// cfg 为空表示：在Server端运行，过滤该情况；上报只在client
		if (null == cfg || !cfg.isOpen() || !cfg.isReport())
		{
			return;
		}

		final Map<String, Object> map = new HashMap<String, Object>();
		map.put("Type", "Response");
		map.put("ServerCode", serviceCode);
		map.put("UniqueCode", response.getOpaque());
		map.put("CommandType", response.getEventType());
		map.put("EvenType", response.getEventType());
		map.put("Describe", response.getDescribe() == null ? "" : response.getDescribe());
		map.putAll(exMap);

		report("Resp", map);
	}
}
