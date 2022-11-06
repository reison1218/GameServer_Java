package game.entity;

import java.util.Date;

import game.utils.TimeUtil;

/**
 * 用户封装类
 * @author reison
 *
 */
public class UserInfo {
	/**用户id**/
	private int userId;
	private String registerPlatform;
	/**最近一次登陆时间**/
	private Date lastLoginTime;
	/**最近登陆的服务器**/
	private String loginServers;
	
	/**
	 * 转换成object数组
	 * @return
	 */
	public Object[] toObjectArray() {
		Object[] oj = new Object[11];
		oj[0] = userId;
		oj[1] = registerPlatform;
		oj[2] = TimeUtil.getDateFormat(lastLoginTime);
		oj[4] = loginServers;
		return oj;
	}

	@Override
	public String toString() {
		return "UserInfo{" + "userId=" + userId + ", registerPlatform='" + registerPlatform + '\'' + ", lastLoginTime=" + lastLoginTime +
				", loginServers='" + loginServers + '\'' + '}';
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getRegisterPlatform() {
		return registerPlatform;
	}

	public void setRegisterPlatform(String registerPlatform) {
		this.registerPlatform = registerPlatform;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getLoginServers() {
		return loginServers;
	}

	public void setLoginServers(String loginServers) {
		this.loginServers = loginServers;
	}
}
