/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.base.config;

/**
 * <pre>
 * 系统工具类
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日 
 */
public final class SystemEnv {

	/**
	 * <pre>
	 * 是否为线上环境
	 * </pre>
	 *
	 * @return false表示本地eclipse环境，true表示jar包部署运行环境如内网、外网测试服、正式服
	 */
	public final static boolean isProduction() {
		return "production".equals(System.getenv("CUR_ENV"));
	}

	public static void main(String[] args) {
		// long st = System.currentTimeMillis();
		// for (int i = 0; i < 100000; i++) {
		// isProduction();
		// }
		// long ct = System.currentTimeMillis() - st;
		// System.out.println(ct);
	}

}
