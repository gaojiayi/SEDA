package com.ai.aif.seda.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Title:使用布尔原子变量，信号量保证只释放一次
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月20日
 * @Version: 1.0
 */
public class ReleaseSemaphoreOnlyOnce
{
	private final AtomicBoolean released = new AtomicBoolean(false);
	
	private final Semaphore semaphore;

	/**
	 * Constructor
	 * @param semaphore 信号量
	 */
	public ReleaseSemaphoreOnlyOnce(Semaphore semaphore)
	{
		this.semaphore = semaphore;
	}

	/**释放信号量*/
	public void release()
	{
		if (this.semaphore != null)
		{
			if (this.released.compareAndSet(false, true))
			{
				this.semaphore.release();
			}
		}
	}
}
