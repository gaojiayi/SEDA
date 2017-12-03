package com.ai.aif.seda.message;

import java.util.HashMap;

import com.ai.aif.seda.utils.MessageUtils;
import com.ai.appframe2.privilege.UserInfoInterface;

/**
 * @Title: 请求消息头
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月8日
 * @Version: 1.0
 */
public class MessageRequest implements CustomMessageHeader
{
	// 唯一标识
	// private String uniqueCode = UUID.randomUUID().toString();
	private String uniqueCode = MessageUtils.uniqueCode();

	private long createTime = System.currentTimeMillis();

	private HashMap<Object, Object> data;

	private UserInfoInterface userInfo;
	
	//log4x上下文
	private String log4xConext;


	/**
	 * @return the createTime
	 */
	public long getCreateTime()
	{
		return createTime;
	}

	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}

	/**
	 * @return the data
	 */
	public HashMap<Object, Object> getData()
	{
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(HashMap<Object, Object> data)
	{
		this.data = data;
	}

	/**
	 * @return the uniqueCode
	 */
	public String getUniqueCode()
	{
		return uniqueCode;
	}

	/**
	 * @return the userInfo
	 */
	public UserInfoInterface getUserInfo()
	{
		return userInfo;
	}

	/**
	 * @param userInfo the userInfo to set
	 */
	public void setUserInfo(UserInfoInterface userInfo)
	{
		this.userInfo = userInfo;
	}

	/**
	 * @param uniqueCode the uniqueCode to set
	 */
	public void setUniqueCode(String uniqueCode)
	{
		this.uniqueCode = uniqueCode;
	}

	/**
	 * @return the log4xConext
	 */
	public String getLog4xConext()
	{
		return log4xConext;
	}

	/**
	 * @param log4xConext the log4xConext to set
	 */
	public void setLog4xConext(String log4xConext)
	{
		this.log4xConext = log4xConext;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("MessageRequest \n[createTime=");
		builder.append(createTime);
		builder.append("\n data=");
		builder.append(data);
		builder.append("\n uniqueCode=");
		builder.append(uniqueCode);
		builder.append("\n userInfo=");
		builder.append(userInfo);
		builder.append("]");
		return builder.toString();
	}
}
