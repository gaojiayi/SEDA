package com.ai.aif.seda.handler;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.CustomMessageHeader;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.service.AbstractSedaService;
import com.ai.aif.seda.utils.LogUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Title:  作为seda   InboundHandler   接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public abstract class AbstractSedaHandler extends SimpleChannelInboundHandler<SedaCommand>
{
	//private static final Logger log = LogManager.getLogger(AbstractSedaHandler.class);
	private static final ILog<?> log = LogUtils.getILog(AbstractSedaHandler.class);
	/**
	 * 获取seda service
	 * 
	 * @return
	 */
	protected abstract AbstractSedaService getSedaSevice();

	protected void processRequest(ChannelHandlerContext ctx, SedaCommand cmd)
	{
		CustomMessageHeader[] requests = cmd.readCommandHeader(MessageRequest.class);

		if (null == requests)
		{
			String str = "0 requests. workers can not handle, over.";
			log.error(str);
			final SedaCommand response = SedaCommand.createResponseCommand(EventType.SYSTEM_ERROR, str);
			response.setOpaque(cmd.getOpaque());
			ctx.writeAndFlush(response);
			return;
		}

		//此处对批量请求进行遍历
		for (CustomMessageHeader req : requests)
		{
			log.debug("Receive request：{}\n{}", cmd, req);
			MessageRequest newReq = (MessageRequest) req;
			SedaCommand newCmd = SedaCommand.createRequestCommand(cmd.getEventType(), req);
			newCmd.setOpaque(newReq.getUniqueCode());
			getSedaSevice().processRequest(ctx, newCmd);
		}

	}

	protected void messageReceived(ChannelHandlerContext ctx, SedaCommand cmd) throws Exception
	{
		switch (cmd.getCommandType())
		{
			case ASYNC_REQUEST:
			case SYNC_REQUEST:
			case ONEWAY_REQUEST:
				processRequest(ctx, cmd);
				break;
			case RESPONSES:
				getSedaSevice().processResponse(ctx, cmd);
				break;
			default:
				log.warn("Unknown message, not accepted.");
				break;
		}

	}

}
