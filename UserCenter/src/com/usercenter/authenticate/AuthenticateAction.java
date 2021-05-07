package com.usercenter.authenticate;


public abstract class AuthenticateAction {
	
	public int appId;
	public String url;
	
	public AuthenticateAction(int appId,String url) {
		this.appId = appId;
		this.url = url;
		init();
	}
	
	/**
	 * 初始化
	 */
	public abstract void init();
	
	/**
	 * 执行
	 */
	public abstract String execute();

}
