package com.base.template.mgr;

import java.util.List;

import com.base.template.AdvertInComeTemplate;
import com.base.template.base.BaseTemplate;

public class AdvertInComeTemplateMgr extends TemplateMgr{

	@Override
	public void initData(List<BaseTemplate> list) {
	}
	
	
	public AdvertInComeTemplate getTemplateById(int id) {
		if(!templateMap.containsKey(id))
			return null;
		AdvertInComeTemplate at = (AdvertInComeTemplate)templateMap.get(id);
		return at; 
	}

}
