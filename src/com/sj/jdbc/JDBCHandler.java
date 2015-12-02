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

import com.sj.jdbc.Pool.OnCheckAction;
import com.sj.utils.LogUtil;

public class JDBCHandler {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	public JDBCHandler() {
		Pool.setOnCheckAction(new OnCheckAction() {

			@Override
			public boolean checkAvailable(Connection conn) {
				// TODO Auto-generated method stub
				Statement stmt = null;
				String result = "";
				ResultSet rs = null;
				try {
					stmt = conn.createStatement();
					rs = stmt.executeQuery("SELECT *  FROM lol_arcatt");
					result = resultSetToJson(rs);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					showError(conn, e);
					return false;
				} finally {
					try {
						stmt.close();
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			}

		});
	}

	public String query(String sql, boolean hasCondition) {
		return query(sql, hasCondition, true);
	}

	public String query(String sql, boolean hasCondition, boolean isrelease) {
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
			showError(conn, e);
			if (e.getSQLState().contains("08S01")
					&& e.getMessage().contains("Communications link failure")) {
				return null;
			}
		} finally {
			JdbcUtil.release(conn, stmt, rs);
			update_info = null;
		}
		return result;
	}

	private void showError(Connection conn, Exception e) {
		String log = "getErrorCode:" + ((SQLException) e).getErrorCode()
				+ "\n getSQLState:" + ((SQLException) e).getSQLState()
				+ "\n getMessage:" + e.getMessage();
		LogUtil.print(log);

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
