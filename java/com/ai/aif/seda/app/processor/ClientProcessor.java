package com.ai.aif.seda.app.processor;

import io.netty.channel.ChannelHandlerContext;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.processor.ISedaProcessor;

/**
 * 
 * @Title: 
 * @Description: 
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月13日
 * @Version: 1.0
 */
public class ClientProcessor implements ISedaProcessor
{

	/* (non-Javadoc)
	 * @see com.ai.aif.seda.processor.ISedaProcessor#processRequest(io.netty.channel.ChannelHandlerContext, com.ai.aif.seda.command.SedaCommand)
	 */
	public SedaCommand processRequest(ChannelHandlerContext ctx, SedaCommand request) throws Exception
	{
		//暂时不实现
		return null;
	}

}
