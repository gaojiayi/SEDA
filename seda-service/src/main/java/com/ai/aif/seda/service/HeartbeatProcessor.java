package com.ai.aif.seda.service;

import io.netty.channel.ChannelHandlerContext;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.processor.ISedaProcessor;

/**
 * @Title: 心跳
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class HeartbeatProcessor implements ISedaProcessor
{

	@Override
	public SedaCommand processRequest(ChannelHandlerContext ctx, SedaCommand request) throws Exception
	{
		SedaCommand resp = SedaCommand.createResponseCommand(request.getEventType(), "Service Normal");
		// 异步操作，步必须要设置
		resp.setOpaque(request.getOpaque());

		return resp;
	}

}
