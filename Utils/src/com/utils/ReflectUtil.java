package com.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.alibaba.fastjson.JSONObject;

public final class ReflectUtil {

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		if (clazz == null) {
			return null;
		}
		try {
			return clazz.getDeclaredMethod(name, parameterTypes);
		} catch (SecurityException e) {
			Log.error("", e);
		} catch (NoSuchMethodException e) {
			Log.error("", e);
		}
		return null;
	}

	public static <T> Method getMethod(T obj, String name, Class<?>... parameterTypes) {
		if (obj == null) {
			return null;
		}
		try {
			return obj.getClass().getDeclaredMethod(name, parameterTypes);
		} catch (SecurityException e) {
			Log.error("", e);
		} catch (NoSuchMethodException e) {
			Log.error("", e);
		}
		return null;
	}

	/**
	 * <pre>
	 * 对象转Map
	 * </pre>
	 *
	 * @param t
	 * @return
	 */
	public final static <T> JSONObject obj2Map(T t) {
		JSONObject jsonObj = new JSONObject();
		if (t == null) {
			return jsonObj;
		}
		Field[] fs = t.getClass().getDeclaredFields();
		for (int i = 0, len = fs.length; i < len; i++) {
			try {
				Field f = fs[i];
				f.setAccessible(true);
				jsonObj.put(f.getName(), f.get(t));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Log.error("", e);
			}
		}
		return jsonObj;
	}

	/**
	 * <pre>
	 * Map转对象
	 * </pre>
	 *
	 * @param t
	 * @return
	 */
	public final static <T> T map2obj(JSONObject map, Class<T> clazz) {
		T t = null;
		try {
			t = clazz.newInstance();
			if (map == null || map.isEmpty()) {
				return t;
			}
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				int mod = field.getModifiers();
				if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
					continue;
				}
				field.setAccessible(true);
				field.set(t, map.getObject(field.getName(), field.getType()));
			}
		} catch (Exception e) {
			Log.error("", e);
		}
		return t;
	}

	public static void main(String[] args) {
	}

}
