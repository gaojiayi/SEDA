package com.ai.aif.seda.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

import com.ai.aif.seda.utils.StringMatchUtils;

public class Log4j2 implements ILog<Logger>
{
	private static final String FQCN = Log4j2.class.getName();

	private final ExtendedLoggerWrapper logger;

	private Log4j2(final Logger logger)
	{
		this.logger = new ExtendedLoggerWrapper((AbstractLogger) logger, logger.getName(), logger.getMessageFactory());
	}

	public static Log4j2 getLogger(Class<?> clazz)
	{
		final Logger wrapped = LogManager.getLogger(clazz);
		return new Log4j2(wrapped);
	}

	public static Log4j2 getRootLogger()
	{
		final Logger wrapped = LogManager.getLogger();
		return new Log4j2(wrapped);
	}

	/**
	 * 暂时没有
	 */
	@Override
	public Logger getLog()
	{
		return null;
	}

	@Override
	public void trace(Object paramObject)
	{
		logger.logIfEnabled(FQCN, Level.TRACE, null, paramObject, null);
	}

	@Override
	public void trace(String paramObject, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.TRACE, null, paramObject, arg);
	}

	@Override
	public void debug(Object paramObject)
	{
		logger.logIfEnabled(FQCN, Level.DEBUG, null, paramObject, null);

	}

	@Override
	public void debug(String paramObject, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.DEBUG, null, paramObject, arg);
	}

	@Override
	public void info(Object paramObject)
	{
		logger.logIfEnabled(FQCN, Level.INFO, null, paramObject, null);

	}

	@Override
	public void info(String paramObject, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.INFO, null, paramObject, arg);
	}

	@Override
	public void warn(Object paramObject)
	{
		logger.logIfEnabled(FQCN, Level.WARN, null, paramObject, null);

	}

	@Override
	public void warn(String paramObject, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.WARN, null, paramObject, arg);
	}

	@Override
	public void error(Object paramObject)
	{
		logger.logIfEnabled(FQCN, Level.ERROR, null, paramObject, null);

	}

	@Override
	public void error(String paramObject, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.ERROR, null, paramObject, arg);

	}

	@Override
	public void fatal(Object paramObject)
	{
		logger.logIfEnabled(FQCN, Level.FATAL, null, paramObject, null);
	}

	@Override
	public void fatal(String paramObject, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.FATAL, null, paramObject, arg);

	}

	@Override
	public void trace(String paramObject, Throwable paramThrowable, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.TRACE, null, StringMatchUtils.formatStr(paramObject, arg), paramThrowable);
	}

	@Override
	public void debug(String paramObject, Throwable paramThrowable, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.DEBUG, null, StringMatchUtils.formatStr(paramObject, arg), paramThrowable);
	}

	@Override
	public void info(String paramObject, Throwable paramThrowable, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.INFO, null, StringMatchUtils.formatStr(paramObject, arg), paramThrowable);
	}

	@Override
	public void warn(String paramObject, Throwable paramThrowable, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.WARN, null, StringMatchUtils.formatStr(paramObject, arg), paramThrowable);
	}

	@Override
	public void error(String paramObject, Throwable paramThrowable, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.ERROR, null, StringMatchUtils.formatStr(paramObject, arg), paramThrowable);
	}

	@Override
	public void fatal(String paramObject, Throwable paramThrowable, Object... arg)
	{
		logger.logIfEnabled(FQCN, Level.FATAL, null, StringMatchUtils.formatStr(paramObject, arg), paramThrowable);
	}

	@Override
	public void trace(Object paramObject, Throwable paramThrowable)
	{
		logger.logIfEnabled(FQCN, Level.TRACE, null, paramObject, paramThrowable);
	}

	@Override
	public void debug(Object paramObject, Throwable paramThrowable)
	{
		logger.logIfEnabled(FQCN, Level.DEBUG, null, paramObject, paramThrowable);
	}

	@Override
	public void info(Object paramObject, Throwable paramThrowable)
	{
		logger.logIfEnabled(FQCN, Level.INFO, null, paramObject, paramThrowable);
	}

	@Override
	public void warn(Object paramObject, Throwable paramThrowable)
	{
		logger.logIfEnabled(FQCN, Level.WARN, null, paramObject, paramThrowable);
	}

	@Override
	public void error(Object paramObject, Throwable paramThrowable)
	{
		logger.logIfEnabled(FQCN, Level.ERROR, null, paramObject, paramThrowable);
	}

	@Override
	public void fatal(Object paramObject, Throwable paramThrowable)
	{
		logger.logIfEnabled(FQCN, Level.FATAL, null, paramObject, paramThrowable);
	}

}
