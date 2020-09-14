package com;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.utils.RandomUtil;

public class HttpTest {

	public static void main(String[] aa) {
//		Map<String, Object> paramMap = new HashMap<String,Object>();
//		paramMap.put("test","test");
//		paramMap.put("test1","test1");
//		System.out.println(HttpUtil.doPost("http://localhost:8080/save", paramMap,true));
		
//		Map<String, Object> paramMap = new HashMap<String,Object>();
//		paramMap.put("test","test");
//		paramMap.put("test1","test1");
//		System.out.println(HttpUtil.doPost("http://localhost:8080/save", paramMap,true));
		List<Integer> list = new ArrayList<Integer>();
		for(int i=0;i<99999;i++) {
			list.add(RandomUtil.rand(1, 99999));
		}
		
		Sort sort = new Sort();
		Date start = new Date();
		for(int i=0;i<9999;i++) {
			list.sort(sort);
		}
		Date end = new Date();
		System.out.println("time:"+(end.getTime()-start.getTime())+"ms");
		
		StringBuffer sb = new StringBuffer();
		for(Integer i:list) {
			sb.append(i);
			sb.append(",");
		}
		//System.out.println(sb.toString());
		
	}
	
	static class Sort implements Comparator<Integer>{

		@Override
		public int compare(Integer o1, Integer o2) {
			return o2.compareTo(o1);
		}
		
	}
}
