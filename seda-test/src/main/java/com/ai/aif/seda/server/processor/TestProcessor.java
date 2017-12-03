package com.ai.aif.seda.server.processor;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.message.MessageResponse;
import com.ai.aif.seda.processor.ISedaProcessor;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月11日
 * @Version: 1.0
 */
public class TestProcessor implements ISedaProcessor
{
	private static final Logger log = LogManager.getLogger(TestProcessor.class);

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.processor.ISedaProcessor#processRequest(io.netty.channel.ChannelHandlerContext,
	 * com.ai.aif.seda.command.SedaCommand)
	 */
	public SedaCommand processRequest(ChannelHandlerContext ctx, SedaCommand request) throws Exception
	{
		MessageResponse resp = new MessageResponse(request.getOpaque());

		SedaCommand cmd = SedaCommand.createResponseCommand(request.getEventType(), resp);

		log.info("Service 收到的消息：" + request.toString());

		// 模拟处理业务
		Thread.sleep(1000);

		MessageRequest header = (MessageRequest) request.readCustomHander();
		resp.setUniqueCode(header.getUniqueCode());
		resp.setReturnCode("00000000");
		resp.setReturnMsg("success");
		cmd.setDescribe("Service return resp");
		cmd.setEventType(EventType.SUCCESS);
		cmd.setOpaque(header.getUniqueCode());

		log.info("返回响应：{}", cmd);

		return cmd;
	}

}
