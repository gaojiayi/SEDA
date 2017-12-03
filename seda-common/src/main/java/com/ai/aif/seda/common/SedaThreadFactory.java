package com.ai.aif.seda.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Title:方便设置线程名
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年5月9日
 * @Version: 1.0
 */
public class SedaThreadFactory implements ThreadFactory
{
	private final AtomicLong threadIndex = new AtomicLong(0);

	private final String threadNamePrefix;

	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	public Thread newThread(Runnable r)
	{
		return new Thread(r, threadNamePrefix + this.threadIndex.incrementAndGet());
	}


	public SedaThreadFactory(final String threadNamePrefix)
	{
		this.threadNamePrefix = threadNamePrefix;
	}

}
