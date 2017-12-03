package com.ai.aif.seda.handler;

import io.netty.channel.ChannelHandlerContext;

import com.ai.aif.seda.app.client.SedaClient;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.service.AbstractSedaService;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月22日
 * @Version: 1.0
 */
public class SedaClientHandler extends AbstractSedaHandler
{
	private SedaClient client;

	public SedaClientHandler(SedaClient client)
	{
		this.client = client;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SedaCommand msg) throws Exception
	{
		messageReceived(ctx, msg);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.handler.AbstractSedaHandler#getSedaSevice()
	 */
	@Override
	protected AbstractSedaService getSedaSevice()
	{
		return client;
	}
}
