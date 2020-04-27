package com.base.template;

import com.base.template.base.BaseTemplate;

public class ConstantTemplate extends BaseTemplate {

	private String id;

	private String value;
	
	

	@Override
	public void initStr() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getId() {
		// TODO Auto-generated method stub
		return id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setId(String id) {
		this.id = id;
	}

}
