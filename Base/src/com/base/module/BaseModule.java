/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

import com.base.executor.AbstractActionQueue;
import com.base.executor.ExecutorMgr;
import com.base.module.ModuleMgr;
import com.base.module.able.IBaseModule;
import com.game.player.GamePlayer;

/**
 * <pre>
 * 模块基类
 * </pre>
 * 
 * @author reison
 */
public abstract class BaseModule extends AbstractActionQueue implements IBaseModule {

	protected String moduleName; // 模块名称
	protected GamePlayer player; // 玩家
	protected int userId; // 玩家id

	public BaseModule(GamePlayer player) {
		this(player.getUserId());
		this.player = player;
	}

	public BaseModule(int userId) {
		super(ExecutorMgr.getPlayerExecutor().getExecutor());
		if (userId == 0) {
			return;
		}
		this.userId = userId;
		moduleName = ModuleMgr.getModuleName(this);
		// 战斗服初始化会一直报错
		// if (moduleName == null) {
		// Log.error("当前面模块缺失AnnModule注解,className:" + this.getClass().getName());
		// }
	}

	public final int getUserId() {
		return userId;
	}

	/**
	 * <pre>
	 * 在所有模块都加载完后执行
	 * ！防止因为某逻辑依赖模块加载顺序而报错
	 * </pre>
	 */
	@Override
	public void afterLoad() {
	}

	/**
	 * <pre>
	 * 刷新排行榜
	 * ·-子类可根据需求重写
	 * </pre>
	 */
	public void updateRank() {
	}

}
