package com.ai.aif.seda.console.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Servlet implementation class Command
 */
public class Command extends HttpServlet
{
	private static final Logger log = LogManager.getLogger(Command.class);

	private static final long serialVersionUID = 1L;

	private static final String OPT_TYPE = "Seda-report-type";

	private static final Map<String, List<Map<String, Object>>> BASEINFOMAP = new HashMap<String, List<Map<String, Object>>>();

	private static final Map<String, Map<String, Object>> QUEUE_MAP = new HashMap<String, Map<String, Object>>();

	private static final Map<String, Map<String, Map<String, Object>>> THREAD_MAP = new HashMap<String, Map<String, Map<String, Object>>>();
	// 用于并发历史数据，打算用图表展示
	private static final Map<String, LRUMap> CONCURRENCY = new HashMap<String, LRUMap>();
	// private static final LRUMap CONCURRENCY = new LRUMap(200);

	// Map<String, List<Map<String, Object>>>
	private static final LRUMap MESSAGE_FLOW = new LRUMap(200);

	static
	{
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				monitor();
			}
		}, 2000, 1000);
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Command()
	{
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ReportEvent evn = ReportEvent.toReportEvent(request.getParameter("queryType"));
		String dataType = request.getParameter("dataType");
		switch (evn)
		{
			case QUERY_QUEUE:
				if ("clear".equalsIgnoreCase(dataType))
				{
					QUEUE_MAP.clear();
				}
				sendMsg(request, response, QUEUE_MAP);
				break;
			case QUERY_SERVER:
				if ("clear".equalsIgnoreCase(dataType))
				{
					BASEINFOMAP.clear();
				}
				sendMsg(request, response, BASEINFOMAP);
				break;
			case QUERY_THREAD:
				if ("clear".equalsIgnoreCase(dataType))
				{
					THREAD_MAP.clear();
				}
				sendMsg(request, response, THREAD_MAP);
				break;
			case QUERY_MESSAGE_FLOW:
				if ("clear".equalsIgnoreCase(dataType))
				{
					MESSAGE_FLOW.clear();
				}
				if (null != dataType && !"".equals(dataType))
				{
					sendMsg(request, response, MESSAGE_FLOW.get(dataType));
				}
				else
				{
					sendMsg(request, response, MESSAGE_FLOW);
				}
				break;
			default:
				log.error(evn + " type not identify.");
				break;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException
	{
		String type = request.getHeader(OPT_TYPE);

		ReportEvent evn = ReportEvent.toReportEvent(type);

		log.debug("received report type : " + type);

		Object obj = getObjectByStream(request);

		log.debug("received stream : " + obj);

		int returnCode = 200;
		if (null == obj)
		{
			returnCode = 300;
			return;
		}

		switch (evn)
		{
			case REGISTER:
				Map<String, List<Map<String, Object>>> temp = (Map<String, List<Map<String, Object>>>) obj;
				for (Map.Entry<String, List<Map<String, Object>>> en : temp.entrySet())
				{
					for (Map<String, Object> ten : en.getValue())
					{
						ten.put("receiveTime", System.currentTimeMillis());
						ten.put("isNormal", "Normal");
					}
				}
				BASEINFOMAP.putAll(temp);
				break;
			case PUT_QUEUE:
				QUEUE_MAP.putAll((Map<String, Map<String, Object>>) obj);
				break;
			case PUT_THREADGROUP:
				THREAD_MAP.putAll((Map<String, Map<String, Map<String, Object>>>) obj);
				break;
			case PUT_MESSAGE_FLOW:
				try
				{
					setMessageFlow(obj);
				}
				catch (Exception e)
				{
					returnCode = 300;
					log.error("", e);
				}
				break;
			default:
				break;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("optCode", String.valueOf(returnCode));
		sendMsg(request, response, JSON.toJSON(map));
	}

	/** 设置消息流水 */
	@SuppressWarnings("unchecked")
	private void setMessageFlow(Object obj)
	{
		Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) obj;
		for (Map.Entry<String, Map<String, Object>> en : map.entrySet())
		{
			Map<String, Object> newMap = new HashMap<String, Object>();
			JSONObject newjson = (JSONObject) en.getValue();
			Iterator<Entry<String, Object>> it = newjson.entrySet().iterator();
			while (it.hasNext())
			{
				Entry<String, Object> entry = it.next();
				newMap.put(entry.getKey(), entry.getValue());
			}

			if (!MESSAGE_FLOW.containsKey(en.getKey()))
			{
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				list.add(newMap);
				Map<String, List<Map<String, Object>>> temp = new HashMap<String, List<Map<String, Object>>>();
				temp.put(en.getKey(), list);
				MESSAGE_FLOW.putAll(temp);
			}
			else
			{
				List<Map<String, Object>> tempList = (List<Map<String, Object>>) MESSAGE_FLOW.get(en.getKey());
				tempList.add(newMap);
				addDataByLRU(tempList);
				Collections.reverse(tempList);
			}
		}
	}

	private void addDataByLRU(List<Map<String, Object>> srcList)
	{
		int maxIndex = 200;
		if (srcList.size() > maxIndex)
		{
			List<Map<String, Object>> tempList = new ArrayList<Map<String,Object>>(srcList);
			srcList.removeAll(tempList.subList(0, tempList.size() - maxIndex));
		}
	}

	private void sendMsg(HttpServletRequest request, HttpServletResponse response, Object obj)
	{
		if (null == obj)
		{
			return;
		}

		response.setCharacterEncoding("UTF-8");
		response.addHeader("Content-type", "application/json; charset=utf-8");
		response.setContentType("application/json; charset=utf-8");
		// response.setHeader("Accept", "application/json");
		PrintWriter out = null;
		try
		{
			String json = JSON.toJSONString(obj);
			// log.debug("response to json {}", json);
			out = response.getWriter();
			out.append(json);
		}
		catch (IOException e)
		{
			log.error("", e);
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	private Object getObjectByStream(HttpServletRequest request)
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			InputStream is = request.getInputStream();
			byte[] by = new byte[1024];
			int i = 0;
			while ((i = is.read(by)) != -1)
			{
				bos.write(by, 0, i);
			}

			return JSON.parse(bos.toByteArray());
		}
		catch (IOException e)
		{
			log.error("", e);
		}
		return null;
	}

	private static void monitor()
	{
		for (Map.Entry<String, List<Map<String, Object>>> en : BASEINFOMAP.entrySet())
		{
			for (Map<String, Object> map : en.getValue())
			{
				long time = (Long) map.get("receiveTime");

				// 120000 毫秒
				if ((time + 2 * 60 * 1000) <= System.currentTimeMillis())
				{
					map.put("isNormal", "Abnormal");
				}
			}

		}

	}

	public static void main(String[] args)
	{
		System.out.println(JSON.toJSONString(MESSAGE_FLOW));
	}

}
