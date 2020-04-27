package com.utils;

/**
 * key - value形式数据保存对象
 */
public class Pair {
	private int param1;
	private long param2;
	private int param3;
	private int param4;

	public Pair() {
	}

	public Pair(int param1, long param2) {
		this.param1 = param1;
		this.param2 = param2;
	}

	public Pair(int param1, long param2, int param3) {
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}

	public Pair(int param1, long param2, int param3, int param4) {
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
		this.param4 = param4;
	}

	public int getParam1() {
		return param1;
	}

	public long getParam2() {
		return param2;
	}

	public int getParam3() {
		return param3;
	}

	public int getParam4() {
		return param4;
	}

	public String getParam4Desc() {
		if (param4 == 0) {
			return "00";
		} else {
			return param4 + "";
		}
	}

	public String getParam2Desc() {
		if (param2 == 0) {
			return "00";
		} else {
			return param2 + "";
		}
	}

	public void setParam1(int param1) {
		this.param1 = param1;
	}

	public void setParam2(int param2) {
		this.param2 = param2;
	}

	public void setParam3(int param3) {
		this.param3 = param3;
	}

	public void setParam4(int param4) {
		this.param4 = param4;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return param1 + "_" + param2 + "_" + param3 + "_" + param4;
	}

}
