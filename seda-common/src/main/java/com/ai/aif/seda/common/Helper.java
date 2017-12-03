package com.ai.aif.seda.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * @Title: 辅助类
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月21日
 * @Version: 1.0
 */
public class Helper
{
	// private static final Logger log = LogManager.getLogger(Helper.class);
	private static final ILog<?> log = LogUtils.getILog(Helper.class);

	/** 内部消息标识 */
	public static final int SEDA_TAG = 0xFFFFFFFF;

	/** 消息压缩 */
	public static final int COMPRESS = 1;

	/** 消息未压缩 */
	public static final int NOT_COMPRESS = 0;

	/**
	 * 根据通道，获取通道地址
	 * @param channel 通道
	 * @return 地址
	 */
	public static String parseChannelRemoteAddr(final Channel channel)
	{
		if (null == channel)
		{
			return "";
		}
		final SocketAddress remote = channel.remoteAddress();
		final String addr = (remote != null) ? remote.toString() : "";

		if (addr.length() > 0)
		{
			int index = addr.lastIndexOf("/");
			if (index >= 0)
			{
				return addr.substring(index + 1);
			}

			return addr;
		}

		return "";
	}

	/**
	 * 将Throwable 转成字符
	 * @param e Throwable
	 * @return str
	 */
	public static String exceptionSimpleDesc(final Throwable e)
	{
		StringBuffer sb = new StringBuffer();
		if (e != null)
		{
			sb.append(e.toString());

			StackTraceElement[] stackTrace = e.getStackTrace();
			if (stackTrace != null && stackTrace.length > 0)
			{
				StackTraceElement elment = stackTrace[0];
				sb.append(", ");
				sb.append(elment.toString());
			}
		}

		return sb.toString();
	}

	/**
	 * get SocketAddress
	 * @param addr IP:PORT
	 * @return SocketAddress
	 */
	public static SocketAddress string2SocketAddress(final String addr)
	{
		String[] s = addr.split(":");
		InetSocketAddress isa = new InetSocketAddress(s[0], Integer.valueOf(s[1]));
		return isa;
	}

	/**
	 * 关闭 Channel
	 * @param channel
	 */
	public static void closeChannel(Channel channel)
	{
		final String addrRemote = parseChannelRemoteAddr(channel);

		channel.close().addListener(new ChannelFutureListener()
		{
			public void operationComplete(ChannelFuture future) throws Exception
			{
				log.info("closeChannel: close the connection to remote address[" + addrRemote + "] result: "
						+ future.isSuccess());
			}
		});
	}

	/**
	 * byte to Object
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static Object ByteToObject(byte[] bytes) throws Exception
	{
		Object obj = null;
		ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
		ObjectInputStream oi = null;
		try
		{
			oi = new ObjectInputStream(bi);
			obj = oi.readObject();
		}
		finally
		{
			bi.close();
			oi.close();
		}
		return obj;
	}

	/**
	 * Object to byte
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static byte[] ObjectToByte(Object obj) throws Exception
	{
		byte[] bytes = null;
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = null;
		try
		{
			oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();
		}
		finally
		{
			bo.close();
			oo.close();
		}
		return (bytes);
	}

	/**
	 * 解压
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public static byte[] uncompress(final byte[] src) throws IOException
	{
		byte[] result = src;
		byte[] uncompressData = new byte[src.length];
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(src);
		InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(src.length);

		try
		{
			while (true)
			{
				int len = inflaterInputStream.read(uncompressData, 0, uncompressData.length);
				if (len <= 0)
				{
					break;
				}
				byteArrayOutputStream.write(uncompressData, 0, len);
			}
			byteArrayOutputStream.flush();
			result = byteArrayOutputStream.toByteArray();
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				byteArrayInputStream.close();
			}
			catch (IOException e)
			{
				log.error(e);
			}
			try
			{
				inflaterInputStream.close();
			}
			catch (IOException e)
			{
				log.error(e);
			}
			try
			{
				byteArrayOutputStream.close();
			}
			catch (IOException e)
			{
				log.error(e);
			}
		}

		return result;
	}

	public static byte[] compress(final byte[] src) throws IOException
	{
		byte[] result = src;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(src.length);
		// 最佳压缩
		java.util.zip.Deflater deflater = new java.util.zip.Deflater(Deflater.BEST_COMPRESSION);
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
		try
		{
			deflaterOutputStream.write(src);
			deflaterOutputStream.finish();
			deflaterOutputStream.close();
			result = byteArrayOutputStream.toByteArray();
		}
		catch (IOException e)
		{
			deflater.end();
			throw e;
		}
		finally
		{
			try
			{
				byteArrayOutputStream.close();
			}
			catch (IOException e)
			{
				log.error("", e);
			}

			deflater.end();
		}

		return result;
	}

	/**
	 * 是否压缩
	 * @param compress
	 * @return boolean
	 */
	public static boolean isCompress(int compress)
	{
		if (COMPRESS == compress)
		{
			return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		Map<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < 50; i++)
		{
			map.put("" + i, "a_" + i);
		}

		try
		{
			byte[] old = ObjectToByte(map);
			byte[] zip = compress(old);
			System.out.println("原：" + old.length);
			System.out.println("后：" + zip.length);

			System.out.println("原Deocde：" + ByteToObject(old));
			System.out.println("后Decode：" + ByteToObject(uncompress(zip)));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
