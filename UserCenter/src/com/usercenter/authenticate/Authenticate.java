package com.usercenter.authenticate;

import java.util.HashMap;
import java.util.Map;

public abstract class Authenticate {
	
	public  Map<String,AuthenticateAction> authActionMap= new HashMap<String,AuthenticateAction>();
	
	public abstract void init();

}
