package com.ai.aif.seda.message;

import java.io.Serializable;
import java.util.Map;

import com.ai.appframe2.privilege.UserInfoInterface;

/**
 * @Title: 响应消息头
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月8日
 * @Version: 1.0
 */
public class MessageResponse implements CustomMessageHeader
{
	private long createTime = System.currentTimeMillis();

	private String uniqueCode;

	private String returnCode;

	private String returnMsg;

	private Map<String, Object> realResult;

	private UserInfoInterface userInfo;

	private Serializable ext;

	public MessageResponse()
	{

	}

	public MessageResponse(String uniqueCode)
	{
		this.uniqueCode = uniqueCode;
	}

	/**
	 * @return the createTime
	 */
	public long getCreateTime()
	{
		return createTime;
	}
	

	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}

	/**
	 * @return the returnMsg
	 */
	public String getReturnMsg()
	{
		return returnMsg;
	}

	/**
	 * @param returnMsg
	 *            the returnMsg to set
	 */
	public void setReturnMsg(String returnMsg)
	{
		this.returnMsg = returnMsg;
	}

	/** 返回码 */
	public String getReturnCode()
	{
		return returnCode;
	}

	public void setReturnCode(String returnCode)
	{
		this.returnCode = returnCode;
	}

	/**
	 * @return the realResult
	 */
	public Map<String, Object> getRealResult()
	{
		return realResult;
	}

	/**
	 * @param realResult
	 *            the realResult to set
	 */
	public void setRealResult(Map<String, Object> realResult)
	{
		this.realResult = realResult;
	}

	/**
	 * @return the userInfo
	 */
	public UserInfoInterface getUserInfo()
	{
		return userInfo;
	}

	/**
	 * @param userInfo
	 *            the userInfo to set
	 */
	public void setUserInfo(UserInfoInterface userInfo)
	{
		this.userInfo = userInfo;
	}

	/**
	 * @return the ext
	 */
	public Serializable getExt()
	{
		return ext;
	}

	/**
	 * @param ext
	 *            the ext to set
	 */
	public void setExt(Serializable ext)
	{
		this.ext = ext;
	}

	/**
	 * @return the uniqueCode
	 */
	public String getUniqueCode()
	{
		return uniqueCode;
	}

	/**
	 * @param uniqueCode
	 *            the uniqueCode to set
	 */
	public void setUniqueCode(String uniqueCode)
	{
		this.uniqueCode = uniqueCode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("MessageResponse:{\n uniqueCode=");
		builder.append(uniqueCode);
		builder.append("\n returnCode=");
		builder.append(returnCode);
		builder.append("\n createTime=");
		builder.append(createTime);
		builder.append("\n returnMsg=");
		builder.append(returnMsg);
		builder.append("\n realResult=");
		builder.append(realResult);
		builder.append("\n userInfo=");
		builder.append(userInfo);
		builder.append("\n ext=");
		builder.append(ext);
		builder.append("\n}");
		return builder.toString();
	}

}
