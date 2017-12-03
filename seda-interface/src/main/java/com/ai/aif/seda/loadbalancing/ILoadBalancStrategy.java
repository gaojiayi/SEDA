package com.ai.aif.seda.loadbalancing;

import java.util.List;

import com.ai.aif.seda.config.Referer;

/**
 * 软负载接口
 * 
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface ILoadBalancStrategy<T>
{
	/**
	 * 负载算法接口
	 * 
	 * @param list 对象集合
	 * @return 对象
	 */
	public T algorithm(List<Referer<T>> list);

}
