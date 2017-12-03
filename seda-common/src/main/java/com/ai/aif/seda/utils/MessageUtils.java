package com.ai.aif.seda.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Title: 为消息休提供的工具类
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class MessageUtils
{
	private static AtomicLong counter = new AtomicLong(0);

	// SimpleDateFormat 是非线程安全的，用ThreadLocal来解决该问题
	private static ThreadLocal<SimpleDateFormat> dateThreadLocal = new ThreadLocal<SimpleDateFormat>();

	// private static Date date = new Date();

	/**
	 * 生成唯一的标识
	 * @return String
	 */
	public static String uniqueCode()
	{
		if (counter.get() > 99999)
		{
			counter.set(1);
		}
		long time = System.currentTimeMillis();
		long newTime = time * 100 + counter.incrementAndGet();
		return "AI" + String.valueOf(newTime);
		// date.setTime(newTime);
		// 格式为：2016072515281400001
		// return String.format("%1$tY%1$tm%1$td%1$tk%1$tM%1$tS%2$05d", date, counter.get());
	}

	private static SimpleDateFormat getFormat()
	{
		SimpleDateFormat dformat = dateThreadLocal.get();
		if (null == dformat)
		{
			dformat = new SimpleDateFormat();
			dateThreadLocal.set(dformat);
		}
		return dformat;
	}

	public static String formatTimeToString(long time)
	{
		SimpleDateFormat dateFormat = getFormat();
		dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Date data = new Date();
		data.setTime(time);
		return dateFormat.format(data);
	}

	/**
	 * 本地IP
	 * @return
	 */
	public static String getLocalIp()
	{
		try
		{
			// Traversal Network interface to get the first non-loopback and non-private address
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			ArrayList<String> ipv4Result = new ArrayList<String>();
			ArrayList<String> ipv6Result = new ArrayList<String>();
			while (enumeration.hasMoreElements())
			{
				final NetworkInterface networkInterface = enumeration.nextElement();
				final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
				while (en.hasMoreElements())
				{
					final InetAddress address = en.nextElement();
					if (!address.isLoopbackAddress())
					{
						if (address instanceof Inet6Address)
						{
							ipv6Result.add(normalizeHostAddress(address));
						}
						else
						{
							ipv4Result.add(normalizeHostAddress(address));
						}
					}
				}
			}

			// prefer ipv4
			if (!ipv4Result.isEmpty())
			{
				for (String ip : ipv4Result)
				{
					if (ip.startsWith("127.0") || ip.startsWith("192.168"))
					{
						continue;
					}

					return ip;
				}

				return ipv4Result.get(ipv4Result.size() - 1);
			}
			else if (!ipv6Result.isEmpty())
			{
				return ipv6Result.get(0);
			}
			// If failed to find,fall back to localhost
			final InetAddress localHost = InetAddress.getLocalHost();
			return normalizeHostAddress(localHost);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}

		return null;

	}

	private static String normalizeHostAddress(final InetAddress localHost)
	{
		if (localHost instanceof Inet6Address)
		{
			return "[" + localHost.getHostAddress() + "]";
		}
		else
		{
			return localHost.getHostAddress();
		}
	}

	public static void main(String[] args)
	{
		// 2016-07-28 15:10:52.714
		// 2016-07-28 15:10:55.505
		SimpleDateFormat dateFormat = getFormat();
		dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.SSS");
		try
		{
			long start = dateFormat.parse("2016-07-28 15:56:27.856").getTime();

			long end = dateFormat.parse("2016-07-28 15:56:31.040").getTime();

			System.out.println(end - start);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		
		System.out.println(getLocalIp());
	}
}
