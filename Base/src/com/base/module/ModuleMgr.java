/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.base.config.Config;
import com.base.dao.SQLExecutor;
import com.base.dbpool.HikariDBPool;
import com.game.module.impl.ModuleLocation;
import com.utils.ClassUtil;
import com.utils.Log;
import com.utils.RandomUtil;

import io.netty.util.internal.StringUtil;

/**
 * <pre>
 * 模块管理
 * </pre>
 * 
 * @author reison
 * @time 2017年3月1日
 */
public final class ModuleMgr {

	/** 模块注解Map<类全路径名,AnnModule> */
	private final static Map<String, AnnModule> moduleMap = new HashMap<>();

	/** Rank模块注解列表 */
	private final static List<AnnModule> rankList = new ArrayList<>();

	private final static Map<String, Class<?>> rankClazzMap = new HashMap<>();

	/** Rank模块类型Map */
	private final static Map<String, AnnModule> rankTypeMap = new HashMap<>();

	/** 模块单例Map(用于处理服务器之间数据包) */
	private final static Map<String, List<IModule>> cmdModule = new HashMap<>();

	/**
	 * <pre>
	 * 初始化所有Key类注解，并检测重复
	 * </pre>
	 *
	 * @return
	 */
	public final static boolean init() {
		AnnModule ann = null;
		Map<String, Class<?>> classMap = new HashMap<>();
		Set<Class<?>> allClasses = ClassUtil.getClasses(ModuleLocation.class.getPackage());
		for (Class<?> clazz : allClasses) {
			try {
				ann = clazz.getAnnotation(AnnModule.class);
				if (ann != null) {
					String name = ann.name().toLowerCase();
					if (classMap.containsKey(name)) {
						Log.error("存在重复的module名称，请检查注解：" + classMap.get(name).getSimpleName() + "以及"
								+ clazz.getSimpleName());
						return false;
					}
					moduleMap.put(clazz.getName(), ann);
					if (!StringUtil.isNullOrEmpty(name)) {
						classMap.put(name, clazz);
					}
					// 检测表是否存在，不存在就创建
					if (!TableType.NO_TABLE.compare(ann.tableType())) {
						SQLExecutor.checkCreateTable(name, ann.comment(), ann.tableType());
					}
					// 排行榜模块
					if (ann.type() == ModuleType.RANK_MODULE) {
						String rankType = ann.rankType();
						rankTypeMap.put(rankType, ann);
						rankClazzMap.put(rankType, clazz);
						// 只有非通用排行榜(非小榜)才需要保存数据库
						if (!rankType.equals("") && !ann.name().equals("")) {
							rankList.add(ann);
						}
					}
					// 非玩家单例模块
					if (ann.type() == ModuleType.CMD_MODULE) {
						String clazzName = clazz.getName();
						List<IModule> mList = cmdModule.get(clazzName);
						if (mList == null) {
							mList = new ArrayList<>();
							cmdModule.put(clazzName, mList);
						}
						for (int i = 0; i < 8; i++) {
							mList.add((IModule) clazz.newInstance());
						}
					}
				}
			} catch (Exception e) {
				Log.error("Load Key class error,name : " + clazz.getSimpleName(), e);
				return false;
			}
		}
		Log.info("带注解的模块初始化成功，数量：" + classMap.size());
		return true;
	}

	/**
	 * <pre>
	 * 获取模块名称
	 * </pre>
	 *
	 * @param module
	 * @return
	 */
	public final static String getModuleName(Object module) {
		AnnModule ann = moduleMap.get(module.getClass().getName());
		if (ann != null) {
			return ann.name().toLowerCase();
		}
		return null;
	}

	/**
	 * <pre>
	 * 获取模块注解
	 * </pre>
	 *
	 * @param module
	 * @return
	 */
	public final static AnnModule getModuleAnn(Class<?> clazz) {
		AnnModule ann = moduleMap.get(clazz.getName());
		if (ann != null) {
			return ann;
		}
		return null;
	}

	/**
	 * <pre>
	 * 是否需要初始化属性管理
	 * </pre>
	 *
	 * @param module
	 * @return
	 */
	public final static boolean needAttrModule(Object module) {
		AnnModule ann = moduleMap.get(module.getClass().getName());
		if (ann != null) {
			return ann.needAttrModule();
		}
		return false;
	}

	/**
	 * <pre>
	 * 获取排行榜模块注解列表
	 * </pre>
	 *
	 * @return
	 */
	public final static List<AnnModule> getRankListModule() {
		return rankList;
	}

	/**
	 * <pre>
	 * 获取排行榜注解
	 * </pre>
	 *
	 * @param rankType
	 * @return
	 */
	public final static AnnModule getRankAnnByType(String rankType) {
		AnnModule ann = rankTypeMap.get(rankType);
		if (ann != null) {
			return ann;
		}
		return rankTypeMap.get("");
	}

	/**
	 * <pre>
	 * 获取排行榜类
	 * </pre>
	 *
	 * @param rankType
	 * @return
	 */
	public final static Class<?> getRankClassByType(String rankType) {
		return rankClazzMap.get(rankType);
	}

	/**
	 * <pre>
	 * 获取cmd模块
	 * </pre>
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T getCmdModule(Class<?> clazz) {
		String clazzName = clazz.getName();
		List<IModule> mList = cmdModule.get(clazzName);
		if (mList == null || mList.isEmpty()) {
			mList = new ArrayList<>();
			cmdModule.put(clazzName, mList);
			for (int i = 0; i < 8; i++) {
				try {
					mList.add((IModule) clazz.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					Log.error("创建实例异常，name:" + clazzName, e);
				}
			}
		}
		IModule m = mList.get(RandomUtil.rand(mList.size()));
		if (m == null) {
			Log.error("找不到cmd模块,module:" + clazz.getName());
			return null;
		}
		return (T) m;
	}

	public static void main(String[] args) {
		Config.init();
		HikariDBPool.init();
		init();
	}

}
