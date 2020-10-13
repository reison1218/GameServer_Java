package com.usercenter.template.mgr;

import java.util.List;

import com.usercenter.template.base.BaseTemplate;

public class SeasonTemplateMgr extends TemplateMgr{
	int firstSeasonId = 0;
	

	@Override
	public void initData(List<BaseTemplate> list) {
		if (list.isEmpty()) {
			return;
		}
		firstSeasonId = (int)list.get(0).getId();
	}
	
	public int getNextSeasonId(int _seasonId) {
		int seasonId = _seasonId+1;
		BaseTemplate bt = templateMap.get(seasonId);
		if(bt == null) {
			return firstSeasonId;
		}
		return (int)bt.getId();
	}
}

