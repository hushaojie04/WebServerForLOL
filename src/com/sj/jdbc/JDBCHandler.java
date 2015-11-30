package com.sj.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import com.sj.utils.LogUtil;

public class JDBCHandler {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	public JDBCHandler() {
		// STEP 2: Register JDBC driver
	}

	Connection conn;

	//
	// private void connectJDBC() {
	// try {
	// Class.forName("com.mysql.jdbc.Driver");
	// // STEP 3: Open a connection
	// LogUtil.print("Connecting to database...");
	// conn = DriverManager.getConnection(DB_URL, USER, PASS);
	// // STEP 4: Execute a query
	//
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// LogUtil.print("ClassNotFoundException " + e.getLocalizedMessage());
	//
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// LogUtil.print("SQLException " + e.getErrorCode());
	//
	// } finally {
	// }
	// }

	public String query(String sql, boolean hasCondition) {
		LogUtil.print("query--------------------start");
		Statement stmt = null;
		String result = "";
		ResultSet rs = null;
		// connectJDBC();
		try {
			conn = JdbcUtil.getConnection();
			LogUtil.print("query----getConnection=" + conn.hashCode());
			stmt = conn.createStatement();
			if (hasCondition && update_info != null) {
				stmt.executeUpdate(update_info);
			}
			rs = stmt.executeQuery(sql);
			result = resultSetToJson(rs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showError(e);

		} finally {
			LogUtil.print("query----release=" + conn.hashCode());
			JdbcUtil.release(conn, stmt, rs);
			update_info = null;
		}
		LogUtil.print("query--------------------end");
		return result;
	}

	private void showError(Exception e) {
		LogUtil.print("getErrorCode:" + ((SQLException) e).getErrorCode()
				+ "\n getSQLState:" + ((SQLException) e).getSQLState()
				+ "\n getMessage:" + e.getMessage());
	}

	String update_info;

	public void update(String sql) {
		LogUtil.print("update:" + sql);
		update_info = sql;
	}

	public String resultSetToJson(ResultSet rs) {
		// json数组
		JSONArray array = new JSONArray();
		try {
			// 获取列数
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			// 遍历ResultSet中的每条数据
			while (rs.next()) {
				JSONObject jsonObj = new JSONObject();

				// 遍历每一列
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
