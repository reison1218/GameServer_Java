package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author reison excel转json工具类
 */
public class ExcelUtils {

	public static void main(String[] aa) {
		excel2json(null);
	}

	/**
	 * excel转json函数
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static void excel2json(String path) {

		File file = new File("/Users/tangjian/Desktop/templates");
		if (file == null || file.listFiles().length == 0) {
			System.out.println("路径错误！没有可以读取的excel文件");
			return;
		}
		FileWriter fileWriter = null;
		InputStream in = null;

		System.out.println("excel2json方法执行....");

		// Excel列的样式，主要是为了解决Excel数字科学计数的问题
		CellStyle cellStyle;
		// 根据Excel构成的对象
		Workbook wb;
		List<Map<String, Object>> jsonList = null;
		int rowIndex = 0;
		int cellIndex = 0;
		Sheet sheet = null;
		long start = 0;
		long end = 0;
		File f = null;
		try {
			// 如果是2007及以上版本，则使用想要的Workbook以及CellStyle
			for (File _file : file.listFiles()) {
				if(_file.getName().equals(".DS_Store")) {
					continue;
				}
				f = _file;
				in = new FileInputStream(_file);
				// 是2007及以上版本 xlsx
				if (_file.getName().endsWith("xlsx")) {
					wb = new XSSFWorkbook(in);
					XSSFDataFormat dataFormat = (XSSFDataFormat) wb.createDataFormat();
					cellStyle = wb.createCellStyle();
					// 设置Excel列的样式为文本
					cellStyle.setDataFormat(dataFormat.getFormat("@"));
				} else {// 是2007以下版本 xls
					POIFSFileSystem fs = new POIFSFileSystem(in);
					wb = new HSSFWorkbook(fs);
					HSSFDataFormat dataFormat = (HSSFDataFormat) wb.createDataFormat();
					cellStyle = wb.createCellStyle();
					// 设置Excel列的样式为文本
					cellStyle.setDataFormat(dataFormat.getFormat("@"));
				}

				// sheet表个数
				int sheetsCounts = wb.getNumberOfSheets();
				String name;
				// 遍历每一个sheet
				for (int i = 0; i < sheetsCounts; i++) {
					jsonList = new ArrayList<Map<String, Object>>();
					rowIndex = 0;
					sheet = wb.getSheetAt(i);
					start = System.currentTimeMillis();
					Iterator<Row> rowIter = sheet.rowIterator();
					fileWriter = new FileWriter("/Users/tangjian/Desktop/test/" + sheet.getSheetName().trim() + ".json");
					// 第一行缓存下来
					Row firstRow = null;
					Row typeRow = null;
					while (rowIter.hasNext()) {
						Row row = rowIter.next();
						if(row.getCell(0)==null||StringUtils.isEmpty(row.getCell(0).toString()))
							continue;
						if (rowIndex > 1) {
							jsonList.add(new HashMap<String, Object>());
						}
							
						if (rowIndex == 0) {
							firstRow = row;
							rowIndex++;
							continue;
						}
						// 第二行缓存数据类型行
						if (rowIndex == 1) {
							typeRow = row;
							rowIndex++;
							continue;
						}
						// 前两行都跳过
						if (rowIndex < 2) {
							rowIndex++;
							continue;
						}

						Iterator<Cell> cellIter = row.cellIterator();
						cellIndex = 0;
						while (cellIter.hasNext()) {
							Cell cell = cellIter.next();
							if (StringUtils.isEmpty(cell.toString())) {
								cellIndex++;
								continue;
							}
							
							if(typeRow.getCell(cellIndex) == null) {
								cellIndex++;
								continue;
							}
							
							
							String dataType = typeRow.getCell(cellIndex).getStringCellValue();
							JSONArray jsonArray = null;
							Object data = null;
							switch (dataType) {
							case "int":
								data = Integer.parseInt(cell.toString().replace(".0", ""));
								break;
							case "string":
								data = new String(cell.toString());
								break;

							case "double":
								data = cell.getNumericCellValue();
								break;
							case "float":
								data = cell.getNumericCellValue();
								break;
							case "int[]":
								jsonArray = (JSONArray)JsonUtil.parse(cell.toString());
								data = jsonArray;
								break;
							case "json":
								data = JSONObject.parse(cell.toString());;
								break;
							}
							name = firstRow.getCell(cellIndex).getStringCellValue();
							if (name.endsWith("client")){
								cellIndex++;
								continue;
							}
							jsonList.get(rowIndex - 2).put(firstRow.getCell(cellIndex).getStringCellValue(), data);
							cellIndex++;
						}
						rowIndex++;
					}
					
					// 写json
					byte[] bytes = JsonUtil.binaryify(jsonList);
					String s = new String(bytes);
					fileWriter.write(s);
					fileWriter.flush();
					end = System.currentTimeMillis();
					System.out.println("生成" + sheet.getSheetName() + ".json文件，耗时" + (end - start) + "ms");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("文件：" + f.getName() + "，sheet:" + sheet.getSheetName());
			System.out.println("rowIndex:"+rowIndex);
			System.out.println("cellIndex:"+cellIndex);
		}

		System.out.println("excel2json方法结束....");

	}
}
