package com.base.template;

import java.util.ArrayList;
import java.util.List;

import com.base.template.base.BaseTemplate;

import io.netty.util.internal.StringUtil;
import proto.BaseProto.ResourcesPt;

/**
 * 收益buff配置
 * 
 * @author reison
 *
 */
public class InComeBuffTemplate extends BaseTemplate {

	/** buffid **/
	private int id;

	/** buff分类 **/
	private int category;

	/** 倍率 **/
	private int multiple;
	/** 持续时间 **/
	private int keepTime;

	private String consume;

	private List<ResourcesPt.Builder> consumeList = new ArrayList<ResourcesPt.Builder>();

	public String getConsume() {
		return consume;
	}

	@Override
	public void initStr() {
		if (StringUtil.isNullOrEmpty(this.consume)) {
			return;
		}
		String[] strs = this.consume.split(";");
		for (String s : strs) {
			int id = Integer.parseInt(s.split("_")[0]);
			double num = Double.parseDouble(s.split("_")[1]);
			ResourcesPt.Builder builder = ResourcesPt.newBuilder();
			builder.setId(id);
			builder.setNum(num);
			consumeList.add(builder);
		}
	}

	public void setConsume(String consume) {
		this.consume = consume;
	}

	public List<ResourcesPt.Builder> getConsumeList() {
		return consumeList;
	}

	public int getMultiple() {
		return multiple;
	}

	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}

	public int getKeepTime() {
		return keepTime;
	}

	public long getKeepTimeMilliSeconds() {
		return keepTime * 1000;
	}

	public void setKeepTime(int keepTime) {
		this.keepTime = keepTime;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getConfigId() {
		return id;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	@Override
	public Object getId() {
		return id;
	}
}
