package com.ai.aif.seda.service.handler;

import io.netty.channel.ChannelHandlerContext;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.handler.AbstractSedaHandler;
import com.ai.aif.seda.service.AbstractSedaService;
import com.ai.aif.seda.service.SedaRemotingServiceImpl;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月17日
 * @Version: 1.0
 */
public class SedaServerHandler extends AbstractSedaHandler
{
	private SedaRemotingServiceImpl service;

	public SedaServerHandler(SedaRemotingServiceImpl serviceImpl)
	{
		this.service = serviceImpl;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SedaCommand cmd) throws Exception
	{
		messageReceived(ctx, cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.handler.AbstractSedaHandler#getSedaSevice()
	 */
	@Override
	protected AbstractSedaService getSedaSevice()
	{
		return service;
	}

}
