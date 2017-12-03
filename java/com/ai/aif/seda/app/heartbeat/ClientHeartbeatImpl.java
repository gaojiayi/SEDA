package com.ai.aif.seda.app.heartbeat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ai.aif.seda.app.client.SedaClient;
import com.ai.aif.seda.command.SedaCommand;
import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.config.client.ServerNode;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.heartbeat.IHeartbeat;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.utils.LogUtils;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class ClientHeartbeatImpl implements IHeartbeat<ServerNode>
{
	// private static final Logger log = LogManager.getLogger(ClientHeartbeatImpl.class);
	private static final ILog<?> log = LogUtils.getILog(ClientHeartbeatImpl.class);

	private ClientConfig cfg = SedaConfigSource.locadClientCfg();

	private SedaClient client;

	// 心跳发送失败的最大次数
	// private int maxFailureTimes = cfg.getAbnormalTimes();

	public ClientHeartbeatImpl()
	{

	}

	public ClientHeartbeatImpl(SedaClient client)
	{
		this.client = client;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.heartbeat.IHeartbeat#ping(java.util.List, com.ai.aif.seda.command.SedaCommand, long)
	 */
	@Override
	public void ping(List<ServerNode> addrs, long timeoutMillis)
	{
		if (null == addrs || addrs.isEmpty() || !cfg.isOpen())
		{
			return;
		}

		// log.debug("Begin : Send heartbeat Request.");
		for (ServerNode node : addrs)
		{
			ping(node, timeoutMillis);
		}
		// log.debug("End : Send heartbeat Request.");
	}

	/** 向服务端发送心跳消息 */
	private void ping(ServerNode node, long timeout)
	{
		String addr = node.getIp() + ":" + node.getPort();

		try
		{
			MessageRequest req = new MessageRequest();
			SedaCommand request = SedaCommand.createRequestCommand(EventType.HEARTBEAT, req);
			// 这里是重点，由于接收端使用了MessageRequest的uniqueCode，所以需要替换。
			request.setOpaque(req.getUniqueCode());

			SedaCommand resp = client.invokeSync(addr, request, timeout);

			if (resp.getEventType() == request.getEventType())
			{
				// if (SedaConstants.OFFLINE.equalsIgnoreCase(node.getState()))
				if (!node.isAvailable())
				{
					log.info("netty service recovery. channel <{}>", addr);
					reset(addr);
					node.setState(SedaConstants.ONLINE);
				}
				return;
			}

			countFail(addr);

			if (isFail(addr))
			{
				// 心跳异常
				node.setState(SedaConstants.OFFLINE);
				log.error("netty service exception. channel <{}>", addr);
			}
		}
		catch (Throwable e)
		{
			countFail(addr);

			if (isFail(addr))
			{
				node.setState(SedaConstants.OFFLINE);
				log.error("netty service exception. channel <{}> {}", addr, e);
			}
		}
	}

	private void countFail(String addr)
	{
		AtomicInteger count = RUN_STATE.get(addr);
		if (null == count)
		{
			count = new AtomicInteger(1);
			RUN_STATE.put(addr, count);
		}
		else
		{
			count.incrementAndGet();
		}
	}

	private void reset(String addr)
	{
		AtomicInteger count = RUN_STATE.get(addr);
		if (null != count)
		{
			count.set(0);
		}
	}

	private boolean isFail(String addr)
	{
		AtomicInteger count = RUN_STATE.get(addr);

		if (null == count)
		{
			return false;
		}

		return count.get() > cfg.getAbnormalTimes();
	}

}
