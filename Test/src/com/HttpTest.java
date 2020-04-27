package com;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.utils.HttpUtil;

public class HttpTest {

	public static void main(String[] aa) {
//		Map<String, Object> paramMap = new HashMap<String,Object>();
//		paramMap.put("test","test");
//		paramMap.put("test1","test1");
//		System.out.println(HttpUtil.doPost("http://localhost:8080/save", paramMap,true));
		
		Map<String, Object> paramMap = new HashMap<String,Object>();
		paramMap.put("test","test");
		paramMap.put("test1","test1");
		System.out.println(HttpUtil.doPost("http://localhost:8080/save", paramMap,true));
	}
}
