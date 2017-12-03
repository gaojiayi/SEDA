package com.ai.aif.seda.service;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.Map;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.message.MessageResponse;
import com.ai.aif.seda.processor.ISedaProcessor;
import com.ai.aif.seda.utils.LogUtils;
import com.ai.appframe2.common.ServiceManager;
import com.ai.comframe.vm.ex.common.data.GlobalContext;
import com.asiainfo.openplatform.srvctl.SrvCtlFactory;

/**
 * @Title: 调用处理能力接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月9日
 * @Version: 1.0
 */
public class CallServiceProcessor implements ISedaProcessor
{
	// private static final Logger log = LogManager.getLogger(CallServiceProcessor.class);
	private static final ILog<?> log = LogUtils.getILog(CallServiceProcessor.class);

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.processor.ISedaProcessor#processRequest(io.netty.channel.ChannelHandlerContext,
	 * com.ai.aif.seda.command.SedaCommand)
	 */
	public SedaCommand processRequest(ChannelHandlerContext ctx, SedaCommand request) throws Exception
	{
		MessageResponse response = new MessageResponse(request.getOpaque());

		SedaCommand resp = SedaCommand.createResponseCommand(request.getEventType(), response);
		// 这里不考虑为空的情况，上层已经过虑；为空不会此外
		MessageRequest req = (MessageRequest) request.readCustomHander();

		// 异步操作，步必须要设置
		resp.setOpaque(request.getOpaque());
		// resp.setOpaque(req.getUniqueCode());
		// response.setUniqueCode(req.getUniqueCode());

		GlobalContext context = (GlobalContext) req.getData();
		String serviceCode = context.getBPMContext().getTaskContext().getServiceCode();

		log.debug("ServiceCode:" + serviceCode + ",User information before calling service:" + req.getUserInfo());

		// 设置log4x上下文
		setLog4xConext(req);

		ServiceManager.setServiceUserInfo(req.getUserInfo());

		Map<String, Object> result = SrvCtlFactory.getInvokeEngine().invokeService(serviceCode, context.getSysParams(),
				context.getBusiParams(), context.getServiceContext());

		response.setUserInfo(ServiceManager.getUser());
		response.setRealResult(result);
		response.setExt((Serializable) context.getServiceContext());
		response.setReturnCode(SedaConstants.SUCCESS);
		response.setReturnMsg("success");

		log.debug("ServiceCode:" + serviceCode + ",User information after calling service:" + req.getUserInfo());

		return resp;
	}

	private void setLog4xConext(MessageRequest req)
	{
		if (null == req.getLog4xConext() || "".equals(req.getLog4xConext()))
		{
			log.error("log4x conext is empty!");
			return;
		}
		log.info("log4x trace conext : {}", req.getLog4xConext());
		//引入log4x
		//Log4xClient.getInstance()._saveTraceContext(req.getLog4xConext());
	}
}
