/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package game.utils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * <pre>
 * 与Map计算相关的工具类
 * </pre>
 * 
 * @author yuxuan.chen
 * @time 2018年7月9日 上午11:05:54
 */
public class MapUtil {

	/**
	 * <pre>
	 * 将map中有的Key上增加一个value，
	 * 若value为Integer，以前的值和现在的值相加之后再存进原key
	 * 若value为Long，以前的值和现在的值相加之后再存进原key
	 * 若value为String，以前的值和现在的拼接之后再存进原key
	 * ...（后续可以再增加）
	 * </pre>
	 *
	 * @param map
	 * @param key
	 * @param value
	 * @param module
	 * @param userId
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> void addValueToMapIfKey(final Map<? super K, ? super V> map, K key, V value, Object module, int userId) {
		if (map == null) {
			return;
		}
		if (value instanceof Integer) {
			int oldIntValue = getIntValue(map, key, module, userId);
			Integer nowIntValue = oldIntValue + (Integer) value;
			map.put(key, (V) nowIntValue);
			return;
		}
		if (value instanceof Long) {
			long oldIntValue = getLongValue(map, key, module, userId);
			Long nowIntValue = oldIntValue + (Long) value;
			map.put(key, (V) nowIntValue);
			return;
		}
		if (value instanceof String) {
			Object object = map.get(key);
			String oldIntValue = "";
			if (object != null) {
				oldIntValue = (String) object;
			}
			String nowIntValue = oldIntValue + (String) value;
			map.put(key, (V) nowIntValue);
			return;
		}
	}

	/**
	 * <pre>
	 * 获取整数值
	 * </pre>
	 *
	 * @param map
	 * @param key
	 * @param module
	 * @return
	 */
	public static <K> int getIntValue(final Map<? super K, ?> map, K key, Object module, int userId) {
		if (map != null) {
			final Object value = map.get(key);
			if (value instanceof Integer) {
				return ((Integer) value).intValue();
			} // 短整型
			if (value instanceof Short) {
				return ((Short) value).intValue();
			}
			// 长整型
			if (value instanceof Long) {
				long temp = ((Long) value).longValue();
				if (temp > Integer.MAX_VALUE) {
					return Integer.MAX_VALUE;
				} else {
					return ((Long) value).intValue();
				}
			}
			if (value instanceof String) {
				int parseInt = 0;
				try {
					parseInt = Integer.parseInt((String) value);
				} catch (Exception e) {
					Log.error("获取模块某数值时，发现原数据无法转整型，module:" + module.getClass().getSimpleName() + ",key:" + key + ",value:" + value + ",userId:" + userId);
				}
				return parseInt;
			}
		}
		return 0;
	}

	/**
	 * <pre>
	 * 获取长整数值
	 * </pre>
	 *
	 * @param map
	 * @param key
	 * @param module
	 * @return
	 */
	public static <K> long getLongValue(final Map<? super K, ?> map, K key, Object module, int userId) {
		if (map != null) {
			final Object value = map.get(key);
			if (value instanceof Integer) {
				return ((Integer) value).longValue();
			} // 短整型
			if (value instanceof Short) {
				return ((Short) value).longValue();
			}
			// 长整型
			if (value instanceof Long) {
				return ((Long) value).longValue();
			}
			if (value instanceof String) {
				long parseInt = 0;
				try {
					parseInt = Long.parseLong((String) value);
				} catch (Exception e) {
					Log.error("获取模块某数值时，发现原数据无法转整型，module:" + module.getClass().getSimpleName() + ",key:" + key + ",value:" + value + ",userId:" + userId);
				}
				return parseInt;
			}
		}
		return 0;
	}

	/**
	 * <pre>
	 * 把两个Map加在一起，把map2添加到map1中
	 * int  long 相加。string拼接
	 * </pre>
	 * 
	 * @param <K>
	 * @param <V>
	 * @param map1
	 * @param map2
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> void add2Map(final Map<? super K, ? super V> map1, Map<K, V> map2) {
		if (map2 == null || map2.isEmpty() || map1 == null) {
			return;
		}
		if (map1.isEmpty()) {
			map1.putAll(map2);
			return;
		}
		for (Entry<K, V> entry : map2.entrySet()) {
			if (map1.containsKey(entry.getKey())) {
				// value 类型是Integer，两值相加。
				if (entry.getValue() instanceof Integer) {
					int map2Value = getIntValue(map2, entry.getKey(), null, 1);
					int map1Value = getIntValue(map1, entry.getKey(), null, 1);
					Long newValue = (long) map2Value + map1Value;
					if (newValue > Integer.MAX_VALUE) {
						newValue = (long) Integer.MAX_VALUE;
					}
					map1.put(entry.getKey(), (V) newValue);
				}
				// value 类型是Long，两值相加。
				if (entry.getValue() instanceof Long) {
					long map2Value = getLongValue(map2, entry.getKey(), null, 1);
					long map1Value = getLongValue(map1, entry.getKey(), null, 1);
					Long newValue = map2Value + map1Value;
					map1.put(entry.getKey(), (V) newValue);
				}
				// value 类型是String，字符拼接。map2在后面
				if (entry.getValue() instanceof String) {
					String str1 = (String) map1.get(entry.getKey());
					String str2 = (String) map2.get(entry.getKey());
					String newValue = str1 + str2;
					map1.put(entry.getKey(), (V) newValue);
				}
			} else {
				map1.put(entry.getKey(), entry.getValue());
			}
		}
		return;
	}

	/**
	 * <pre>
	 * map的value乘以multiple
	 * 所有的key都乘以multiple
	 * </pre>
	 * 
	 * @param <V>
	 * @param map1
	 */
	public static <V> void expansionMapMultiple(final Map<String, V> map1, double multiple) {
		Set<String> keySet = map1.keySet();
		expansionMapMultiple(map1, keySet, multiple);
	}

	/**
	 * <pre>
	 * map的value乘以multiple
	 * </pre>
	 * 
	 * @param <V>
	 * @param map1
	 */
	public static <V> void expansionMapMultiple(final Map<String, V> map1, List<String> keys, double multiple) {
		Set<String> keySet = new HashSet<String>(keys);
		expansionMapMultiple(map1, keySet, multiple);
	}

	/**
	 * <pre>
	 * map的value乘以multiple
	 * </pre>
	 * 
	 * @param <V>
	 * @param map1
	 */
	@SuppressWarnings("unchecked")
	public static <V> void expansionMapMultiple(final Map<String, V> map1, Set<String> keys, double multiple) {
		if (map1 == null) {
			return;
		}
		if (map1.isEmpty()) {
			return;
		}
		for (String key : keys) {
			if (map1.containsKey(key)) {
				Object object = map1.get(key);
				if (object instanceof Integer) {
					int map1Value = getIntValue(map1, key, null, 1);
					Long newValue = (long) (map1Value * multiple);
					if (newValue > Integer.MAX_VALUE) {
						newValue = (long) Integer.MAX_VALUE;
					}
					map1.put(key, (V) newValue);
				}
				// value 类型是Long。
				if (object instanceof Long) {
					long map1Value = getLongValue(map1, key, null, 1);
					Long newValue = (long) (map1Value * multiple);
					if (newValue < 0 & map1Value > 0 && multiple > 0) {
						Log.error("两个Map值相加之后超出了long最大值,key:" + key + ",preValue:" + map1Value + ",multiple:" + multiple);
						newValue = Long.MAX_VALUE;
					}
					map1.put(key, (V) newValue);
				}
			}
		}
		return;
	}

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, Object> sortMapByKey(Map<String, Object> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}

		Map<String, Object> sortMap = new TreeMap<String, Object>(new MapKeyComparator());

		sortMap.putAll(map);

		return sortMap;
	}
}

/**
 * <pre>
 * 使用 Map按key进行排序规则
 * </pre>
 * 
 * @author yuxuan.chen
 * @time 2018年9月19日 上午12:37:58
 */
class MapKeyComparator implements Comparator<String> {

	@Override
	public int compare(String str1, String str2) {

		return str1.compareTo(str2);
	}
}
