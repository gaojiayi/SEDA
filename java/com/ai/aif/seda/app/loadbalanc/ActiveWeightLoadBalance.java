package com.ai.aif.seda.app.loadbalanc;

import java.util.List;
import java.util.Random;

import com.ai.aif.seda.config.Referer;

/**
 * 低并发优化" 负载均衡
 * 
 * <pre>
 *    1） 低并发度优先： referer的某时刻的call数越小优先级越高 
 *    2） 低并发referer获取策略：
 * 		由于Reerer List可能很多，比如上百台，如果每次都要从这上百个Referer或者最低并发的几个，性能有些损耗，
 * 		因此 rndom.nextInt(list.size()) 获取一个起始的index，然后获取最多不超过MAX_REFERER_COUNT的
 * 		状态是sAvailable的referer进行判断activeCount.
 * </pre>
 * 
 * *
 * @Title:低并发优化
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class ActiveWeightLoadBalance<T> extends AbstractLoadBalance<T>
{
	public static final int MAX_REFERER_COUNT = 10;

	private static Random random = new Random();

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.app.loadbalanc.AbstractLoadBalance#doSelect(java.util.List)
	 */
	@Override
	protected T doSelect(List<Referer<T>> referers)
	{
		int refererSize = referers.size();
		int startIndex = random.nextInt(refererSize);
		int currentCursor = 0;
		int currentAvailableCursor = 0;

		Referer<T> referer = null;

		//如果还没有全部遍历一遍，并且收集到的可用服务不超过MAX_REFERER_COUNT，
		//那么取这currentAvailableCursor个可用的服务中，call最小的服务
		while (currentAvailableCursor < MAX_REFERER_COUNT && currentCursor < refererSize)
		{
			Referer<T> temp = referers.get((startIndex + currentCursor) % refererSize);
			//依次获取currentCursor次
			currentCursor++;
			if (!temp.isAvailable())
			{
				continue;
			}
			//有可用的服务  才会currentAvailableCursor自增
			currentAvailableCursor++;
			if (referer == null)
			{
				referer = temp;
			}
			else
			{
				if (compare(referer, temp) > 0)
				{
					referer = temp;
				}
			}
		}
		
		//遍历结束
		if (null != referer)
		{
			// 这里不考虑空的情况
			return referer.getReferer();
		}
		return null;
	}

	private int compare(Referer<T> referer1, Referer<T> referer2)
	{
		return referer1.activeRefererCount() - referer2.activeRefererCount();
	}
}
