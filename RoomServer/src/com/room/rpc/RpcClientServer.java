package com.room.rpc;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.base.rpc.IService;

public class RpcClientServer {

	public static void main(String[] aa) {
		String url = "rmi://localhost:8888/";
		try {
			Context namingContext = new InitialContext();
			// 检索指定的对象。 即找到服务器端相对应的服务对象存根
			IService service = (IService) namingContext.lookup(url + "service");
			long start = System.currentTimeMillis();
			for(int i=0;i<999999;i++) {
				service.service("test");
			}
			long end = System.currentTimeMillis();
			System.out.println(end - start);
			
			
			long _start = System.currentTimeMillis();
			for(int i=0;i<999999;i++) {
				service.setStr("sdf");
			}
			long _end = System.currentTimeMillis();
			System.out.println(_end - _start);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
