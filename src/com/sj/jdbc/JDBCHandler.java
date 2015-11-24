package com.sj.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import com.sj.utils.LogUtil;

public class JDBCHandler {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "#####################";
	static final String USER = "#############";
	static final String PASS = "##############";
	Connection conn = null;
	Statement stmt = null;

	public JDBCHandler() {
		// STEP 2: Register JDBC driver
		connectJDBC();
	}

	private void connectJDBC() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// STEP 3: Open a connection
			LogUtil.print("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// STEP 4: Execute a query
			LogUtil.print("Creating statement...");
			stmt = conn.createStatement();
 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.print("ClassNotFoundException " + e.getLocalizedMessage());

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.print("SQLException " + e.getErrorCode());

		} finally {
		}
	}

	public String query(String sql) {
		LogUtil.print("query:" + sql);

		String result = "";
		try {
			ResultSet rs = stmt.executeQuery(sql);
			result = resultSetToJson(rs);
			// STEP 6: Clean-up environment
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.print("getErrorCode:" + e.getErrorCode());
			LogUtil.print("getSQLState:" + e.getSQLState());
			LogUtil.print("getMessage:" + e.getMessage());

			connectJDBC();
			return query(sql);
		}
		return result;
	}

	public void update(String sql) {
		LogUtil.print("update:" + sql);

		try {
			stmt.executeUpdate(sql);
			// STEP 6: Clean-up environment
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.print("getErrorCode:" + e.getErrorCode());
			LogUtil.print("getSQLState:" + e.getSQLState());
			LogUtil.print("getMessage:" + e.getMessage());

			connectJDBC();
			try {
				stmt.executeUpdate(sql);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public String resultSetToJson(ResultSet rs) {
		// json����
		JSONArray array = new JSONArray();
		try {
			// ��ȡ����
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			// ����ResultSet�е�ÿ������
			while (rs.next()) {
				JSONObject jsonObj = new JSONObject();

				// ����ÿһ��
				for (int i = 1; i <= columnCount; i++) {
					String columnName = metaData.getColumnLabel(i);
					String value = rs.getString(columnName);
					jsonObj.put(columnName, value);
				}
				// array.put();
				array.add(jsonObj);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array.toString();
	}
}