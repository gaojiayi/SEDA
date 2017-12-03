package com.ai.aif.seda.config;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public abstract class Node
{
	/**
	 * 获取需要填充的对象
	 * @return
	 */
	protected abstract Object getCopyObject();
	
	public void setNodeValue(PropertyNode node)
	{
		try
		{
			if (null == getCopyObject())
			{
				BeanUtils.copyProperty(this, node.getName(), node.getValue());
			}
			else
			{
				BeanUtils.copyProperty(getCopyObject(), node.getName(), node.getValue());
			}
		}
		catch (IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch (InvocationTargetException e1)
		{
			e1.printStackTrace();
		}
	}

}
