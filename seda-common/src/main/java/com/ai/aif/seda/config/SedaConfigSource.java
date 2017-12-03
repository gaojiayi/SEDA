package com.ai.aif.seda.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ai.aif.seda.common.ConfigType;
import com.ai.aif.seda.config.client.ClientConfig;
import com.ai.aif.seda.config.server.ServerConfig;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;
import com.ai.aif.seda.utils.SystemUtils;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class SedaConfigSource
{
	// private static final Logger LOG = LogManager.getLogger(SedaConfigSource.class);
	private static final ILog<?> LOG = LogUtils.getILog(SedaConfigSource.class);

	private final static String CLIENT_CONFIG = "seda-client-rules.xml";

	private final static String SERVER_CONFIG = "seda-server-rules.xml";

	private final static String SEDA_CFG_PATH = "seda.cfg.path";

	private static ClientConfig clientCfg;

	private static ServerConfig serverCfg;

	// 初始时间
	private static long initTime;

	private final static String BASE_PATH = "../config/seda";

	private SedaConfigSource()
	{
	}

	/**
	 * 更新时间
	 * @return
	 */
	public static long getInitTime()
	{
		return initTime;
	}

	/**
	 * 根据终端类型来获取配置文件
	 * @param type
	 * @return file;
	 */
	public static File getConfigFile(ConfigType type)
	{
		switch (type)
		{
			case SERVER:
				return getFile("seda-server.xml");
			case CLIENT:
				return getFile("seda-client.xml");
			default:
				return null;
		}
	}

	/**
	 * 重新加载配置
	 * @param type 终端类型
	 * @return AbstractConfig
	 */
	public static AbstractConfig reloadConfig(ConfigType type)
	{
		switch (type)
		{
			case SERVER:
				serverCfg = null;
				return locadServerCfg();
			case CLIENT:
				clientCfg = null;
				return locadClientCfg();
			default:
				return null;
		}
	}

	/**
	 * 拿配置消息
	 * @return ClientConfig
	 */
	public static ClientConfig locadClientCfg()
	{
		if (!SystemUtils.isService())
		{
			if (null != clientCfg)
			{
				return clientCfg;
			}
			initTime = System.currentTimeMillis();

			Object obj = parseXml(getFile("seda-client.xml"), CLIENT_CONFIG);

			LOG.info("Client loading configuration:\n{}", obj);

			if (null != obj)
			{
				clientCfg = (ClientConfig) obj;
			}
		}
		return clientCfg;
	}

	/**
	 * 拿配置消息
	 * @return ServerConfig
	 */
	public static ServerConfig locadServerCfg()
	{
		if (null != serverCfg)
		{
			return serverCfg;
		}

		initTime = System.currentTimeMillis();

		Object obj = parseXml(getFile("seda-server.xml"), SERVER_CONFIG);

		LOG.info("Server loading configuration:\n{}", obj);

		if (null != obj)
		{
			serverCfg = (ServerConfig) obj;
		}

		return serverCfg;
	}

	public static File getFile(String fileName)
	{
		File file = new File(BASE_PATH, fileName);

		if (!file.exists())
		{
			LOG.warn("{} non-existent.", file.getAbsolutePath());

			String basePath = System.getProperty(SEDA_CFG_PATH);

			LOG.warn("Use env seda.cfg.path = {}", basePath);

			file = new File(basePath, fileName);
		}
		return file;
	}

	private static Object parseXml(File file, String rulesPath)
	{
		InputStream inputs = SedaConfigSource.class.getResourceAsStream(rulesPath);

		Digester digs;

		try
		{
			digs = DigesterLoader.createDigester(new InputSource(inputs));

			return digs.parse(file);
		}
		catch (IOException e)
		{
			LOG.error("Failed to load  {} Or {}", rulesPath, file.getAbsolutePath(), e);
		}
		catch (SAXException e)
		{
			LOG.error("Analytical file failed ", e);
		}

		return null;
	}

	public static void main(String[] args)
	{
		System.out.println(locadClientCfg());
		System.out.println(locadServerCfg());
	}
}
