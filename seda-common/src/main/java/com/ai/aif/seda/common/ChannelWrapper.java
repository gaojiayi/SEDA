package com.ai.aif.seda.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @Title: 客户端的连接容器
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月22日
 * @Version: 1.0
 */
public class ChannelWrapper
{
	private final ChannelFuture channelFuture;

	/**
	 * set ChannelFuture
	 * 
	 * @param channelFuture
	 */
	public ChannelWrapper(ChannelFuture channelFuture)
	{
		this.channelFuture = channelFuture;
	}

	/**
	 * 是否建立连接
	 * 
	 * @return
	 */
	public boolean isOK()
	{
		return (this.channelFuture.channel() != null && this.channelFuture.channel().isActive());
	}

	/**
	 * 是否具备写的条件
	 * 
	 * @return
	 */
	public boolean isWriteable()
	{
		return this.channelFuture.channel().isWritable();
	}

	/**
	 * 获取对应的channel
	 * @return
	 */
	public Channel getChannel()
	{
		return this.channelFuture.channel();
	}

	public ChannelFuture getChannelFuture()
	{
		return channelFuture;
	}

}
