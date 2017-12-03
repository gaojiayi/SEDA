package com.ai.aif.seda.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.message.ParameterizedMessage;

import com.ai.aif.seda.log.ILog;

/**
 * @Title: 模糊匹配
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class StringMatchUtils
{
	private static final ILog<?> log = LogUtils.getILog(StringMatchUtils.class);

	// 计算部分匹配表
	private static int[] getNext(String pattern)
	{
		int j = 0, k = -1;
		int[] next = new int[pattern.length()];
		next[0] = -1;
		while (j < pattern.length() - 1)
		{
			if (-1 == k || pattern.charAt(j) == pattern.charAt(k))
			{
				j++;
				k++;
				next[j] = k;
			}
			else
			{
				k = next[k];
			}
		}

		return next;
	}

	/**
	 * KMP 算法
	 * @param target 目标字符
	 * @param pattern 匹配字符
	 * @return
	 */
	public static int kmpMatch(String target, String pattern)
	{
		int i = 0, j = 0, index = 0;
		int[] next = getNext(pattern); // 计算部分匹配表
		while (i < target.length() && j < pattern.length())
		{
			if (-1 == j || target.charAt(i) == pattern.charAt(j))
			{
				i++;
				j++;
			}
			else
			{
				j = next[j]; // 如果出现部分不匹配，获取跳过的位置
			}
		}

		if (j >= pattern.length())
		{
			index = i - pattern.length(); // 匹配成功，返回匹配子串的首字符下标
		}
		else
		{
			index = -1; // 匹配失败
		}

		return index;
	}

	/**
	 * @param target
	 * @param pattern
	 * @return
	 */
	public static boolean search(Map<Object, Object> target, Map<String, String> pattern)
	{
		if (null == target || null == pattern)
		{
			throw new NullPointerException();
		}
		// log.debug("matching begin.");
		List<Boolean> temp = new ArrayList<Boolean>();

		for (Map.Entry<Object, Object> en : target.entrySet())
		{
			for (Map.Entry<String, String> pen : pattern.entrySet())
			{
				if (!en.getKey().toString().equalsIgnoreCase(pen.getKey()))
				{
					// 目前只考虑值为MAP的，值中MAP不考虑
					if (en.getValue() instanceof Map<?, ?>)
					{
						Map<?, ?> tmap = (Map<?, ?>) en.getValue();

						for (Map.Entry<?, ?> ten : tmap.entrySet())
						{
							if (ten.getKey().toString().equalsIgnoreCase(pen.getKey()))
							{
								match(temp, ten, pen);
							}
						}
					}
					continue;
				}
				match(temp, en, pen);
			}
		}

		if (temp.isEmpty())
		{
			return false;
		}

		return !temp.contains(false);
	}

	private static void match(List<Boolean> result, Map.Entry<?, ?> target, Map.Entry<String, String> pen)
	{
		int index = kmpMatch(target.getValue().toString(), pen.getValue());

		log.debug("{}({}) matching {}({}) {}.", pen.getKey(),//
				pen.getValue(),//
				target.getKey(),//
				target.getValue(),//
				index >= 0 ? "successfully" : "failed");

		if (index >= 0)
		{
			result.add(true);
		}
		else
		{
			result.add(false);
		}
	}

	public static String formatStr(String pattern, Object... arrays)
	{
		try
		{
			ParameterizedMessage p = new ParameterizedMessage(pattern, arrays);

			return p.getFormattedMessage();
		}
		catch (final IllegalFormatException ife)
		{
			return pattern;
		}
	}

	public static void main(String[] args)
	{
		String target = "youwei";
		String pattern = "we";
		int index = kmpMatch(target, pattern);

		Map<Object, Object> targets = new HashMap<Object, Object>();

		Map<String, String> tt = new HashMap<String, String>();
		tt.put("B", "abcd");
		tt.put("c", "aa");

		targets.put("A", "you wei hello word");
		targets.put("D", "abc");
		targets.put("C", tt);

		Map<String, String> t = new HashMap<String, String>();

		t.put("A", "he");
		t.put("B", "a");

		System.err.println(search(targets, t));

		System.out.format("[%s] is in the pos = %d of [%s]", pattern, index, target);
	}

}
