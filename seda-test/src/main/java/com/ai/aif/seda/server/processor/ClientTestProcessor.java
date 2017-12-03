package com.ai.aif.seda.server.processor;

import io.netty.channel.ChannelHandlerContext;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.message.MessageResponse;
import com.ai.aif.seda.processor.ISedaProcessor;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月11日
 * @Version: 1.0
 */
public class ClientTestProcessor implements ISedaProcessor
{

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.processor.ISedaProcessor#processRequest(io.netty.channel.ChannelHandlerContext,
	 * com.ai.aif.seda.command.SedaCommand)
	 */
	public SedaCommand processRequest(ChannelHandlerContext ctx, SedaCommand request) throws Exception
	{
		MessageResponse resp = new MessageResponse(request.getOpaque());

		SedaCommand cmd = SedaCommand.createResponseCommand(request.getEventType(), resp);
		cmd.setOpaque(request.getOpaque());

		cmd.setDescribe("Client return resp");
		
		System.out.println("xxxxxxxxxxxxxxx");
		
		return cmd;
	}
}
