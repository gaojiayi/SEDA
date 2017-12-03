package com.ai.aif.seda.common;

/**
 * @Title:配对
 * @Description:
 * @Copyright: Copyright (c) 2016 CMC CRM TA Dept-NJ All Rights Reserved
 * @Company: Asiainfo
 * @Author: yougw
 * @since: 2016年4月16日
 * @Version: 1.0
 */
public class Pair<P1, P2>
{

	private P1 object1;

	private P2 object2;

	/**
	 * 设置配对配对关系
	 * @param object1
	 * @param object2
	 */
	public Pair(P1 object1, P2 object2)
	{
		super();
		this.object1 = object1;
		this.object2 = object2;
	}

	/**
	 * @return the object1
	 */
	public P1 getObject1()
	{
		return object1;
	}

	/**
	 * @param object1 the object1 to set
	 */
	public void setObject1(P1 object1)
	{
		this.object1 = object1;
	}

	/**
	 * @return the object2
	 */
	public P2 getObject2()
	{
		return object2;
	}

	/**
	 * @param object2 the object2 to set
	 */
	public void setObject2(P2 object2)
	{
		this.object2 = object2;
	}
}
