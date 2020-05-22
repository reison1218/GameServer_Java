package com.usercenter.action;

import com.usercenter.base.executor.Action;
import com.usercenter.base.executor.ActionQueue;
import com.usercenter.entity.UserInfo;
import com.usercenter.entity.UserInfoDao;

public class UpdateUserInfoAction extends Action{
	UserInfo userInfo;
	
	public UpdateUserInfoAction(ActionQueue queue,UserInfo _userInfo) {
		super(queue);
		userInfo = _userInfo;
	}

	@Override
	public void execute() {
		UserInfoDao.getInstance().updateUserInfo(userInfo);
	}
}
