package com.usercenter.template.mgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.usercenter.template.SeasonTemplate;
import com.usercenter.template.base.BaseTemplate;

public class SeasonTemplateMgr extends TemplateMgr{
	
	Map<Integer,List<SeasonTemplate>> stMap = new HashMap<>();

	@Override
	public void initData(List<BaseTemplate> list) {
		for(BaseTemplate bt:list) {
			if(!stMap.containsKey(bt.getId())) {
				stMap.put((int)bt.getId(), new ArrayList<>());
			}
			List<SeasonTemplate> _list = stMap.get(bt.getId());
			_list.add((SeasonTemplate)bt);
		}
	}
	
	public int getNextSeasonId(int gameId,int seasonId) {
		List<SeasonTemplate> list = stMap.get(gameId);
		if(list == null) {
			return 0;
		}
		int index = 0;
		for(int i=0;i<list.size();i++) {
			SeasonTemplate st = list.get(i);
			int id = (int)st.getId();
			if(id != gameId) {
				continue;
			}
			index = i;
			break;
		}
		if (index == list.size()-1) {
			index = 0;
		}
		SeasonTemplate st = list.get(index);
		return (int)st.getId();
	}
}

