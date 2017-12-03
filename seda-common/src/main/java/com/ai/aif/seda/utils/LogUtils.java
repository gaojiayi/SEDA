package com.ai.aif.seda.utils;

import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.log.Log4j;
import com.ai.aif.seda.log.Log4j2;

/**
 * 日志工具
 * 
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class LogUtils
{
	private static String logType = System.getProperty("log4j.type", SedaConstants.LOG4J_TYPE);

	private static ILog<?> log;

	/*** 是否为LOG4J2 */
	private static boolean isLog4j2()
	{
		return SedaConstants.LOG4J2_TYPE.equalsIgnoreCase(logType);
	}

	public static ILog<?> getILog(Class<?> clazz)
	{
		if (null != log)
		{
			return log;
		}

		if (isLog4j2())
		{
			log = Log4j2.getLogger(clazz);
		}
		else
		{
			log = Log4j.getLogger(clazz);
		}
		return log;
	}
}
