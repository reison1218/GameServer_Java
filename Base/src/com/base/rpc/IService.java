package com.base.rpc;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IService extends Remote{
	
	List<String> service(String content) throws RemoteException;
	
	void setStr(String str) throws RemoteException;

}
