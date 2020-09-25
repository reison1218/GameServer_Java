package com.usercenter.entity;

/**
 * 服务器信息
 * @author tangjian
 *
 */
public class ServerInfo {
	/**id**/
	private int id;
	/**游戏id**/
	private int game_id;
	/**游戏名字**/
	private String name;
	/**服务器ip地址**/
	private String ip;
	/**tcp端口**/
	private int port;
	/**http端口**/
	private int http_port;

	/**
	 * 获取id
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * 设置id
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	

	public int getGame_id() {
		return game_id;
	}

	public void setGame_id(int game_id) {
		this.game_id = game_id;
	}

	/**
	 * 获取服务器名字
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置服务器名字
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取服务器ip
	 * @return
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * 设置服务器ip
	 * @param ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * 获取服务器暴露端口
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 设置服务端端口
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public int getHttp_port() {
		return http_port;
	}

	public void setHttp_port(int http_port) {
		this.http_port = http_port;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("id:");
		sb.append(id);
		sb.append(",");
		sb.append("game_id:");
		sb.append(""+game_id);
		sb.append(",");
		sb.append("name:");
		sb.append(name);
		sb.append(",");
		sb.append("ip:");
		sb.append(ip);
		sb.append(",");
		sb.append("port:");
		sb.append(port);
		sb.append("http_port:");
		sb.append(http_port);
		return sb.toString();
	}
}
