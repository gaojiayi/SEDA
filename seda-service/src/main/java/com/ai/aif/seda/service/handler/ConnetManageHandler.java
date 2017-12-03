package com.ai.aif.seda.service.handler;

import com.ai.aif.seda.common.Helper;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Title: 连接管理
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月17日
 * @Version: 1.0
 */
public class ConnetManageHandler extends ChannelDuplexHandler
{
	// private static final Logger log = LogManager.getLogger(ConnetManageHandler.class);
	private static final ILog<?> log = LogUtils.getILog(ConnetManageHandler.class);

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.info("netty server pipeline: channelRegistered " + remoteAddress + "");
		super.channelRegistered(ctx);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.info("netty server pipeline: channelUnregistered, the channel[" + remoteAddress + "]");
		super.channelUnregistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.info("netty server pipeline: channelActive, the channel[" + remoteAddress + "]");
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.info("netty server pipeline: channelInactive, the channel[" + remoteAddress + "]");
		super.channelInactive(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
	{
		if (evt instanceof IdleStateEvent)
		{
			IdleStateEvent evnet = (IdleStateEvent) evt;
			if (evnet.state().equals(IdleState.ALL_IDLE))
			{
				final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
				log.warn("netty server pipeline: IDLE exception [" + remoteAddress + "]");
				Helper.closeChannel(ctx.channel());
			}
		}

		ctx.fireUserEventTriggered(evt);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.warn("netty server pipeline: exceptionCaught " + remoteAddress + "");
		log.warn("netty server pipeline: exceptionCaught exception.", cause);
		Helper.closeChannel(ctx.channel());
	}

}
