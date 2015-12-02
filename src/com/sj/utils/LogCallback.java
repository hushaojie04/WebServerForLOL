package com.sj.utils;

public class LogCallback {
	static int count = 0;
	private StringBuilder mLogInfo = new StringBuilder();

	public void catchLogInfo(String info) {
		mLogInfo.append("\n" + info);
	}

	public String showLogInfo() {
		return mLogInfo != null ? mLogInfo.toString() : "";
	}

	public void clear() {
		mLogInfo = new StringBuilder("LogCallback" + count++ + " :");
	}
}
