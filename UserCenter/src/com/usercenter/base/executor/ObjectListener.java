package com.usercenter.base.executor;

import java.util.EventListener;

public interface ObjectListener extends EventListener {

	public void onEvent(ObjectEvent event);

}
