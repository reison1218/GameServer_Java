package com.usercenter.template;

import com.usercenter.template.base.BaseTemplate;

/**
 * 赛季配置实体
 * @author reison
 *
 */
public class SeasonTemplate extends BaseTemplate implements Comparable<Integer>{
	
	/**赛季id**/
	private int id;
	/**元素**/
	private int element;
	

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void initStr() {
		
	}

	public int getElement() {
		return element;
	}

	public void setElement(int element) {
		this.element = element;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int compareTo(Integer o) {
		return id>=o?1:0;
	}
}
