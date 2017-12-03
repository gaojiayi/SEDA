package com.ai.aif.seda.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;

import com.ai.aif.seda.common.Helper;
import com.ai.aif.seda.config.AbstractConfig;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;
import com.ai.aif.seda.utils.SystemUtils;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月17日
 * @Version: 1.0
 */
public class SedaEncoder extends MessageToByteEncoder<SedaCommand>
{
	// private static final Logger log = LogManager.getLogger(SedaEncoder.class);
	private static final ILog<?> log = LogUtils.getILog(SedaEncoder.class);

	//private ClientConfig cfg = SedaConfigSource.locadClientCfg();

	private AbstractConfig getCfg()
	{
		if (SystemUtils.isService())
		{
			return SedaConfigSource.locadServerCfg();
		}
		return SedaConfigSource.locadClientCfg();
	}

	/*
	 * (non-Javadoc)
	 * @see io.netty.handler.codec.MessageToByteEncoder#encode(io.netty.channel.ChannelHandlerContext, java.lang.Object,
	 * io.netty.buffer.ByteBuf)
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, SedaCommand msg, ByteBuf out) throws Exception
	{
		try
		{
			// ---------------------------------------从基础消息开始会被压缩
			// ------------- 4 ------ 4 ----- 4 --- length -- 4 --- length
			// 消息格式： SEDA TAG|压缩标识|基础消息长度|基础消息（msg）|内部消息体长度|内部消息体
			ByteBuffer buffer = msg.makeMessageHeaderToNet();// 将bean对象换成KEY VALUE

			byte[] datas = Helper.ObjectToByte(msg);

			// 总长
			int length = 12;// 总长度4+TAG4+压缩标识

			length += datas.length;

			byte[] newByte = null;
			int isCompress = Helper.NOT_COMPRESS;
			if (null != buffer)
			{
				length += buffer.limit() + 4;//此处加4   表示内部消息长度占用4字节

				//达到压缩阈值
				if (length >= getCfg().getMesgCompressionLimit())
				{
					log.debug("The message body reaches {} bytes and is compressed.", getCfg().getMesgCompressionLimit());
					isCompress = Helper.COMPRESS;
					newByte = Helper.compress(buffer.array());
					length -= (buffer.limit() - newByte.length);
				}
				else
				{
					newByte = buffer.array();
				}
			}

			ByteBuffer src = ByteBuffer.allocate(4 + length);

			// 总长度,暂时不需要
			src.putInt(length);

			src.putInt(Helper.SEDA_TAG);

			// 是否压缩
			src.putInt(isCompress);

			// data length
			src.putInt(datas.length);

			// data data
			src.put(datas);

			if (null != newByte)
			{
				// 具体消息体
				src.putInt(newByte.length);  //内部消息长度
				src.put(newByte);  //内部消息体
			}

			src.flip();
			out.writeBytes(src);
		}
		catch (Exception e)
		{
			log.error("encode exception,{} ,{}", Helper.parseChannelRemoteAddr(ctx.channel()), e);

			if (null != msg)
			{
				log.error(msg.toString());
			}

			Helper.closeChannel(ctx.channel());
		}
	}

}
