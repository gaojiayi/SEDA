package com.ai.aif.seda.app;

import java.util.HashMap;
import java.util.Map;

import com.ai.aif.seda.app.loadbalanc.ActiveWeightLoadBalance;
import com.ai.aif.seda.app.loadbalanc.RandomImpl;
import com.ai.aif.seda.app.loadbalanc.RoundRobinImpl;
import com.ai.aif.seda.common.LoadType;
import com.ai.aif.seda.config.SedaConfigSource;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.config.client.ServerNode;
import com.ai.aif.seda.loadbalancing.ILoadBalancStrategy;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;

/**
 * @Title: 负载工厂
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class LoadBalancFactory
{
	//private static Logger log = LogManager.getLogger(LoadBalancFactory.class);
	private static final ILog<?> log = LogUtils.getILog(LoadBalancFactory.class);
	
	private static ClientConfig cfg = SedaConfigSource.locadClientCfg();

	private static Map<LoadType, ILoadBalancStrategy<ServerNode>> STRATEGY = new HashMap<LoadType, ILoadBalancStrategy<ServerNode>>();

	static
	{
		STRATEGY.put(LoadType.RANDOM, new RandomImpl<ServerNode>());
		STRATEGY.put(LoadType.POLLING, new RoundRobinImpl<ServerNode>());
		STRATEGY.put(LoadType.OPTIMAL, new ActiveWeightLoadBalance<ServerNode>());
	}

	public static ILoadBalancStrategy<ServerNode> newInstance()
	{
		LoadType type = LoadType.toLoadType(cfg.getLoad());

		if (null == type)
		{
			log.warn("{} unknown. go round robin", cfg.getLoad());
			type = LoadType.POLLING;
		}
		return STRATEGY.get(type);

	}

}
