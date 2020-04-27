package com.base.executor;

import java.util.EventObject;

/**
 * <pre>
 * 事件对象
 * </pre>
 * 
 * @author reison
 */
public class ObjectEvent extends EventObject {

	private static final long serialVersionUID = -855454486771839444L; // 序列
	protected Object objData; // 自定义参数
	/** 事件类型 对应listener列表的key */
	protected int eventType; // 事件类型

	/**
	 * @param obj 系统默认参数
	 * @param objData 自定义参数
	 * @param eventType 事件健值
	 */
	public ObjectEvent(Object obj, Object objData, int eventType) {
		super(obj);
		this.objData = objData;
		this.eventType = eventType;
	}

	public void setObject(Object objData) {
		this.objData = objData;
	}

	public Object getObject() {
		return this.objData;
	}

	public int getEventType() {
		return eventType;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ObjectEvent [objData=" + objData + ", eventType=" + eventType + ", source=" + source + "]";
	}

}
