package com.ai.aif.seda.utils;

/**
 * @Title: 系统工具
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class SystemUtils
{
	/**
	 * 判断是Server端还是Client<br>
	 * 出现该方法的原因： 1、Server 端依赖了整个module，重点也包括了Client端，Client侧的相关线程是不应该存在<br>
	 * 2、由于Maven的层次、依赖关系没设计好<br>
	 * 由于上述两点，就产出了该方法；目前暂时不修改，后面版本修改依赖关系
	 * @return
	 */
	public static boolean isService()
	{
		String serverName = System.getProperty("appframe.server.name");
		
		if (("" + serverName).startsWith("oppf_sedaserver_"))
		{
			return true;
		}
		return false;
	}
}
