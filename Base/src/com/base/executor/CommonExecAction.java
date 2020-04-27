/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.utils.Log;


/**
 * <pre>
 * 通用执行Action
 * </pre>
 */
public final class CommonExecAction extends Action {
	private Method mth; // 方法
	private Object obj; // 对象
	private Object[] args; // 参数

	public CommonExecAction(ActionQueue queue, Method mth, Object obj, Object... args) {
		super(queue);
		this.mth = mth;
		this.obj = obj;
		this.args = args;
	}

	/**
	 * @see com.hitalk.dnk.execaction.Action#execute()
	 */
	@Override
	public void execute() {
		try {
			if (mth == null) {
				Log.error("CommonExecAction method为null");
				return;
			}
			if (!Modifier.isStatic(mth.getModifiers()) && obj == null) {
				Log.error("CommonExecAction 实例方法 obj为null，mth:" + mth.getName());
				return;
			}
			mth.invoke(obj, args);
		} catch (IllegalArgumentException e) {
			Log.error("", e);
		} catch (IllegalAccessException e) {
			Log.error("", e);
		} catch (InvocationTargetException e) {
			Log.error("", e);
		} catch (Throwable e) {
			Log.error("", e);
		}
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (mth == null) {
			return this.getClass().getSimpleName() + "-mth null";
		}
		return this.mth.getDeclaringClass().getSimpleName() + "." + this.mth.getName() + "()";
	}

	/**
	 * @return
	 * @see com.hitalk.dnk.execaction.Action#getLogName()
	 */
	@Override
	public String getLogName() {
		return this.toString();
	}

	/**
	 * @return
	 * @see com.hitalk.dnk.execaction.Action#getComment()
	 */
	@Override
	public String getComment() {
		return this.getClass().getSimpleName();
	}
}
