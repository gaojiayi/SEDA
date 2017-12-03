package com.ai.aif.seda.app.loadbalanc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ai.aif.seda.config.Referer;
import com.ai.aif.seda.config.client.ServerNode;

/**
 * @Title: 轮询
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月13日
 * @Version: 1.0
 */
public class RoundRobinImpl<T> extends AbstractLoadBalance<T>
{
	// 索引
	private final AtomicInteger currIndex = new AtomicInteger(0);

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.app.loadbalanc.AbstractLoadBalance#doSelect(java.util.List)
	 */
	@Override
	protected T doSelect(List<Referer<T>> referers)
	{
		int index = currIndex.incrementAndGet();

		for (int i = 0; i < referers.size(); i++)
		{
			Referer<T> ref = referers.get((i + index) % referers.size());

			if (ref.isAvailable())
			{
				return ref.getReferer();
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		RoundRobinImpl<ServerNode> robin = new RoundRobinImpl<ServerNode>();

		List<Referer<ServerNode>> list = new ArrayList<Referer<ServerNode>>();
		ServerNode node = new ServerNode();
		node.setIp("A");
		node.setPort(1000);
		node.setState("online");
		list.add(node);
		node = new ServerNode();
		node.setIp("B");
		node.setPort(2000);
		node.setState("online");
		list.add(node);
		node = new ServerNode();
		node.setIp("c");
		node.setPort(3000);
		node.setState("online");
		list.add(node);

		System.out.println(list);
		System.out.println("------");
		for (int i = 0; i < 11; i++)
		{
			ServerNode ref = robin.algorithm(list);
			System.out.println(ref);
		}
		// robin.doSelect(referers);
	}
}
