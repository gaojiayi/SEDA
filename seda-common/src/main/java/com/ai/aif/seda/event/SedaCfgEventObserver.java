package com.ai.aif.seda.event;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ai.appframe2.common.DataStructInterface;

/**
 * zookeeper数据有变化的观察者
 * 
 * @author Administrator
 * @version [版本号, 2015年6月25日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SedaCfgEventObserver
{
	private static final Log log = LogFactory.getLog(SedaCfgEventObserver.class);

	// 事件处理的方法
	private String invokerMethod;

	// 事件处理的类
	private Class<?> invokerClass;

	public SedaCfgEventObserver(String invokerMethod, Class<?> invokerClass)
	{
		super();
		this.invokerMethod = invokerMethod;
		this.invokerClass = invokerClass;

	}

	public void notify(DataStructInterface newData, DataStructInterface oldData)
	{
		try
		{
			Method m = invokerClass.getMethod(invokerMethod, new Class[] { DataStructInterface.class,
					DataStructInterface.class });
			m.invoke(invokerClass.newInstance(), new Object[] { newData, oldData });
		}
		catch (Exception e)
		{
			log.error("调用执行者失败", e);
		}
	}
}
