package com.ai.aif.seda.command;

import java.nio.ByteBuffer;

import com.ai.aif.seda.common.Helper;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月17日
 * @Version: 1.0
 */
public class SedaDecoder extends LengthFieldBasedFrameDecoder
{
	// private static final Logger log = LogManager.getLogger(SedaDecoder.class);
	private static final ILog<?> log = LogUtils.getILog(SedaDecoder.class);
	
	private static final int FRAME_MAX_LENGTH = //
	Integer.parseInt(System.getProperty("com.seda.deocde.frameMaxLength", "8388608"));

	public SedaDecoder()
	{
		super(FRAME_MAX_LENGTH, 0, 4, 0, 4);
	}

	/*
	 * (non-Javadoc)
	 * @see io.netty.handler.codec.LengthFieldBasedFrameDecoder#decode(io.netty.channel.ChannelHandlerContext,
	 * io.netty.buffer.ByteBuf)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception
	{
		ByteBuf frame = null;
		try
		{
			frame = (ByteBuf) super.decode(ctx, in);
			if (null == frame)
			{
				return null;
			}

			ByteBuffer byteBuffer = frame.nioBuffer();

			return decode(byteBuffer, in);
		}
		catch (Exception e)
		{
			log.error("decode exception, " + Helper.parseChannelRemoteAddr(ctx.channel()), e);

			Helper.closeChannel(ctx.channel());
		}
		finally
		{
			if (null != frame)
			{
				frame.release();
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see io.netty.handler.codec.ByteToMessageDecoder#decode(io.netty.channel.ChannelHandlerContext,
	 * io.netty.buffer.ByteBuf, java.util.List)
	 */
	protected Object decode(ByteBuffer byteBuffer, ByteBuf in) throws Exception
	{
		// ------------- 4 --------- 4 --- length -- 4 --- length
		// 消息格式： SEDA TAG|基础消息长度|基础消息|内部消息体长度|内部消息体

		int tage = byteBuffer.getInt();

		if (Helper.SEDA_TAG != tage)
		{
			return null;
		}

		// 是否压缩
		int compress = byteBuffer.getInt();

		boolean isCompress = Helper.isCompress(compress);

		return decoder(byteBuffer, isCompress);
	}

	private SedaCommand decoder(ByteBuffer byteBuffer, boolean isCompress)
	{
		int dataLength = byteBuffer.getInt();

		// 基础消息
		byte[] baseData = new byte[dataLength];

		byteBuffer.get(baseData);

		// 内部消息
		byte[] body = null;
		if (byteBuffer.hasRemaining())
		{
			int bodyLength = byteBuffer.getInt();
			body = new byte[bodyLength];
			byteBuffer.get(body);
		}
		return SedaCommand.decode(baseData, body, isCompress);
	}
}
