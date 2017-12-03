package com.ai.aif.seda.processor;

import io.netty.channel.ChannelHandlerContext;

import com.ai.aif.seda.command.SedaCommand;

/**
 * @Title: 处理器接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月8日
 * @Version: 1.0
 */
public interface ISedaProcessor
{
	/**
	 * 事件处理接口
	 * @param ctx 通道处理的上下文
	 * @param request 通信对象
	 * @return SedaCommand
	 * @throws Exception
	 */
	public SedaCommand processRequest(ChannelHandlerContext ctx, SedaCommand request) throws Exception;

	
}
