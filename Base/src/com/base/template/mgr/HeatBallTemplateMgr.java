package com.base.template.mgr;

import java.util.List;

import com.base.template.HeatBallTemplate;
import com.base.template.base.BaseTemplate;

/**
 * 星球配置mgr
 * 
 * @author reison
 *
 */
public class HeatBallTemplateMgr extends TemplateMgr {

	public int maxId = 0;

	@Override
	public void initData(List<BaseTemplate> list) {
		maxId = list.size();
	}

	public int getMaxId() {
		return maxId;
	}

	/**
	 * 根据配置id获得星球配置
	 * 
	 * @param id
	 * @return
	 */
	public HeatBallTemplate getTemplateById(int id) {
		return (HeatBallTemplate) templateMap.get(id);
	}
}
