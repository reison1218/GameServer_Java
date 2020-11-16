package com;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class OtherTest {

	public static Integer k = 0;

	public static void main(String[] aa) {

//		ByteBuf bb = Unpooled.buffer(1024);
//		
//		long start = System.currentTimeMillis();
//		for(int i=0;i<999999;i++) {
//			bb.writeInt(1);
//		}
//		
//		for(int i=0;i<999999;i++) {
//			bb.readInt();
//		}
//		long end = System.currentTimeMillis();
//		
//		System.out.println("time:"+(end-start)+"ms");
		String str = "test";
		test(str);
		System.out.println(str);
	}
	
	public static void test(String str) {
		str = "aa";
		System.out.println(str);
	}
}
