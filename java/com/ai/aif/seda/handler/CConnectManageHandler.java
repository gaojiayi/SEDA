package com.ai.aif.seda.handler;

import java.net.SocketAddress;

import com.ai.aif.seda.app.client.SedaClient;
import com.ai.aif.seda.common.Helper;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Title: 客户端连接管理
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月22日
 * @Version: 1.0
 */
public class CConnectManageHandler extends ChannelDuplexHandler
{
	// private static final Logger log = LogManager.getLogger(SedaClientHandler.class);
	private static final ILog<?> log = LogUtils.getILog(CConnectManageHandler.class);
	
	private SedaClient client;

	public CConnectManageHandler(SedaClient client)
	{
		this.client = client;
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
			ChannelPromise promise) throws Exception
	{
		final String local = localAddress == null ? "UNKNOW" : localAddress.toString();
		final String remote = remoteAddress == null ? "UNKNOW" : remoteAddress.toString();
		log.info("netty client pipeline: connect  {} => {}", local, remote);
		super.connect(ctx, remoteAddress, localAddress, promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.info("netty client pipeline: disconnect " + remoteAddress + "");
		client.closeChannel(ctx.channel());
		super.disconnect(ctx, promise);

	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.info("netty client pipeline: close " + remoteAddress + "");
		client.closeChannel(ctx.channel());
		super.close(ctx, promise);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		final String remoteAddress = Helper.parseChannelRemoteAddr(ctx.channel());
		log.warn("netty client pipeline: exceptioncaught " + remoteAddress + "");
		log.warn("netty client pipeline: exceptioncaught exception.", cause);
		client.closeChannel(ctx.channel());
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
				log.warn("netty client pipeline: idle exception [" + remoteAddress + "]");
				client.closeChannel(ctx.channel());
			}
		}

		ctx.fireUserEventTriggered(evt);
	}

}
