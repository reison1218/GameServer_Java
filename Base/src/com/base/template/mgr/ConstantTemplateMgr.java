package com.base.template.mgr;

import java.util.List;

import com.base.template.ConstantTemplate;
import com.base.template.base.BaseTemplate;

/**
 * 常量表mgr
 * 
 * @author reison
 *
 */
public class ConstantTemplateMgr extends TemplateMgr {

	@Override
	public void initData(List<BaseTemplate> list) {

	}

	/**
	 * 通过id拿到配置
	 * 
	 * @param id
	 * @return
	 */
	public Object getValueById(String id) {
		BaseTemplate template = getTemlateById(id);
		if (template == null)
			return null;
		ConstantTemplate t = (ConstantTemplate) template;
		return t.getValue();
	}

	public String getValue(String id) {
		BaseTemplate template = getTemlateById(id);
		if (template == null)
			return null;
		ConstantTemplate t = (ConstantTemplate) template;
		return (String) t.getValue();
	}

	public int getIntValue(String id) {
		BaseTemplate template = getTemlateById(id);
		if (template == null)
			return 0;
		ConstantTemplate t = (ConstantTemplate) template;
		return Integer.parseInt((String) t.getValue());
	}

	public double getDoubleValue(String id) {
		BaseTemplate template = getTemlateById(id);
		if (template == null)
			return 0;
		ConstantTemplate t = (ConstantTemplate) template;
		return Double.parseDouble((String) t.getValue());
	}

	public float getFloatValue(String id) {
		BaseTemplate template = getTemlateById(id);
		if (template == null)
			return 0;
		ConstantTemplate t = (ConstantTemplate) template;
		return Float.parseFloat((String) t.getValue());
	}

}