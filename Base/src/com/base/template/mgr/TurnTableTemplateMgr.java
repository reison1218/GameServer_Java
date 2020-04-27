package com.base.template.mgr;

import java.util.List;

import com.base.template.TurnTableTemplate;
import com.base.template.base.BaseTemplate;

/**
 * 转盘配置表
 * 
 * @author reison
 *
 */
public class TurnTableTemplateMgr extends TemplateMgr {

	private int totalWeight = 0;

	@Override
	public void initData(List<BaseTemplate> list) {
		if (list == null || list.isEmpty())
			return;
		for (BaseTemplate bt : list) {
			TurnTableTemplate tt = (TurnTableTemplate) bt;
			totalWeight += tt.getProbability();
		}
	}

	public int getTotalWeight() {
		return totalWeight;
	}

	public TurnTableTemplate getById(int id) {
		if (!templateMap.containsKey(id))
			return null;

		return (TurnTableTemplate) templateMap.get(id);
	}

}
