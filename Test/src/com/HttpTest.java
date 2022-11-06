package com;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utils.HttpUtil;
import com.utils.RandomUtil;

public class HttpTest {

	public static void main(String[] aa) {
		Map<String,Object> map = new HashMap<>();
		map.put("test", "hello");
		String res = HttpUtil.doPost("http://127.0.0.1:7777/test", map, true);
		System.out.println(res);
	}
	
	static class Sort implements Comparator<Integer>{

		@Override
		public int compare(Integer o1, Integer o2) {
			return o2.compareTo(o1);
		}
		
	}
}
