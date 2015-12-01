package com.sj.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sj.utils.LogUtil;

public class JdbcUtil {

	/**
	 * @Field: pool 数据库连接池
	 */
	private static Pool pool = new Pool();

	public static Connection getConnection() throws SQLException {
		Connection connection = pool.getConnection();
		return connection;
	}

	public static void release(Connection conn, Statement st, ResultSet rs) {
		LogUtil.print("release=" + conn.hashCode());

		if (rs != null) {
			try {
				// 关闭存储查询结果的ResultSet对象
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rs = null;
		}
		if (st != null) {
			try {
				// 关闭负责执行SQL命令的Statement对象
				st.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (conn != null) {
			try {
				// 关闭Connection数据库连接对象
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
