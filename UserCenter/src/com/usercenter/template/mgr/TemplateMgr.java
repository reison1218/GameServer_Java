package com.usercenter.template.mgr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usercenter.base.config.SystemEnv;
import com.usercenter.template.base.BaseTemplate;
import com.utils.ClassUtil;
import com.utils.Log;
import com.utils.StringUtils;

/**
 * 读取json配置文件mgr
 * 
 * @author reison
 *
 */
public abstract class TemplateMgr {

	Map<Object, BaseTemplate> templateMap = new HashMap<Object, BaseTemplate>();

	static Map<Class<?>, TemplateMgr> mgrMap = new HashMap<Class<?>, TemplateMgr>();

	/**
	 * 初始化配置表
	 * 
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static boolean init() {
		String appPath = System.getenv("PWD");
		if (SystemEnv.isProduction() && appPath == null) {
			Log.error("服务器启动错误，请从/xxx/xxx/app目录运行启动脚本！curPath:" + appPath);
			return false;
		}
		// template路径
		String filePath = "/Users/tangjian/git/GameServer_Java/UserCenter/template";
		if (!StringUtils.isEmpty(appPath)) {
			filePath = appPath + "/template";
		}
		Log.info("开始加载json配置文件..." + filePath);
		File file = new File(filePath);
		JSONArray jsonArray = null;
		Map<String, Class<?>> allClasses = ClassUtil.getClassesMap(TemplateMgr.class.getPackage());
		List<BaseTemplate> templateList = null;
		try {
			TemplateMgr mgr = null;
			String name = null;
			// 读取json配置文件
			for (File _file : file.listFiles()) {
				name = getFileName(_file.getName());
				if (!name.endsWith("template")) {
					name+="Template";
				}
				Class<?> clazz = Class.forName("com.usercenter.template." + name);
				Class<?> _clazz = allClasses.get(name + "Mgr");
				mgr = (TemplateMgr) _clazz.newInstance();
				mgrMap.put(_clazz, mgr);
				String content = FileUtils.readFileToString(_file, "UTF-8");
				jsonArray = JSON.parseArray(content);
				templateList = new ArrayList<BaseTemplate>();
				Iterator<Object> it = jsonArray.iterator();
				JSONObject json = null;
				BaseTemplate bt = null;
				while (it.hasNext()) {
					json = (JSONObject) it.next();
					bt = (BaseTemplate) json.toJavaObject(json, clazz);
					bt.initStr();
					templateList.add(bt);
					mgr.templateMap.put(bt.getId(), bt);
				}
				if (!allClasses.containsKey(name + "Mgr"))
					continue;
				mgr.initData(templateList);
			}

			Log.info("json配置加载成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 获得名字
	 * 
	 * @param name
	 * @return
	 */
	private static String getFileName(String name) {
		if (name == null || "".equals(name))
			return "";
		name = name.substring(0, name.indexOf("."));
		return name;
	}

	/**
	 * 每个子类可以重构配置数据结构
	 * 
	 * @param list
	 */
	public abstract void initData(List<BaseTemplate> list);

	/**
	 * 通过class获取配置表mgr
	 * 
	 * @param clazz
	 * @return
	 */
	public static TemplateMgr getTemlateMgr(Class<?> clazz) {
		return mgrMap.get(clazz);
	}

	/**
	 * 通过id获得配置
	 * 
	 * @param id
	 * @return
	 */
	public BaseTemplate getTemlateById(Object id) {
		return templateMap.get(id);
	}

	/**
	 * 拿到所有配置
	 * 
	 * @return
	 */
	public Collection<BaseTemplate> getAll() {
		return templateMap.values();
	}
}
