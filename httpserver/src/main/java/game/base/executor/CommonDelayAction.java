/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.base.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import game.utils.Log;


/**
 * <pre>
 * 通用执行延时Action
 * </pre>
 */
public final class CommonDelayAction extends DelayAction {
	private Method mth; // 执行的方法
	private Object obj; // 对象
	private Object[] args; // 参数
	private boolean repeated; // 是否重复

	public CommonDelayAction(long delay, ActionQueue queue, boolean repeated, Method mth, Object obj, Object... args) {
		super(queue, delay);
		this.mth = mth;
		this.obj = obj;
		this.args = args;
		this.repeated = repeated;
	}

	/**
	 * @see com.hitalk.dnk.execaction.Action#execute()
	 */
	@Override
	public void execute() {
		try {
			mth.invoke(obj, args); // 执行
		} catch (IllegalArgumentException e) {
			Log.error("", e);
		} catch (IllegalAccessException e) {
			Log.error("", e);
		} catch (InvocationTargetException e) {
			Log.error("", e);
		}
		if (!repeated) {
			return;
		}
		// 获取队列.并且执行当前延迟action
		getActionQueue().enDelayQueue(this);
		// if (getActionQueue() != null) {
		// CommonExecutor.enDelayQueue(getActionQueue(), this);
		// } else {
		// CommonExecutor.enDelayQueue(this);
		// }
	}

	/**
	 * @return
	 * @see Object#toString()
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
	 * @see com.hitalk.dnk.execaction.Action#getComment()
	 */
	@Override
	public String getComment() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return
	 * @see com.hitalk.dnk.execaction.Action#getLogName()
	 */
	@Override
	public String getLogName() {
		return this.toString();
	}

	public final void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

}
