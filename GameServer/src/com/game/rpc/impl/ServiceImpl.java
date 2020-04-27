package com.game.rpc.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import com.base.rpc.IService;

public class ServiceImpl extends UnicastRemoteObject implements IService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceImpl() throws RemoteException {
		super();
	}
	
	@Override
	public void setStr(String str) throws RemoteException {
		System.out.println();
	}

	@Override
	public List<String> service(String content) throws RemoteException {
		List<String> list = new ArrayList<String>();
		try {
			list.add("a");
			list.add("b");
			list.add("c");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
