package com.ai.aif.seda.app.interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ai.aif.seda.app.report.ReportMessageFlow;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.config.AbstractConfig;
import com.ai.aif.seda.interceptor.RpcInterceptor;
import com.ai.aif.seda.message.CustomMessageHeader;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.message.MessageResponse;
import com.ai.aif.seda.message.ResponseFuture;
import com.ai.aif.seda.utils.MessageUtils;
import com.ai.comframe.vm.ex.common.data.GlobalContext;

/**
 * @Title:记录请求日志并上报流水，暂不考虑异步执行
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class RpcMessageLog implements RpcInterceptor
{
	private static final Logger log = LogManager.getLogger(RpcMessageLog.class);

	private static final ConcurrentHashMap<String/* unique code */, String/* service code */> serverCodeMap = new ConcurrentHashMap<String, String>();

	private AbstractConfig cfg;

	public RpcMessageLog(AbstractConfig cfg)
	{
		this.cfg = cfg;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.interceptor.RpcInterceptor#beforeRequest(com.ai.aif.seda.command.SedaCommand)
	 */
	@Override
	public void beforeRequest(SedaCommand request)
	{
		if (!cfg.isRecordRequestLog() || null == request)
		{
			return;
		}

		if (!filter(request))
		{
			return;
		}

		// request|servercode|code|create time|commandType|EventType|request content
		if (null != request.readCustomHanders())
		{
			for (CustomMessageHeader header : request.readCustomHanders())
			{
				MessageRequest req = (MessageRequest) header;
				String serviceCode = getServerCode(req);
				serverCodeMap.putIfAbsent(req.getUniqueCode(), serviceCode);
				log.info("Request |{}|{}|{}|{}|{}|{}", serviceCode, //
						req.getUniqueCode(), //
						MessageUtils.formatTimeToString(req.getCreateTime()), //
						// addr, //
						request.getCommandType(), //
						request.getEventType(), //
						req.getData());

				ReportMessageFlow.reportMsgFlow(serviceCode, request, req);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.ai.aif.seda.interceptor.RpcInterceptor#afterResponse(java.lang.String,com.ai.aif.seda.command.SedaCommand,
	 * com.ai.aif.seda.command.SedaCommand)
	 */
	@Override
	public void afterResponse(String addr, SedaCommand request, SedaCommand response)
	{
		if (!cfg.isRecordRequestLog() || null == request)
		{
			return;
		}

		if (!filter(request))
		{
			return;
		}

		String serviceCode = getServerCode(request);
		// response|server code|code|create time|addr|commandType|EventType|describe|returnCode|returnMsg|result
		if (null != response)
		{
			long time = System.currentTimeMillis();
			Map<?, ?> result = null;
			String returnCode = "";
			String msg = "";
			if (null != response.readCustomHander())
			{
				MessageResponse resp = (MessageResponse) response.readCustomHander();
				time = resp.getCreateTime();
				result = resp.getRealResult();
				returnCode = resp.getReturnCode();
				msg = resp.getReturnMsg();
			}
			String newTime = MessageUtils.formatTimeToString(time);
			log.info("Response|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", serviceCode, response.getOpaque(), //
					newTime, //
					addr == null ? "Unknown address" : addr, //
					response.getCommandType(), //
					response.getEventType(), //
					response.getDescribe() == null ? "" : response.getDescribe(), //
					returnCode,//
					msg == null ? "" : msg,//
					result == null ? "" : result.toString());

			// 上报流水
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("CreateTime", newTime);
			map.put("RemoteAddress", addr == null ? "Unknown address" : addr);
			map.put("ReturnCode", returnCode);
			map.put("ReturnMsg", msg == null ? "" : msg);
			map.put("Result", result == null ? "" : result.toString());
			ReportMessageFlow.reportMsgFlow(serviceCode, response, map);
		}
		else
		{
			// response|server code|code|create time|addr|commandType|EventType|ERROR
			log.info("Response|{}|{}|{}|{}|{}|{}|{}", serviceCode, request.getOpaque(), //
					MessageUtils.formatTimeToString(System.currentTimeMillis()), //
					addr == null ? "Unknown address" : addr, //
					request.getCommandType(), //
					request.getEventType(), //
					"ERROR");

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("RemoteAddress", addr == null ? "Unknown address" : addr);
			ReportMessageFlow.reportMsgFlow(serviceCode, response, map);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.interceptor.RpcInterceptor#afterResponse(com.ai.aif.seda.command.SedaCommand,
	 * com.ai.aif.seda.message.ResponseFuture)
	 */
	@Override
	public void afterResponse(SedaCommand request, ResponseFuture respFuture)
	{
		if (!cfg.isRecordRequestLog() || null == request)
		{
			return;
		}

		if (!filter(request))
		{
			return;
		}

		try
		{
			SedaCommand respCommand = null;
			if (null != respFuture)
			{
				respCommand = respFuture.getRespCommand();
				respCommand.setDescribe(respFuture.getCause() == null ? "" : respFuture.getCause()
						.getLocalizedMessage());
			}
			afterResponse(respFuture.getRemoteAddr(), request, respCommand);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private boolean filter(SedaCommand request)
	{
		switch (request.getEventType())
		{
			case HEARTBEAT:
			case REFRESH_DATA:
				return false;
			default:
				return true;
		}
	}

	private String getServerCode(MessageRequest req)
	{
		String serviceCode = "no service code";
		if (req.getData() instanceof GlobalContext)
		{
			GlobalContext context = (GlobalContext) req.getData();
			serviceCode = context.getBPMContext().getTaskContext().getServiceCode();
		}
		return serviceCode;
	}

	private String getServerCode(SedaCommand request)
	{
		String serviceCode = "no service code";
		CustomMessageHeader msg = request.readCustomHander();
		if (null != msg)
		{
			if (msg instanceof MessageRequest)
			{
				MessageRequest req = (MessageRequest) msg;
				serviceCode = getServerCode(req);
				serverCodeMap.remove(req.getUniqueCode());
			}
			else if (msg instanceof MessageResponse)
			{
				MessageResponse res = (MessageResponse) msg;
				return serverCodeMap.remove(res.getUniqueCode());
			}
		}
		return serviceCode;
	}

}
