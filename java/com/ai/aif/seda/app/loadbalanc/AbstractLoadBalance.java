package com.ai.aif.seda.app.loadbalanc;

import java.util.List;

import com.ai.aif.seda.config.Referer;
import com.ai.aif.seda.loadbalancing.ILoadBalancStrategy;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;

/**
 * @Title:负载抽象接口
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public abstract class AbstractLoadBalance<T> implements ILoadBalancStrategy<T>
{
	// private static final Logger log = LogManager.getLogger(AbstractLoadBalance.class);
	private static final ILog<?> log = LogUtils.getILog(AbstractLoadBalance.class);

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.loadbalancing.ILoadBalancStrategy#algorithm(java.util.List)
	 */
	@Override
	public T algorithm(List<Referer<T>> referers)
	{
		if (null == referers || referers.size() == 0)
		{
			log.error("These services are not available.");
			return null;
		}
		T ref = null;

		if (referers.size() == 1)
		{
			ref = referers.get(0).isAvailable() ? referers.get(0).getReferer() : null;
		}
		else if (referers.size() > 1)
		{
			ref = doSelect(referers);
		}
		else
		{
			// do nothing
		}
		if (null != ref)
		{
			return ref;
		}
		log.error("These services are not available.");
		return null;
	}

	/**
	 * 具体选择算法
	 * 
	 * @param referers
	 * @return Referer
	 */
	protected abstract T doSelect(List<Referer<T>> referers);
}
