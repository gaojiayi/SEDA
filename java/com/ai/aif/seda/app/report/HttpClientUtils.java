package com.ai.aif.seda.app.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.ai.aif.seda.common.ReportEvent;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.utils.LogUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @Title:httpclient
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class HttpClientUtils
{
	private static final ILog<?> log = LogUtils.getILog(HttpClientUtils.class);

	private static final String SUCCESS = "200";

	public static boolean sendPost(String url, String parameters, ReportEvent evn)
	{
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		try
		{
			// method.addRequestHeader("Content-type", "application/json; charset=utf-8");
			// method.addRequestHeader("Accept", "application/json");
			method.addRequestHeader("Seda-report-type", evn.name());
			method.setRequestEntity(new StringRequestEntity(parameters, "application/json", "UTF-8"));

			log.debug("body {}", parameters);

			client.getHttpConnectionManager().getParams().setConnectionTimeout(2000);
			client.getHttpConnectionManager().getParams().setSoTimeout(2000);
			int statusCode = client.executeMethod(method);

			log.debug("resp status {}", statusCode);
			log.debug("response {}", method.getStatusLine());

			String respStr = method.getResponseBodyAsString();

			log.debug("response entity:{}", respStr);

			if (null != respStr && !"".equals(respStr.trim()))
			{
				Map<?, ?> result = (Map<?, ?>) JSON.parseObject(respStr, Map.class);

				return SUCCESS.equalsIgnoreCase(String.valueOf(result.get("optCode")));
			}
		}
		catch (Exception e)
		{
			log.error("", e);
		}
		finally
		{
			method.releaseConnection();
		}
		return false;
	}

	public static void main(String[] args)
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put("serviceCode", "1235");

		System.out.println(JSON.toJSONString(map));

		String str = "{\"Resp\":{\"UniqueCode\":\"AI147419909477101\",\"Result\":\"\",\"EvenType\":\"SUCCESS\",\"CommandType\":\"SUCCESS\",\"ReturnCode\":\"00000000\",\"Type\":\"Response\",\"CreateTime\":\"2016-09-18 19:44:54.912\",\"ServerCode\":\"no service code\",\"RemoteAddress\":\"127.0.0.1:8989\",\"Describe\":\"\",\"ReturnMsg\":\"success\"}}";

		Map<String, Map<String, Object>> temp = (Map<String, Map<String, Object>>) JSON.parse(str);
		for (Map.Entry<String, Map<String, Object>> en : temp.entrySet())
		{
			JSONObject jobj = (JSONObject) en.getValue();
			Iterator<Entry<String, Object>> it = jobj.entrySet().iterator();
			while (it.hasNext())
			{
				Entry<String, Object> entr = it.next();
				System.out.println(entr.getKey());
				System.out.println(entr.getValue());
			}
			// System.out.println(jobj.entrySet());
		}
		System.out.println(((Map<?, ?>) JSON.parse(str)).entrySet());
	}
}
