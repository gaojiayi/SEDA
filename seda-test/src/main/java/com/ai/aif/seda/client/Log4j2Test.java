package com.ai.aif.seda.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.server.UID;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.ai.aif.seda.utils.MessageUtils;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class Log4j2Test
{

	public static void main(String[] args)
	{

		for (;;)
		{
			System.out.println(System.currentTimeMillis());
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private static void log4j2()
	{
		File file = new File(Log4j2Test.class.getResource(".").getPath(), "log4j2.xml");

		BufferedInputStream in = null;
		try
		{
			in = new BufferedInputStream(new FileInputStream(file));
			ConfigurationSource source = new ConfigurationSource(in);
			Configurator.initialize(null, source);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Logger log = LogManager.getLogger(Log4j2Test.class);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++)
			log.info(MessageUtils.uniqueCode());
		long end = System.currentTimeMillis();

		System.out.println(end - start);
	}

}
