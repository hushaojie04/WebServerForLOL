package com.sj.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.sj.utils.LogUtil;

public class JdbcPool implements DataSource {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//	static final String DB_URL = "jdbc:mysql://localhost/dedecmsv57gbk";
//	 static final String USER = "root";
//	 static final String PASS = "123qwe";
	 static final String DB_URL = "jdbc:mysql://103.38.43.218/qushiw";
	 static final String USER = "shaojie";
	 static final String PASS = "shaojie";

	private static LinkedList<Connection> listConnections = new LinkedList<Connection>();
	static {
		try {
			Class.forName(JDBC_DRIVER);
			LogUtil.print("Connecting to database...");
			for (int i = 0; i < 10; i++) {
				Connection conn;
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				// 将获取到的数据库连接加入到listConnections集合中，listConnections集合此时就是一个存放了数据库连接的连接池
				listConnections.add(conn);
			}
			LogUtil.print("init listConnections数据库连接池大小为"
					+ listConnections.size());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		LogUtil.print("getConnection listConnections数据库连接池大小为"
				+ listConnections.size());
		if (listConnections.size() > 0) {
			// 从listConnections集合中获取一个数据库连接
			final Connection conn = listConnections.removeFirst();
			LogUtil.print("listConnections数据库连接池大小是" + listConnections.size());
			// 返回Connection对象的代理对象
			Class[] interfaces = conn.getClass().getInterfaces();
			if (interfaces == null || interfaces.length <= 0) {
				interfaces = new Class[1];
				interfaces[0] = Connection.class;
			}
			return (Connection) Proxy.newProxyInstance(JdbcPool.class
					.getClassLoader(), interfaces,
					new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method,
								Object[] args) throws Throwable {
							if (!method.getName().equals("close")) {
								return method.invoke(conn, args);
							} else {
								// 如果调用的是Connection对象的close方法，就把conn还给数据库连接池
								listConnections.add(conn);
								LogUtil.print(conn
										+ "被还给listConnections数据库连接池了！！");
								LogUtil.print("listConnections数据库连接池大小为"
										+ listConnections.size());
								return null;
							}
						}
					});
		} else {
			throw new RuntimeException("对不起，数据库忙");
		}
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
