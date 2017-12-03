package com.ai.aif.seda.config.client;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.config.Node;
import com.ai.aif.seda.config.Referer;

/**
 * @param <T>
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class ServerNode extends Node implements Referer<ServerNode>
{
	//用于负载
	private AtomicInteger refererCount = new AtomicInteger(0);
	//用于显示，一直累加
	private AtomicLong selectCount = new AtomicLong(0);

	private String serverId;

	private String desc;

	// state=online/offline
	private String state;

	private String ip;

	private int port;

	private String userName;

	private String password;

	// 连接数
	private int concurrentNumber;

	// 连接超时,暂时没用
	private long connectTimeout;
	
	public long getSelectCount()
	{
		return selectCount.get();
	}

	/**
	 * @return the serverId
	 */
	public String getServerId()
	{
		return serverId;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(String serverId)
	{
		this.serverId = serverId;
	}

	/**
	 * @return the desc
	 */
	public String getDesc()
	{
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	/**
	 * @return the state
	 */
	public String getState()
	{
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state)
	{
		this.state = state;
	}

	/**
	 * @return the ip
	 */
	public String getIp()
	{
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip)
	{
		this.ip = ip;
	}

	/**
	 * @return the port
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @return the userName
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the concurrentNumber
	 */
	public int getConcurrentNumber()
	{
		return concurrentNumber;
	}

	/**
	 * @param concurrentNumber the concurrentNumber to set
	 */
	public void setConcurrentNumber(int concurrentNumber)
	{
		this.concurrentNumber = concurrentNumber;
	}

	/**
	 * @return the connectTimeout
	 */
	public long getConnectTimeout()
	{
		return connectTimeout;
	}

	/**
	 * @param connectTimeout the connectTimeout to set
	 */
	public void setConnectTimeout(long connectTimeout)
	{
		this.connectTimeout = connectTimeout;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerNode other = (ServerNode) obj;
		if (serverId == null)
		{
			if (other.serverId != null)
				return false;
		}
		else if (!serverId.equals(other.serverId))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.config.Node#getCopyObject()
	 */
	@Override
	protected Object getCopyObject()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.config.Referer#activeRefererCount()
	 */
	@Override
	public int activeRefererCount()
	{
		return refererCount.get();
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.config.Referer#isAvailable()
	 */
	@Override
	public boolean isAvailable()
	{
		return SedaConstants.ONLINE.equalsIgnoreCase(getState());
	}

	/**
	 * 记录调用数
	 */
	public void increment(int size)
	{
		selectCount.incrementAndGet();//选用次数，累积计数
		refererCount.addAndGet(size);
	}

	/**
	 * 释放记录数
	 */
	public void decrement()
	{
		refererCount.decrementAndGet();
	}

	/**
	 * 释放记录数
	 */
	public void decrement(int size)
	{
		refererCount.addAndGet(0 - size);
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.config.Referer#getReferer()
	 */
	@Override
	public ServerNode getReferer()
	{
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ServerNode:{serverId=");
		builder.append(serverId);
		builder.append(",desc=");
		builder.append(desc);
		builder.append(",state=");
		builder.append(state);
		builder.append(",ip=");
		builder.append(ip);
		builder.append(",port=");
		builder.append(port);
		// builder.append(",userName=");
		// builder.append(userName);
		// builder.append(",password=");
		// builder.append(password);
		builder.append(",concurrentNumber=");
		builder.append(concurrentNumber);
		builder.append(",connectTimeout=");
		builder.append(connectTimeout);
		builder.append(",refererCount=");
		builder.append(activeRefererCount());
		builder.append("}");
		return builder.toString();
	}

}
