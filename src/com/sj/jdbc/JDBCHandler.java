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
	}

	public String query(String sql, boolean hasCondition) {
		Statement stmt = null;
		String result = "";
		ResultSet rs = null;
		// connectJDBC();
		Connection conn = null;
		try {
			conn = JdbcUtil.getConnection();
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
			JdbcUtil.release(conn, stmt, rs);
			update_info = null;
		}
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
