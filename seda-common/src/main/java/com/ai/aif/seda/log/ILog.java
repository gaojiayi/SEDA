package com.ai.aif.seda.log;

/**
 * SEDA日志接口，为了兼容log4j2
 * 
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface ILog<T>
{
	public T getLog();

	public abstract void trace(Object paramObject);

	public abstract void trace(String paramObject, Object... arg);

	public abstract void debug(Object paramObject);

	public abstract void debug(String paramObject, Object... arg);

	public abstract void info(Object paramObject);

	public abstract void info(String paramObject, Object... arg);

	public abstract void warn(Object paramObject);

	public abstract void warn(String paramObject, Object... arg);

	public abstract void error(Object paramObject);

	public abstract void error(String paramObject, Object... arg);

	public abstract void fatal(Object paramObject);

	public abstract void fatal(String paramObject, Object... arg);

	public abstract void trace(String paramObject, Throwable paramThrowable, Object... arg);

	public abstract void debug(String paramObject, Throwable paramThrowable, Object... arg);

	public abstract void info(String paramObject, Throwable paramThrowable, Object... arg);

	public abstract void warn(String paramObject, Throwable paramThrowable, Object... arg);

	public abstract void error(String paramObject, Throwable paramThrowable, Object... arg);

	public abstract void fatal(String paramObject, Throwable paramThrowable, Object... arg);

	public abstract void trace(Object paramObject, Throwable paramThrowable);

	public abstract void debug(Object paramObject, Throwable paramThrowable);

	public abstract void info(Object paramObject, Throwable paramThrowable);

	public abstract void warn(Object paramObject, Throwable paramThrowable);

	public abstract void error(Object paramObject, Throwable paramThrowable);

	public abstract void fatal(Object paramObject, Throwable paramThrowable);

}
