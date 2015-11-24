package com.sj.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	public static void print(String message) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		System.out.println(df.format(new Date()));
		message = df.format(new Date()) + ":" + message;
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream("d:log.txt", true));
			out.println(message);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
