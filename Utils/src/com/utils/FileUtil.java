/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <pre>
 * 文件工具类
 * </pre>
 * 
 * @author rui.yan
 * @time 2018年3月23日 下午4:13:42
 */
public class FileUtil {

	/**
	 * <pre>
	 * 文件写入
	 * </pre>
	 *
	 * @param FileUrl 如: E:\boss.txt
	 * @param content 需要写入的内容
	 */
	public final static void outputFile(String FileUrl, String content) {
		BufferedWriter bw = null;
		PrintWriter pw = null;
		try {
			File file = new File(FileUrl);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			pw = new PrintWriter(fos);
			bw = new BufferedWriter(pw);
			bw.write(content);
			bw.flush();
			System.out.println("文件写入完成~");
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				pw.close();
				bw.close();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

}
