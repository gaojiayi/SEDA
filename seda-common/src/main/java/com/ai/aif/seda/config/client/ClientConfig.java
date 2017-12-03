package com.ai.aif.seda.config.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ai.aif.seda.common.LoadType;
import com.ai.aif.seda.common.Priority;
import com.ai.aif.seda.common.SedaConstants;
import com.ai.aif.seda.config.AbstractConfig;
import com.ai.aif.seda.config.PropertyNode;
import com.ai.aif.seda.config.Referer;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public class ClientConfig extends AbstractConfig
{
	// 通道最大空闲时间
	// private int channelMaxIdleTimeSeconds = 120;
	// 是否开启seda
	private String isOpen = "off";

	// 默认消费线程个数
	private int defaultConsumerThreads = 1;

	// 批量数
	private int sendBatchSize = 5;

	// 存储优先级
	private Map<Priority, Float> ratioMap = new HashMap<Priority, Float>();

	// 默认大小
	private int defaultQueueSize = 50000;

	private int defaultPort = 8989;

	// 毫秒
	private long defaultConnectTimeout = 3000;

	// 毫秒
	private long sendTimeout = 3000;

	private LoadType type;

	private String load;

	private String heartbeatClassName;

	/* 心跳异常次数，达到后视为异常* */
	private int abnormalTimes = 2;

	private String isReport = "off";

	private String url;

	// 20秒
	private long interval = 20000L;

	private Set<QueueNode> queues = new HashSet<QueueNode>();

	private List<Referer<ServerNode>> servers = new ArrayList<Referer<ServerNode>>();
	
	
	/*<consumer-thread-ratio>
	<property name="H" value="3" /> <!-- 线程个数：3*defaultConsumerThreads -->
	<property name="M" value="2" />
	<property name="L" value="1" />
</consumer-thread-ratio>*/
	public void setRatio(PropertyNode node)
	{
		if (null == node)
		{
			return;
		}

		Priority pr = Priority.toPriority(node.getName());

		if (null == pr)
		{
			throw new RuntimeException(node.getName() + " invalid. priority = H/M/L");
		}

		float pvalue = 0;

		try
		{
			pvalue = Float.parseFloat(node.getValue());
		}
		catch (NumberFormatException ex)
		{
			throw new RuntimeException(node.getName() + " value as integer . case : " + ex.toString());
		}

		ratioMap.put(pr, pvalue);
	}

	/**
	 * @return the queues
	 */
	public Set<QueueNode> getQueues()
	{
		return queues;
	}

	/**
	 * @return the type
	 */
	public LoadType getType()
	{
		return type;
	}

	public void addQueue(QueueNode queue)
	{
		// id 重复会覆盖
		if (queues.contains(queue))
		{
			queues.remove(queue);
		}

		queues.add(queue);
	}

	/**
	 * @return the isOpen
	 */
	public boolean isOpen()
	{
		return "on".equalsIgnoreCase(isOpen);
	}

	/**
	 * @param isOpen the isOpen to set
	 */
	public void setIsOpen(String isOpen)
	{
		this.isOpen = isOpen;
	}

	/**
	 * @param queues the queues to set
	 */
	public void setQueues(Set<QueueNode> queues)
	{
		this.queues = queues;
	}

	/**
	 * @return the heartbeatClassName
	 */
	public String getHeartbeatClassName()
	{
		return heartbeatClassName;
	}

	/**
	 * @param heartbeatClassName the heartbeatClassName to set
	 */
	public void setHeartbeatClassName(String heartbeatClassName)
	{
		this.heartbeatClassName = heartbeatClassName;
	}

	/**
	 * @return the servers
	 */
	public List<Referer<ServerNode>> getServers()
	{
		return servers;
	}

	public void addServer(ServerNode queue)
	{
		if (SedaConstants.OFFLINE.equalsIgnoreCase(queue.getState()))
		{
			// 离线的不加载
			return;
		}

		
		if (servers.contains(queue))
		{
			servers.remove(queue);
		}
		servers.add(queue);
	}

	/**
	 * @param servers the servers to set
	 */
	public void setServers(List<Referer<ServerNode>> servers)
	{
		this.servers = servers;
	}

	/**
	 * @return the defaultQueueSize
	 */
	public int getDefaultQueueSize()
	{
		return defaultQueueSize;
	}

	/**
	 * @param defaultQueueSize the defaultQueueSize to set
	 */
	public void setDefaultQueueSize(int defaultQueueSize)
	{
		this.defaultQueueSize = defaultQueueSize;
	}

	/**
	 * @return the sendBatchSize
	 */
	public int getSendBatchSize()
	{
		return sendBatchSize;
	}

	/**
	 * @param sendBatchSize the sendBatchSize to set
	 */
	public void setSendBatchSize(int sendBatchSize)
	{
		this.sendBatchSize = sendBatchSize;
	}

	/**
	 * @return the ratioMap
	 */
	public Map<Priority, Float> getRatioMap()
	{
		return ratioMap;
	}

	/**
	 * @param ratioMap the ratioMap to set
	 */
	public void setRatioMap(Map<Priority, Float> ratioMap)
	{
		this.ratioMap = ratioMap;
	}

	/**
	 * @return the defaultPort
	 */
	public int getDefaultPort()
	{
		return defaultPort;
	}

	/**
	 * @param defaultPort the defaultPort to set
	 */
	public void setDefaultPort(int defaultPort)
	{
		this.defaultPort = defaultPort;
	}

	/**
	 * @return the defaultConnectTimeout
	 */
	public long getDefaultConnectTimeout()
	{
		return defaultConnectTimeout;
	}

	/**
	 * @param defaultConnectTimeout the defaultConnectTimeout to set
	 */
	public void setDefaultConnectTimeout(long defaultConnectTimeout)
	{
		this.defaultConnectTimeout = defaultConnectTimeout;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ai.aif.seda.config.Node#getCopyObject()
	 */
	@Override
	protected Object getCopyObject()
	{
		return null;
	}

	/**
	 * @return the defaultConsumerThreads
	 */
	public int getDefaultConsumerThreads()
	{
		return defaultConsumerThreads;
	}

	/**
	 * @param defaultConsumerThreads the defaultConsumerThreads to set
	 */
	public void setDefaultConsumerThreads(int defaultConsumerThreads)
	{
		this.defaultConsumerThreads = defaultConsumerThreads;
	}

	public String getLoad()
	{
		return this.load;
	}

	/**
	 * @param load the load to set
	 */
	public void setLoad(String load)
	{
		this.load = load;
		setType(LoadType.toLoadType(load));
	}

	/**
	 * @param type the type to set
	 */
	public void setType(LoadType type)
	{
		this.type = type;
	}

	/**
	 * @return the sendTimeout
	 */
	public long getSendTimeout()
	{
		return sendTimeout;
	}

	/**
	 * @param sendTimeout the sendTimeout to set
	 */
	public void setSendTimeout(long sendTimeout)
	{
		this.sendTimeout = sendTimeout;
	}

	/**
	 * @return the abnormalTimes
	 */
	public int getAbnormalTimes()
	{
		return abnormalTimes;
	}

	/**
	 * @param abnormalTimes the abnormalTimes to set
	 */
	public void setAbnormalTimes(int abnormalTimes)
	{
		this.abnormalTimes = abnormalTimes;
	}

	/**
	 * @return the isReport
	 */
	public boolean isReport()
	{
		return "on".equalsIgnoreCase(isReport);
	}

	/**
	 * @param isReport the isReport to set
	 */
	public void setIsReport(String isReport)
	{
		this.isReport = isReport;
	}

	/**
	 * @return the url
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url)
	{
		this.url = url;
	}

	/**
	 * @return the interval
	 */
	public long getInterval()
	{
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(long interval)
	{
		this.interval = interval;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString());
		builder.append("\nClientConfig:{");
		builder.append("\n isOpen=");
		builder.append(isOpen);
		builder.append("\n workerThreads=");
		builder.append(getWorkerThreads());
		builder.append("\n defaultConsumerThreads=");
		builder.append(defaultConsumerThreads);
		builder.append("\n sendBatchSize=");
		builder.append(sendBatchSize);
		builder.append("\n ratioMap=");
		builder.append(ratioMap);
		builder.append("\n defaultQueueSize=");
		builder.append(defaultQueueSize);
		builder.append("\n defaultPort=");
		builder.append(defaultPort);
		builder.append("\n defaultConnectTimeout=");
		builder.append(defaultConnectTimeout);
		builder.append("\n loadType=");
		builder.append(type);
		builder.append("\n sendTimeout=");
		builder.append(sendTimeout);
		builder.append("\n heartbeatClassName=");
		builder.append(heartbeatClassName);
		builder.append("\n abnormalTimes=");
		builder.append(abnormalTimes);
		builder.append("\n isReport=");
		builder.append(isReport);
		builder.append("\n url=");
		builder.append(url);
		builder.append("\n interval=");
		builder.append(interval);
		builder.append("\n queues=");
		builder.append(queues);
		builder.append("\n servers=");
		builder.append(servers);
		builder.append("\n}");
		return builder.toString();
	}

}
