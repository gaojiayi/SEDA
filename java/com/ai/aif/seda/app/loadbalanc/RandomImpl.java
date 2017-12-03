package com.ai.aif.seda.app.loadbalanc;

import java.util.List;

import com.ai.aif.seda.config.Referer;

/**
 * @Title: 随机
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月13日
 * @Version: 1.0
 */
public class RandomImpl<T> extends AbstractLoadBalance<T>
{

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.app.loadbalanc.AbstractLoadBalance#doSelect(java.util.List)
	 */
	@Override
	protected T doSelect(List<Referer<T>> referers)
	{
		int idx = (int) (Math.random() * referers.size());

		for (int i = 0; i < referers.size(); i++)
		{
			Referer<T> ref = referers.get((i + idx) % referers.size());

			if (ref.isAvailable())
			{
				return ref.getReferer();
			}
		}
		return null;
	}

}
