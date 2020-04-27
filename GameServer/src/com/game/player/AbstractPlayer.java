/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.game.player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.base.dao.SQLExecutor;
import com.base.executor.AbstractActionQueue;
import com.base.executor.ExecutorMgr;
import com.base.key.PlayerKey;
import com.base.mgr.GateChannelMgr;
import com.base.module.AnnModule;
import com.base.module.IModule;
import com.base.module.ModuleType;
import com.base.module.TableType;
import com.base.module.able.Jsonable;
import com.base.netty.packet.Packet;
import com.base.player.IPlayer;
import com.game.code.impl.GameServerCode;
import com.game.module.impl.ModuleLocation;
import com.utils.ClassUtil;
import com.utils.Log;
import com.utils.MathUtil;
import com.utils.StringUtils;
import com.utils.TimeUtil;

import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.StringUtil;

/**
 * <pre>
 * 抽象玩家类
 * </pre>
 * 
 * @author reison
 * @param <T>
 * @time 2019年7月27日
 */
public abstract class AbstractPlayer extends AbstractActionQueue implements IPlayer, Jsonable {

	/** 用户Id */
	protected int userId;

	/** 所有模块的单例集合<类全名,单例> */
	protected Map<String, IModule> moduleMap = new ConcurrentHashMap<>();

	/** 玩家基础数据(数据库用JSON存储) */
	protected Map<String, Object> dataMap = new ConcurrentHashMap<>();

	/** 改变的数据key列表 */
	protected final Set<String> changeList = new ConcurrentSet<>();

	/** 数据状态 */
	protected AtomicBoolean update = new AtomicBoolean();

	/** 模块名称 */
	protected static String moduleName = "player";

	/** 包含区服的昵称，如S1.随意玩玩 */
	protected String nickNameWithSite;

	public final static boolean init() {
		SQLExecutor.checkCreateTable(moduleName, "玩家", TableType.JSON_TABLE_ONLY);
		return true;
	}

	public AbstractPlayer(int userId) {
		super(ExecutorMgr.getPlayerExecutor().getExecutor());
		this.userId = userId;
		// 初始化模块
		initModule();
	}

	/**
	 * <pre>
	 *  
	 * 调用模块方法
	 * ！反射调用,100W次 200ms左右 —— 逻辑：for (int i = 0; i < 1000; i++);
	 * </pre>
	 *
	 * @param moduleClass 模块类
	 * @param mthName     方法名称
	 * @param userId      用户Id
	 * @param reqInfo     参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T> T invoke(Class<? extends IModule> moduleClass, String mthName, Packet packet, Class<?>... clazzs) {
		// 获取模块
		IModule module = getModule(moduleClass);
		// debug
		StringBuilder debug = new StringBuilder();
		debug.append(moduleClass.getSimpleName());
		debug.append(".");
		debug.append(mthName);
		debug.append("()");
		if (packet != null) {
			debug.append(packet.toString());
		}
		if (module == null) {
			Log.error("找不到该模块:" + debug);
			return null;
		}
		// 获取方法
		Method mth = null;
		try {
			if (packet != null) {
				mth = module.getClass().getMethod(mthName, Packet.class);
			} else if (clazzs.length > 0) {
				mth = module.getClass().getMethod(mthName, clazzs);
			} else {
				mth = module.getClass().getMethod(mthName);
			}
			if (mth == null) {
				Log.error("找不到该方法:" + debug);
				return null;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			Log.error("反射获取模块方法出错:" + debug, e);
			return null;
		}
		// 反射执行方法/返回值
		try {
			if (packet != null) {
				return (T) mth.invoke(module, packet);
			} else if (clazzs.length > 0) {
				return (T) mth.invoke(module, packet);
			} else {
				return (T) mth.invoke(module);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.error("反射调用模块方法出错:" + debug, e);
		}
		return null;
	}

	/**
	 * <pre>
	 * 管理员调用模块方法
	 * </pre>
	 *
	 * @param moduleClass 模块类
	 * @param mthName     方法名称
	 * @param userId      用户Id
	 * @param params      参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T> T invokeAdmin(Class<? extends IModule> moduleClass, String mthName, Class<?>[] params,
			Object... objects) {
		// 获取单例
		IModule module = getModule(moduleClass);
		StringBuilder debug = new StringBuilder();
		debug.append(moduleClass.getName());
		debug.append(".");
		debug.append(mthName);
		debug.append("()");
		if (module == null) {
			Log.error("找不到该模块:" + debug);
			return null;
		}
		Method mth = null;
		try {
			mth = module.getClass().getMethod(mthName, params);
			if (mth == null) {
				Log.error("找不到该方法:" + debug);
				return null;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			Log.error("反射获取模块方法出错:" + debug, e);
			return null;
		}
		try {
			Object[] newParams = Arrays.copyOfRange(objects, 0, params.length);
			for (int i = 0, len = params.length; i < len; i++) {
				Class<T> clazz = (Class<T>) params[i];
				switch (clazz.getSimpleName().toLowerCase()) {
				case "int":
					try {
						if (newParams[i] instanceof String) {
							newParams[i] = (int) Integer.parseInt((String) newParams[i]);
						} else {
							newParams[i] = (int) newParams[i];
						}
					} catch (Exception e) {
						newParams[i] = 0;
					}
					break;
				case "short":
					try {
						newParams[i] = ((Number) newParams[i]).shortValue();
					} catch (Exception e) {
						newParams[i] = (short) 0;
					}
					break;
				case "long":
					try {
						newParams[i] = (long) newParams[i];
					} catch (Exception e) {
						newParams[i] = (long) 0;
					}
					break;
				case "string":
					try {
						newParams[i] = (String) newParams[i];
					} catch (Exception e) {
						newParams[i] = "";
					}
					break;
				default:
					break;
				}
			}
			return (T) mth.invoke(module, newParams);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.error("反射调用模块方法出错:" + debug, e);
		}
		return null;
	}

	/**
	 * <pre>
	 * 初始化所有模块单例，并检测重复
	 * </pre>
	 *
	 * @return
	 */
	public boolean initModule() {
		AnnModule ann = null;
		Map<String, Class<?>> classMap = new HashMap<>();
		// 模块包中的所有模块
		Set<Class<?>> allClasses = ClassUtil.getClasses(ModuleLocation.class.getPackage());
		for (Class<?> clazz : allClasses) {
			try {
				// 检测是否包含annModule注解
				ann = clazz.getAnnotation(AnnModule.class);
				if (ann != null) {
					// 返回类的名字
					String name = clazz.getSimpleName();
					// 返回全局限名
					String fullName = clazz.getName();
					// 检测有没有重名的模块
					if (classMap.containsKey(name)) {
						Log.error("模块类名存在重复，请检查：" + classMap.get(name).getName() + "以及" + fullName);
						return false;
					}
					// 装进去
					classMap.put(name, clazz);
					IModule module = null;
					// 需卸载模块注册
					if (ann.type() == ModuleType.PLAYER_MODULE) {
						Constructor<?> constructor = clazz.getConstructor(this.getClass());
						if (constructor != null) {
							module = (IModule) constructor.newInstance(this);
							moduleMap.put(fullName, module);
						}
					}
				}
			} catch (Exception e) {
				Log.error("Load module obj error,name : " + clazz.getSimpleName(), e);
				return false;
			}
		}
		return true;
	}

	/**
	 * <pre>
	 * 通过模块类获取模块单例
	 * </pre>
	 *
	 * @param moduleClass
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final <T> T getModule(Class<T> moduleClass) {
		IModule m = moduleMap.get(moduleClass.getName());
		if (m == null) {
			Log.error("玩家对象中找不到模块,module:" + moduleClass.getName() + ",userId:" + userId);
			return null;
		}
		return (T) m;
	}

	/**
	 * @return
	 * @see com.hitalk.h5.base.lv.player.IPlayer#getUserId()
	 */
	@Override
	public int getUserId() {
		return this.userId;
	}

	public <T> void initData(String key, T t) {
		if (t == null) {
			Log.error("不能设置null值,key:" + key);
			return;
		}
		Object pre = getDataMap().get(key);
		if (pre == null) {
			getDataMap().put(key, t);
			update.getAndSet(true);
		}
	}

	/**
	 * <pre>
	 * 设置数据
	 * 获取时返回数据格式如下：
	 * 数字根据范围转化为int,long 浮点——>BigDecimal
	 * Date请用秒数或毫秒数，否则将返回yyyy-MM-dd HH:mm:ss类型字符串
	 * !为了提高性能、避免时区问题，建议牺牲一定可读性，使用int存储秒数表示Date
	 * </pre>
	 * 
	 * @param key
	 * @param t
	 */
	@Override
	public final <T> void setData(String key, T t) {
		if (t == null) {
			// Log.error("不能设置null值,key:" + key);
			return;
		}
		Object pre = getDataMap().get(key);
		if (pre == null || !pre.equals(t)) {
			update.getAndSet(true);
			getDataMap().put(key, t);
			changeList.add(key);
		}
	}

	/**
	 * @param key
	 * @param value
	 */
	@Override
	public final int addData(String key, int value) {
		if (value == 0) {
			return 0;
		}
		int preValue = MathUtil.getIntValue(getDataMap(), key, this, userId);
		long newValue = (long) preValue + value;
		if (newValue > Integer.MAX_VALUE) {
			Log.error("超出整型最大值,key:" + key + ",class:" + this.getClass().getSimpleName());
			newValue = Integer.MAX_VALUE;
		}
		getDataMap().put(key, (int) newValue);
		update.getAndSet(true);
		changeList.add(key);
		return value;
	}

	/**
	 * 添加数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Number addData(String key, Number value) {
		if (value == null) {
			return -1;
		}
		if (StringUtil.isNullOrEmpty(key))
			return -1;
		Number preValue = MathUtil.getNumberValue(getDataMap(), key, this, userId);
		Number newValue = 0;

		if (value instanceof Integer) {
			newValue = preValue.intValue() + (int) value;
		} else if (value instanceof Long) {
			newValue = preValue.longValue() + (long) value;
		} else if (value instanceof Float) {
			newValue = preValue.floatValue() + (float) value;
		} else if (value instanceof Double) {
			newValue = preValue.doubleValue() + (double) value;
		}
		if (newValue.doubleValue() < 0)
			newValue = 0;
		getDataMap().put(key, newValue);
		update.getAndSet(true);
		changeList.add(key);
		return value;
	}

	/**
	 * 添加数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Number addDoubleData(String key, double value) {

		double preValue = MathUtil.getDoubleValue(getDataMap(), key, this, userId);
		double newValue = 0;

		newValue = preValue + value;
		if (newValue < 0)
			newValue = 0;
		getDataMap().put(key, newValue);
		update.getAndSet(true);
		changeList.add(key);
		return value;
	}

	/**
	 * <pre>
	 * 获取整数数值
	 * </pre>
	 *
	 * @param key
	 * @return
	 */
	public final int getIntData(String key) {
		return MathUtil.getIntValue(getDataMap(), key, this, userId);
	}

	/**
	 * <pre>
	 * 获取双精度数值
	 * </pre>
	 *
	 * @param key
	 * @return
	 */
	public final double getDoubleData(String key) {
		return MathUtil.getDoubleValue(getDataMap(), key, this, userId);
	}

	/**
	 * <pre>
	 * 获取长整数数值
	 * </pre>
	 *
	 * @param key
	 * @return
	 */
	public final long getLongData(String key) {
		return MathUtil.getLongValue(getDataMap(), key, this, userId);
	}

	/**
	 * 获取字符串数值
	 * 
	 * @param key
	 * @return
	 */
	public final String getStringData(String key) {
		return MathUtil.getStringValue(getDataMap(), key, this, userId);
	}

	// /**
	// * <pre>
	// * 获取时间类型
	// * </pre>
	// *
	// * @param key
	// * @return
	// */
	// public final Date getDate(String key) {
	// Object o = getDataMap().get(key);
	// if (o == null) {
	// return null;
	// }
	// if (!(o instanceof String)) {
	// Log.error("获取模块Date数值时，发现数据类型错误，key:" + key + ",value:" + o);
	// return null;
	// }
	// Date date = TimeUtil.validDate((String) o);
	// if (date == null) {
	// Log.error("获取模块Date数值时，发现时间格式错误，key:" + key + ",value:" + o);
	// return null;
	// }
	// return date;
	// }

	/**
	 * <pre>
	 * 获取数据
	 * 返回数据格式：
	 * 数字根据类型：int,long或BigDecimal(浮点)
	 * Date请用秒数或毫秒数，否则将返回yyyy-MM-dd HH:mm:ss类型字符串
	 * !为了提高性能、避免时区问题，建议牺牲一定可读性，使用int存储秒数表示Date
	 * </pre>
	 * 
	 * @param key
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final <T> T getData(String key) {
		Object o = getDataMap().get(key);
		if (o == null) {
			return null;
		}
		return (T) o;
	}

	/**
	 * <pre>
	 * 玩家是否在线,有内存数据就在线
	 * </pre>
	 *
	 * @return
	 */
	public final boolean isOnline() {

		return true;
	}

	/** 统计发送字节数 */
	private final AtomicInteger sendBytes = new AtomicInteger();

	public final void printSendBytes() {
		System.err.println(TimeUtil.getDateFormat() + "LogSendBytes:" + sendBytes.get() / 1024 + "KB" + this);
	}

	public final void clearSendBytes() {
		sendBytes.getAndSet(0);
	}

	/**
	 * <pre>
	 * 发送数据包
	 * </pre>
	 *
	 * @param code
	 * @param body
	 */
	public final boolean sendClientPacket(int code, Object body) {
		if (code == 0) {
			return false;
		}
		if (!isOnline()) {
			return false;
		}
		if (body == null)
			return false;

		if (code != GameServerCode.COMPRESS) {
			// System.out.println("发送到客户端，code:" + code + ",len:" + buf.readableBytes() +
			// this + "\nbody:" + JsonUtil.stringify(body));
		}
		// 发送数据包
		int size = GateChannelMgr.sendOneClient(code, userId, body);
		// 统计发送到客户端的数据包
		sendBytes.addAndGet(size);
		return true;
	}

	/**
	 * <pre>
	 * 发送数据包
	 * </pre>
	 *
	 * @param code
	 * @param body
	 */
	public final boolean sendGatePacket(int code, Object body) {
		if (code == 0) {
			return false;
		}
		if (!isOnline()) {
			return false;
		}
		if (body == null)
			return false;

		if (code != GameServerCode.COMPRESS) {
			// System.out.println("发送到客户端，code:" + code + ",len:" + buf.readableBytes() +
			// this + "\nbody:" + JsonUtil.stringify(body));
		}
		// 发送数据包
		int size = GateChannelMgr.sendOneGate(code, userId, body);
		sendBytes.addAndGet(size);
		return true;
	}

	/**
	 * <pre>
	 * 发送空包
	 * </pre>
	 *
	 * @param code
	 * @return
	 */
	public final boolean sendClientPacket(short code) {
		return sendClientPacket(code, null);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return " UserId:" + userId;
	}

	/**
	 * <pre>
	 * 获取玩家所有模块
	 * </pre>
	 *
	 * @return
	 */
	public Collection<IModule> getAllModule() {
		return moduleMap.values();
	}

	/**
	 * <pre>
	 * 返回包含区服的昵称，如S1.随意玩玩
	 * </pre>
	 *
	 * @return
	 */
	public String getNickNameWithSite() {
		if (StringUtils.isEmpty(nickNameWithSite)) {
			nickNameWithSite = getStringData(PlayerKey.NICK_NAME);
		}
		return nickNameWithSite;
	}

	public void setNickNameWithSite(String str) {
		nickNameWithSite = str;
	}

	public Map<String, Object> getDataMap() {
		return dataMap;
	}

}
