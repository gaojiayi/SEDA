package com.ai.aif.seda.common;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public enum Priority
{
	H,

	M,

	L;

	public static Priority toPriority(String type)
	{
		if (H.toString().equalsIgnoreCase(type))
		{
			return H;
		}
		else if (M.toString().equalsIgnoreCase(type))
		{
			return M;
		}
		else if (L.toString().equalsIgnoreCase(type))
		{
			return L;
		}
		else
		{
			return null;
		}
	}
}
