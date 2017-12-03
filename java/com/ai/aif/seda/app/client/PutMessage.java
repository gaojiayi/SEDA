package com.ai.aif.seda.app.client;

import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.QueueFullException;
import com.ai.aif.seda.exception.RemotingException;
import com.ai.aif.seda.interceptor.RpcInterceptor;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.message.MessageResponse;
import com.ai.aif.seda.message.ResponseFuture;
import com.ai.aif.seda.utils.LogUtils;

/**
 * @Title:存储消息
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class PutMessage
{
	// private static final Logger log = LogManager.getLogger(PutMessage.class);
	private static final ILog<?> log = LogUtils.getILog(PutMessage.class);

	private DispatcherService adapter = DispatcherService.newInstance();

	/**
	 * 发送消息
	 * @param req 消息
	 * @param timeoutMillis
	 * @return MessageResponse
	 */
	public MessageResponse sendMessage(MessageRequest req, long timeoutMillis)
	{
		MessageResponse messageResp = new MessageResponse(req.getUniqueCode());

		final ResponseFuture resp = new ResponseFuture(req.getUniqueCode(), timeoutMillis, null, null);

		try
		{
			beforeRequest(req);

			checkIsOpen();

			//放进队列     根据条件匹配到默认的队列    执行异步请求
			adapter.putMessage(req);
			adapter.setSrvSendTimeout(timeoutMillis);// 向后传递超时时间
			
			adapter.responseTable.put(req.getUniqueCode(), resp);
			//异步在此处获取等待哦
			
			// 超时时间，这里增加1秒，CLIENT端超时时间 >= Server端的执行时间
			SedaCommand respCommand = resp.awaitResponse(timeoutMillis + 1000);

			if (null != respCommand)
			{
				return (MessageResponse) respCommand.readCustomHander();
			}
			setResp(req, timeoutMillis, messageResp, resp);

			return messageResp;
		}
		catch (NullPointerException e)
		{
			messageResp.setReturnCode(SedaConstants.PARAM_EMPTY_EXCEPTION);
			messageResp.setReturnMsg(e.getLocalizedMessage());
			log.error("", e);
		}
		catch (QueueFullException e)
		{
			messageResp.setReturnCode(e.getReturnCode());
			messageResp.setReturnMsg(e.getLocalizedMessage());
			log.error("", e);
		}
		catch (InterruptedException e)
		{
			messageResp.setReturnCode(SedaConstants.SYSTEM_ERROR);
			messageResp.setReturnMsg(e.getLocalizedMessage());
			log.error("", e);
			
		}
		catch (RemotingException e)
		{
			messageResp.setReturnCode(e.getReturnCode());
			messageResp.setReturnMsg(e.getMessage());
			log.error("", e);
		}
		finally
		{
			adapter.responseTable.remove(req.getUniqueCode());

			afterResponse(req, messageResp, resp);
		}
		return messageResp;
	}

	/***
	 * 是否打开
	 * @throws RemotingException
	 */
	private void checkIsOpen() throws RemotingException
	{

		ClientConfig cfg = SedaConfigSource.locadClientCfg();

		if (!cfg.isOpen())
		{
			throw new RemotingException(SedaConstants.SEDA_NOT_OPEN, "seda not open. please open.");
		}

	}

	private void setResp(MessageRequest req, long timeoutMillis, MessageResponse messageResp, final ResponseFuture resp)
	{
		messageResp.setReturnCode(SedaConstants.TIMEOUT);
		messageResp.setReturnMsg("wait response on the unique code <" + req.getUniqueCode() + "> timeout, "
				+ timeoutMillis + "(ms)");
		log.error(messageResp.getReturnMsg());
	}

	private void beforeRequest(MessageRequest req)
	{
		SedaCommand reqCmd = SedaCommand.createRequestCommand(EventType.CALL_THIRD_PARTY_SERVICE, req);
		//实际上也是调用的拦截器的前置通知
		adapter.getClient().beforeRequest(reqCmd);
	}

	private void afterResponse(MessageRequest req, MessageResponse resp, ResponseFuture respFuture)
	{
		RpcInterceptor rpc = adapter.getClient().getRpcInterceptor();

		if (null != rpc)
		{
			// 只用于记录日志
			SedaCommand reqCmd = SedaCommand.createRequestCommand(EventType.CALL_THIRD_PARTY_SERVICE, req);
			if (null != respFuture.getRespCommand())
			{
				rpc.afterResponse(reqCmd, respFuture);
			}
			else
			{
				SedaCommand respCmd = SedaCommand.createResponseCommand(reqCmd.getEventType(), resp);
				respCmd.setOpaque(req.getUniqueCode());
				respFuture.setRespCommand(respCmd);
				rpc.afterResponse(reqCmd, respFuture);
			}
		}
	}
}
