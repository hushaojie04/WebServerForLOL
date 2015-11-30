package com.sj.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionWrapper implements InvocationHandler {
	private Connection originConnection;
	public Connection Connection;
	public long lastAccessTime = System.currentTimeMillis();

	public ConnectionWrapper(Connection conn) {

		Class[] interfaces = conn.getClass().getInterfaces();
		if (interfaces == null || interfaces.length <= 0) {
			interfaces = new Class[1];
			interfaces[0] = Connection.class;
		}
		Connection = (Connection) Proxy.newProxyInstance(conn.getClass()
				.getClassLoader(), interfaces, this);
		originConnection = conn;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		Object obj = null;
		if (method.getName().equals("close")) {
			Pool.pushBackToPool(this);
		} else {
			obj = method.invoke(originConnection, args);
		}
		lastAccessTime = System.currentTimeMillis();
		return obj;
	}

	public void close() {
		try {
			originConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
