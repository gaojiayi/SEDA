package com.ai.aif.seda.console.action;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.PropertyConfigurator;

/**
 * @Title:加载日志
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class Log4jConfigListener extends HttpServlet
{
	private static final long serialVersionUID = 6065867280434223528L;

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		String path = config.getInitParameter("log4jPath");
		String prefix = config.getServletContext().getRealPath("/");
		PropertyConfigurator.configure(prefix + path);
	}
}
