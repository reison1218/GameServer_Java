package com.usercenter.entity;

import java.util.Date;

import com.utils.TimeUtil;

/**
 * 用户封装类
 * @author reison
 *
 */
public class UserInfo {
	/**用户id**/
	private int userId;
	/**gameid**/
	private int gameId;
	/**用户昵称**/
	private String nickName;
	/**用户真实姓名**/
	private String realName;
	/**用户手机号**/
	private String phoneNo;
	/**用户注册ip**/
	private String registerIp;
	/**用户创建时间**/
	private Date createTime;
	/**用户注册平台**/
	private String registerPlatform;
	/**用户平台id**/
	private String platformId;
	
	/**
	 * 转换成object数组
	 * @return
	 */
	public Object[] toObjectArray() {
		Object[] oj = new Object[9];
		oj[0] = userId;
		oj[1] = gameId;
		oj[2] = nickName;
		oj[3] = realName;
		oj[4] = phoneNo;
		oj[5] = registerIp;
		oj[6] = TimeUtil.getDateFormat(createTime);
		oj[7] = registerPlatform;
		oj[8] = platformId;
		return oj;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("userId:");
		sb.append(userId);
		sb.append(",");
		sb.append("gameId:");
		sb.append(gameId);
		sb.append(",");
		sb.append("realName:");
		sb.append(nickName);
		sb.append(",");
		sb.append("realName:");
		sb.append(realName);
		sb.append(",");
		sb.append("phoneNo:");
		sb.append(phoneNo);
		sb.append(",");
		sb.append("registerIp:");
		sb.append(registerIp);
		sb.append(",");
		sb.append("createTime:");
		sb.append(TimeUtil.getDateFormat(createTime));
		sb.append(",");
		sb.append("registerPlatform:");
		sb.append(registerPlatform);
		sb.append(",");
		sb.append("platformId:");
		sb.append(platformId);
		return sb.toString();
	}
	
	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getPhoneNo() {
		return phoneNo;
	}
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	public String getRegisterIp() {
		return registerIp;
	}
	public void setRegisterIp(String registerIp) {
		this.registerIp = registerIp;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getRegisterPlatform() {
		return registerPlatform;
	}
	public void setRegisterPlatform(String registerPlatform) {
		this.registerPlatform = registerPlatform;
	}
	public String getPlatformId() {
		return platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}
	
	

}
