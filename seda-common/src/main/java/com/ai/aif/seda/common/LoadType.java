package com.ai.aif.seda.common;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public enum LoadType
{
	// random/polling/optimal
	/** 随机 */
	RANDOM,

	/** 轮询 */
	POLLING,

	/** 最优 */
	OPTIMAL;

	/**
	 * 匹配负载类型
	 * @param type
	 * @return LoadType
	 */
	public static LoadType toLoadType(String type)
	{
		if (RANDOM.toString().equalsIgnoreCase(type))
		{
			return RANDOM;
		}
		else if (POLLING.toString().equalsIgnoreCase(type))
		{
			return POLLING;
		}
		else if (OPTIMAL.toString().equalsIgnoreCase(type))
		{
			return OPTIMAL;
		}
		else
		{
			return null;
		}
	}
}
