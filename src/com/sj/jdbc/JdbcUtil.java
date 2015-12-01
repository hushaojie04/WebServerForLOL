package com.sj.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sj.utils.LogUtil;

public class JdbcUtil {

	/**
	 * @Field: pool ���ݿ����ӳ�
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
				// �رմ洢��ѯ�����ResultSet����
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			rs = null;
		}
		if (st != null) {
			try {
				// �رո���ִ��SQL�����Statement����
				st.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (conn != null) {
			try {
				// �ر�Connection���ݿ����Ӷ���
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
