package com.ai.aif.seda.common;

/**
 * @Title:
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @Version: 1.0
 */
public interface SedaConstants
{
	/*** 成功 */
	public static final String SUCCESS = "00000";

	/*** 超时 */
	public static final String TIMEOUT = "70001";

	/*** 连接失败 */
	public static final String CONNECT_EXCEPTION = "70002";

	/** 请求堆积已经超出上限 */
	public static final String TOO_MUCH_REQUEST_EXCEPTION = "70003";

	/** 发送失败 */
	public static final String SEND_REQUEST_EXCEPTION = "70004";

	/** Seda Server 端不可用 */
	public static final String NOT_AVAILABLE_SERVICES_EXCEPTION = "70005";

	/** 调用失败 */
	public static final String CALL_EXCEPTION = "70006";

	/** 队列已经满 */
	public static final String QUEUE_FULL_EXCEPTION = "70007";

	/** 参数为空 */
	public static final String PARAM_EMPTY_EXCEPTION = "70008";
	
	/** SEDA 未来开启 */
	public static final String SEDA_NOT_OPEN = "70009";

	/** 系统异常 */
	public static final String SYSTEM_ERROR = "71111";

	// /////////////////////////////////////////////////////////

	/*** 在线 */
	public static final String ONLINE = "online";

	/*** 离线 */
	public static final String OFFLINE = "offline";

	// /////////////////////////////////////////////////////////

	/** log4j2 */
	public static final String LOG4J2_TYPE = "LOG4J2";

	/** log4j **/
	public static final String LOG4J_TYPE = "LOG4J";
}
