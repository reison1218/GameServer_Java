package com.base.template;

import com.base.template.base.BaseTemplate;

/**
 * 资源配置表
 * 
 * @author reison
 *
 */
public class ResourcesTemplate extends BaseTemplate {

	/** 资源id **/
	private int id;

	/** 资源名称 **/
	private String name;
	
	@Override
	public void initStr() {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public Object getId() {
		return id;
	}

}
