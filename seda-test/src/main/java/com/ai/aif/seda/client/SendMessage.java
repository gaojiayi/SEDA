package com.ai.aif.seda.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;

import com.ai.aif.seda.app.client.PutMessage;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.message.MessageResponse;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月11日
 * @Version: 1.0
 */
public class SendMessage
{
	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SendMessage.class);

	static Map<String, Object> map = new HashMap<String, Object>();

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// 解决：log4j:WARN No appenders could be found for logger
		// System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.NoOpLog");
		/*
		 * map.put("xxxxxxx", 1111); map.put("yyyy", "xxxx"); map.put("uuu", new HashMap<String, String>() { {
		 * put("xxxxxx1", "11"); } }); try { System.out.println(map); System.out.println(Helper.ObjectToByte(map)); }
		 * catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		// System.out.println(r);

		// thread();
		sendMessage();
//		while (true)
//		{
//			sendMessage();
//			try
//			{
//				Thread.sleep(2000);
//			}
//			catch (InterruptedException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}

		// test();
	}

	private static void test()
	{
		MessageRequest req = new MessageRequest();
		req.setCreateTime(System.currentTimeMillis());

		HashMap<Object, Object> map = new HashMap<Object, Object>();
		map.put("A", "you");
		map.put("B", "ab");
		map.put("C", "hello");

		req.setData(map);

		Map<String, String> targets = new HashMap<String, String>();

		targets.put("A", "you wei hello word");
		targets.put("B", "abc");
		// targets.put("C", "abc");

		// System.out.println(StringMatchUtils.search(targets, req));
	}

	private static void sendMessage()
	{
		PutMessage putMsg = new PutMessage();

		MessageRequest req = new MessageRequest();

		req.setCreateTime(System.currentTimeMillis());

		HashMap<Object, Object> map = new HashMap<Object, Object>();
		map.put("channel_code", "2001");
		map.put("service_type", "BCD");
		map.put("region_code", "A");

		req.setData(map);

		MessageResponse resp = putMsg.sendMessage(req, 4000);

		log.info("结果：{}", resp);
	}

	private static void thread()
	{
		ExecutorService ser = Executors.newCachedThreadPool(new ThreadFactory()
		{
			private AtomicInteger index = new AtomicInteger();

			@Override
			public Thread newThread(Runnable r)
			{
				// TODO Auto-generated method stub
				return new Thread(r, "Test_pool_" + index.incrementAndGet());
			}
		});
		final PutMessage putMsg = new PutMessage();
		for (int i = 0; i < 20; i++)
		{
			Runnable run = new Runnable()
			{

				@Override
				public void run()
				{

					MessageRequest req = new MessageRequest();

					req.setCreateTime(System.currentTimeMillis());

					HashMap<Object, Object> map = new HashMap<Object, Object>();
					map.put("channel_code", "2001");
					map.put("service_type", "BCD");
					map.put("region_code", "A");

					req.setData(map);

					MessageResponse resp = putMsg.sendMessage(req, 4000);

					log.info("结果：{}", resp);

				}
			};
			ser.submit(run);
		}
	}

}
