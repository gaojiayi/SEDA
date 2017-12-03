package com.ai.aif.seda.command;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.beanutils.BeanUtils;

import com.ai.aif.seda.common.CommandType;
import com.ai.aif.seda.common.Helper;
import com.ai.aif.seda.event.EventType;
import com.ai.aif.seda.exception.RemotingCommandException;
import com.ai.aif.seda.log.ILog;
import com.ai.aif.seda.message.CustomMessageHeader;
import com.ai.aif.seda.message.MessageRequest;
import com.ai.aif.seda.utils.LogUtils;

/**
 * @Title: 用于客户端与服务端交互
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月8日
 * @Version: 1.0
 */
public class SedaCommand implements Serializable
{
	private static final long serialVersionUID = -6364495149395540482L;

	private static final ILog<?> log = LogUtils.getILog(SedaCommand.class);

	// body 标识
	private static final int BODY_FALG = 0xABCDEF;

	// 请求序列
	private static AtomicLong REQ_SEQUENCE = new AtomicLong(0);

	private transient CustomMessageHeader[] messageHeader;

	private EventType eventType;

	private CommandType commandType;

	private String describe;

	// 消息体
	private transient ByteBuffer body;

	// 消息体是否有压缩
	private transient boolean isCompress;

	// 序列号
	private String opaque = String.valueOf(REQ_SEQUENCE.getAndIncrement());

	public static SedaCommand createRequestCommand(EventType eventType, CustomMessageHeader... customHeader)
	{
		SedaCommand cmd = new SedaCommand();
		cmd.setCommandType(CommandType.ASYNC_REQUEST);
		cmd.setEventType(eventType);
		cmd.messageHeader = customHeader;
		return cmd;
	}

	public static SedaCommand createResponseCommand(EventType eventType, String describe,
			CustomMessageHeader... messageHeader)
	{
		SedaCommand cmd = new SedaCommand();
		cmd.setEventType(eventType);
		cmd.setDescribe(describe);
		cmd.messageHeader = messageHeader;
		cmd.setCommandType(CommandType.RESPONSES);

		return cmd;

	}

	public static SedaCommand createResponseCommand(EventType eventType, String describe)
	{
		SedaCommand cmd = new SedaCommand();
		cmd.setEventType(eventType);
		// cmd.messageHeader = messageHeader;
		cmd.setDescribe(describe);
		cmd.setCommandType(CommandType.RESPONSES);

		return cmd;

	}

	public static SedaCommand createResponseCommand(EventType eventType, CustomMessageHeader... messageHeader)
	{
		SedaCommand cmd = new SedaCommand();
		cmd.setEventType(eventType);
		cmd.messageHeader = messageHeader;
		cmd.setCommandType(CommandType.RESPONSES);

		return cmd;

	}

	public static SedaCommand createResponseCommand(EventType eventType,
			Class<? extends CustomMessageHeader> messageHeader)
	{
		CustomMessageHeader header;
		try
		{
			header = messageHeader.newInstance();
		}
		catch (InstantiationException e)
		{
			return null;
		}
		catch (IllegalAccessException e)
		{
			return null;
		}

		return createResponseCommand(eventType, header);

	}

	/**
	 * 将messageHeader属性放入map中；为了避免序列化，反序列化。
	 */
	public ByteBuffer makeMessageHeaderToNet()
	{
		if (this.messageHeader != null && messageHeader.length != 0)
		{
			ByteBuffer buffers = ByteBuffer.allocate(0);
			for (CustomMessageHeader header : messageHeader)
			{
				Field[] fields = header.getClass().getDeclaredFields();

				Map<String, Object> tempFields = new HashMap<String, Object>();

				for (Field field : fields)
				{
					if (!Modifier.isStatic(field.getModifiers()))
					{
						String name = field.getName();
						if (!name.startsWith("this"))
						{
							Object value = null;
							try
							{
								field.setAccessible(true);
								value = field.get(header);
							}
							catch (IllegalArgumentException e)
							{
							}
							catch (IllegalAccessException e)
							{
							}

							if (value != null)
							{
								tempFields.put(name, value);
							}
						}
					}
				}
				ByteBuffer buf = recordBody(tempFields);

				if (null != buf)
				{
					ByteBuffer temp = ByteBuffer.allocate(buf.limit() + buffers.limit());
					temp.put(buffers.array());
					temp.put(buf.array());
					buffers = temp;
				}
			}
			return buffers;
		}
		return null;
	}

	// 记录字节
	private ByteBuffer recordBody(Map<String, Object> tempFields)
	{
		if (null == tempFields || tempFields.size() == 0)
		{
			return null;
		}
		try
		{
			byte[] data = Helper.ObjectToByte(tempFields);

			int length = 4;

			length += data.length;

			ByteBuffer buffer = ByteBuffer.allocate(4 + length);
			buffer.putInt(BODY_FALG);// T
			buffer.putInt(data.length);// l
			buffer.put(data); // V
			return buffer;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get SedaCommand
	 * @param baseBytes 基础消息
	 * @param body 内部消息体
	 * @param compress 是否压缩
	 * @return SedaCommand
	 */
	public static SedaCommand decode(byte[] baseBytes, byte[] body, boolean compress)
	{
		SedaCommand seda;
		try
		{
			seda = (SedaCommand) Helper.ByteToObject(baseBytes);
			seda.isCompress = compress;
			if (null != body)
			{
				
				seda.body = ByteBuffer.wrap(body);
			}
			return seda;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private List<Object> decoder(ByteBuffer buffer)
	{
		List<Object> list = new ArrayList<Object>();

		while (buffer.hasRemaining())
		{
			if (buffer.getInt() == BODY_FALG)
			{
				int length = buffer.getInt();

				byte[] temp = new byte[length];

				buffer.get(temp);

				try
				{
					list.add(Helper.ByteToObject(temp));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	/**
	 * 获取Message Header
	 * @param classHeader
	 * @return CustomMessageHeader
	 * @throws RemotingCommandException
	 */
	@SuppressWarnings("unchecked")
	public CustomMessageHeader[] readCommandHeader(Class<? extends CustomMessageHeader> classHeader)
	{
		if (this.body != null && body.capacity() > 0)
		{
			List<Object> extFields = loadExtFields();

			CustomMessageHeader[] custHeaders = new CustomMessageHeader[extFields.size()];
			int index = 0;
			for (Object obj : extFields)
			{
				HashMap<Object, Object> map = (HashMap<Object, Object>) obj;
				CustomMessageHeader objectHeader = newInstance(classHeader);
				custHeaders[index] = objectHeader;

				// 检查返回对象是否有效
				Field[] fields = objectHeader.getClass().getDeclaredFields();

				for (Field field : fields)
				{
					if (!Modifier.isStatic(field.getModifiers()))
					{
						String fieldName = field.getName();
						if (!fieldName.startsWith("this"))
						{
							Object value = map.get(fieldName);
							try
							{
								if (null == value)
								{
									continue;
								}

								field.setAccessible(true);

								BeanUtils.copyProperty(objectHeader, field.getName(), value);
							}
							catch (Throwable e)
							{
								log.error("Bean:{},filedName:{},value:{},{}", objectHeader, field.getName(), value, e);
							}
						}
					}
				}
				index++;
			}
			return custHeaders;
		}
		return null;
	}

	/** 装载属性 */
	private List<Object> loadExtFields()
	{
		List<Object> extFields = new ArrayList<Object>();

		if (isCompress)
		{
			try
			{
				byte[] newBody = Helper.uncompress(this.body.array());
				extFields.addAll(decoder(ByteBuffer.wrap(newBody)));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			extFields.addAll(decoder(this.body));
		}
		return extFields;
	}

	private CustomMessageHeader newInstance(Class<? extends CustomMessageHeader> classHeader)
	{
		CustomMessageHeader objectHeader;
		try
		{
			objectHeader = classHeader.newInstance();
		}
		catch (InstantiationException e)
		{
			log.error("",e);
			return null;
		}
		catch (IllegalAccessException e)
		{
			log.error("",e);
			return null;
		}
		return objectHeader;
	}

	public CustomMessageHeader readCustomHander()
	{
		if (null == messageHeader)
		{
			return null;
		}
		return this.messageHeader[0];
	}

	public CustomMessageHeader[] readCustomHanders()
	{
		return this.messageHeader;
	}

	/**
	 * 默认：sync Request
	 * @return the commandType
	 */
	public CommandType getCommandType()
	{
		if (null == commandType)
		{
			return CommandType.SYNC_REQUEST;
		}
		return commandType;
	}

	/**
	 * @param commandType the commandType to set
	 */
	public void setCommandType(CommandType commandType)
	{
		this.commandType = commandType;
	}

	/**
	 * @return the eventType
	 */
	public EventType getEventType()
	{
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(EventType eventType)
	{
		this.eventType = eventType;
	}

	/**
	 * @return the opaque
	 */
	public String getOpaque()
	{
		return opaque;
	}

	/**
	 * @return the describe
	 */
	public String getDescribe()
	{
		return describe;
	}

	/**
	 * @param describe the describe to set
	 */
	public void setDescribe(String describe)
	{
		this.describe = describe;
	}

	/**
	 * @param opaque the opaque to set
	 */
	public void setOpaque(String opaque)
	{
		this.opaque = opaque;
	}

	/**
	 * @param messageHeader the messageHeader to set
	 */
	public void setMessageHeader(CustomMessageHeader... messageHeader)
	{
		this.messageHeader = messageHeader;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SedaCommand:{\n eventType=");
		builder.append(eventType);
		builder.append("\n commandType=");
		builder.append(commandType);
		builder.append("\n opaque=");
		builder.append(opaque);
		builder.append("\n describe=");
		builder.append(describe);
		builder.append("\n}");
		return builder.toString();
	}
}
