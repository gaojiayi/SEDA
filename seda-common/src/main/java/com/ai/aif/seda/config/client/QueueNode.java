package com.ai.aif.seda.config.client;

import java.util.HashMap;
import java.util.Map;

import com.ai.aif.seda.common.Priority;
import com.ai.aif.seda.config.PropertyNode;

/**
 * @Title: Queue 相关属性
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class QueueNode
{
	private String queueId;

	private int size;

	private String priority;

	private Priority pr = Priority.L;

	private Map<String, String> condition = new HashMap<String, String>();

	/**
	 * @return the condition
	 */
	public Map<String, String> getCondition()
	{
		return condition;
	}

	public void setCondition(PropertyNode node)
	{
		condition.put(node.getName(), node.getValue());
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(Map<String, String> condition)
	{
		this.condition = condition;
	}

	/**
	 * @return the queueId
	 */
	public String getQueueId()
	{
		return queueId;
	}

	/**
	 * @param queueId the queueId to set
	 */
	public void setQueueId(String queueId)
	{
		this.queueId = queueId;
	}

	/**
	 * @return the size
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * @return the pr
	 */
	public Priority getPr()
	{
		return pr;
	}

	/**
	 * @param pr the pr to set
	 */
	public void setPr(Priority pr)
	{
		this.pr = pr;
	}

	/**
	 * @return the priority
	 */
	public String getPriority()
	{
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority)
	{
		Priority pr = Priority.toPriority(priority);

		if (null == pr)
		{
			throw new RuntimeException("Priority invalid. priority = H/M/L");
		}

		setPr(pr);

		this.priority = priority;
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
		// result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((queueId == null) ? 0 : queueId.hashCode());
		// result = prime * result + size;
		// result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		QueueNode other = (QueueNode) obj;
		if (queueId == null)
		{
			if (other.queueId != null)
			{
				return false;
			}
		}
		else if (!queueId.equals(other.queueId))
		{
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("QueueNode:{queueId=");
		builder.append(queueId);	
		builder.append(",size=");
		builder.append(size);
		builder.append(",priority=");
		builder.append(priority);
		builder.append(",matchCondition=");
		builder.append(condition);
		// builder.append(", pr=");
		// builder.append(pr);
		builder.append("}");
		return builder.toString();
	}

}
