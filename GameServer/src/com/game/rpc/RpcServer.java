package com.game.rpc;

import java.rmi.registry.LocateRegistry;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.base.rpc.IService;
import com.game.rpc.impl.ServiceImpl;
import com.utils.Log;

public class RpcServer {

	public static boolean start() {
		try {
			IService service = new ServiceImpl();
			// 本地主机上的远程对象注册表Registry的实例，并指定端口为8888，这一步必不可少（Java默认端口是1099），
			// 必不可缺的一步，缺少注册表创建，则无法绑定对象到远程注册表上
			LocateRegistry.createRegistry(8888);
			// 初始化命名空间
			Context namingContext = new InitialContext();
			// 将名称绑定到对象,即向命名空间注册已经实例化的远程服务对象
			namingContext.rebind("rmi://127.0.0.1:8888/service", service);

		} catch (Exception e) {
			Log.error("rpc server start fail! message:" + e.getMessage());
			return false;
		}

		return true;
	}

	public static void main(String[] aa) {
		start();
	}
}
