package com.sj.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import com.sj.utils.LogUtil;

public class Pool {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	 static final String DB_URL = "jdbc:mysql://localhost/dedecmsv57gbk";
	 static final String USER = "root";
	 static final String PASS = "123qwe";
	private static LinkedList<ConnectionWrapper> m_notUsedConnection = new LinkedList<ConnectionWrapper>();
	private static HashSet<ConnectionWrapper> m_usedUsedConnection = new HashSet<ConnectionWrapper>();
	static private long m_lastClearClosedConnection = System
			.currentTimeMillis();
	public static long CHECK_CLOSED_CONNECTION_TIME = 10000; // 4
	static OnCheckAction mOnCheckAction;
	static {
		initDriver();
	}

	private static void initDriver() {
		Driver driver = null;
		try {
			driver = (Driver) Class.forName(JDBC_DRIVER).newInstance();
			installDriver(driver);
		} catch (Exception e) {
		}
	}

	public static void installDriver(Driver driver) {
		try {
			DriverManager.registerDriver(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized void pushBackToPool(ConnectionWrapper Connection) {
		boolean exist = m_usedUsedConnection.remove(Connection);
		LogUtil.print("pushBackToPool " + exist + " "
				+ Connection.Connection.hashCode());

		if (exist) {
			m_notUsedConnection.addLast(Connection);
			LogUtil.print("pushBackToPool m_notUsedConnection.size:"
					+ m_notUsedConnection.size());
		}
	}

	public static void setOnCheckAction(OnCheckAction action) {
		mOnCheckAction = action;
	}

	public static synchronized Connection getConnection() {
		clearClosedConnection();
		LogUtil.print("getConnection m_notUsedConnection.size:"
				+ m_notUsedConnection.size());

		while (m_notUsedConnection.size() > 0) {
			ConnectionWrapper wrapper = (ConnectionWrapper) m_notUsedConnection
					.removeFirst();
			try {
				if (wrapper.Connection.isClosed()) {
					continue;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mOnCheckAction != null
					&& !mOnCheckAction.checkAvailable(wrapper.Connection)) {
				LogUtil.print("close the error Connection");
				wrapper.close();
				continue;
			}
			m_usedUsedConnection.add(wrapper);
			LogUtil.print("m_usedUsedConnection:size=" + ""
					+ m_usedUsedConnection.size() + "");
			// if (DEBUG) {
			// wrapper.debugInfo = new Throwable(
			// "Connection initial statement");
			// }
			return wrapper.Connection;

		}
		int newCount = getIncreasingConnectionCount();
		LinkedList list = new LinkedList();
		ConnectionWrapper wrapper = null;
		LogUtil.print("newCount:" + newCount);
		for (int i = 0; i < newCount; i++) {
			wrapper = getNewConnection();
			if (wrapper != null) {
				list.add(wrapper);
			}
		}
		if (list.size() == 0) {
			return null;
		}
		wrapper = (ConnectionWrapper) list.removeFirst();
		m_usedUsedConnection.add(wrapper);
		m_notUsedConnection.addAll(list);
		list.clear();

		return wrapper.Connection;
	}

	private static ConnectionWrapper getNewConnection() {
		try {
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			return new ConnectionWrapper(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static int close() {
		int count = 0;

		Iterator iterator = m_notUsedConnection.iterator();
		while (iterator.hasNext()) {
			try {
				((ConnectionWrapper) iterator.next()).close();
				count++;
			} catch (Exception e) {
			}
		}
		m_notUsedConnection.clear();

		iterator = m_usedUsedConnection.iterator();
		while (iterator.hasNext()) {
			try {
				ConnectionWrapper wrapper = (ConnectionWrapper) iterator.next();
				wrapper.close();
				count++;
			} catch (Exception e) {
			}
		}
		m_usedUsedConnection.clear();

		return count;
	}

	private static void clearClosedConnection() {
		long time = System.currentTimeMillis();
		// sometimes user change system time,just return
		if (time < m_lastClearClosedConnection) {
			time = m_lastClearClosedConnection;
			return;
		}
		// no need check very often
		if (time - m_lastClearClosedConnection < CHECK_CLOSED_CONNECTION_TIME) {
			return;
		}
		m_lastClearClosedConnection = time;

		// begin check
		Iterator iterator = m_notUsedConnection.iterator();
		while (iterator.hasNext()) {
			ConnectionWrapper wrapper = (ConnectionWrapper) iterator.next();
			try {
				if (wrapper.Connection.isClosed()) {
					iterator.remove();
				}
			} catch (Exception e) {
				iterator.remove();
				// if (DEBUG) {
				// System.out
				// .println("connection is closed, this connection initial StackTrace");
				// wrapper.debugInfo.printStackTrace();
				// }
			}
		}

		// make connection pool size smaller if too big
		int decrease = getDecreasingConnectionCount();
		if (m_notUsedConnection.size() < decrease) {
			return;
		}

		while (decrease-- > 0) {
			ConnectionWrapper wrapper = (ConnectionWrapper) m_notUsedConnection
					.removeFirst();
			try {
				wrapper.Connection.close();
			} catch (Exception e) {
			}
		}
		LogUtil.print("clearClosedConnection m_notUsedConnection "
				+ m_notUsedConnection.size());
	}

	public static int getIncreasingConnectionCount() {
		int count = 1;
		int current = getConnectionCount();
		count = current / 4;
		if (count < 1) {
			count = 1;
		}
		return count;
	}

	public static int getDecreasingConnectionCount() {
		int count = 0;
		int current = getConnectionCount();
		if (current < 10) {
			return 0;
		}
		return current / 3;
	}

	public static synchronized int getNotUsedConnectionCount() {
		return m_notUsedConnection.size();
	}

	public static synchronized int getUsedConnectionCount() {
		return m_usedUsedConnection.size();
	}

	public static synchronized int getConnectionCount() {
		return m_notUsedConnection.size() + m_usedUsedConnection.size();
	}

	public interface OnCheckAction {
		boolean checkAvailable(Connection conn);
	}
}
