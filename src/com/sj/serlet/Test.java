package com.sj.serlet;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sj.jdbc.JDBCHandler;
import com.sj.utils.LogUtil;

/**
 * Servlet implementation class Test
 */
@WebServlet("/Test")
public class Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
	JDBCHandler mJDBCHandler;
	Map<String, String> attrMap = new HashMap<String, String>();
	JSONArray tempArray = new JSONArray();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Test() {
		super();
		// TODO Auto-generated constructor stub
		mJDBCHandler = new JDBCHandler();
		// mJDBCHandler.update(queryChildrenAreaInfo());

		String data = mJDBCHandler.query("SELECT *  FROM lol_arcatt",false);
		LogUtil.print(data);
		JSONArray jsonArray = JSONArray.fromObject(data);
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject object = jsonArray.getJSONObject(i);
			attrMap.put(object.getString("att"), object.getString("attname"));
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *      http://localhost:8080/JavaWebApp/action/read?table=lol_archives
	 *      http://localhost:8080/JavaWebApp/action/read?arttype=0
	 *      http://localhost
	 *      :8080/JavaWebApp/action/read?arttype=1&start=0&end=20
	 *      http://localhost
	 *      :8080/JavaWebApp/action/read?arttype=1&start=0&end=20
	 * 
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String data = "null";
		LogUtil.print("---------doGet-------------{");
		LogUtil.print("request.getPathInfo()=" + request.getPathInfo());
		LogUtil.print("request.getQueryString()=" + request.getQueryString());

		Map<String, String[]> map = request.getParameterMap();
		String sql = "";
		if (request.getPathInfo().contains("/read")) {
			if (request.getQueryString().contains("table")) {
				sql = "SELECT * FROM " + request.getParameter("table");
				data = mJDBCHandler.query(sql,false);
			} else if (request.getQueryString().contains("arttype")) {
				sql = createSQLForArcType(request.getParameter("arttype"),
						request.getParameter("start"),
						request.getParameter("end"));
				data = handleData(mJDBCHandler.query(sql,hasCondition),
						request.getParameter("arttype"));
			} else if (request.getQueryString().contains("aid")
					&& request.getQueryString().contains("typeid")) {
				sql = createSQLForAddonarticle(request.getParameter("aid"),
						request.getParameter("typeid"));
				data = mJDBCHandler.query(sql,false);
			}
		} else if (request.getPathInfo().contains("/refresh")) {
			LogUtil.print("---refresh---");
			if (request.getQueryString().contains("arttype")) {
				// check position
				if (request.getQueryString().contains("aid")
						&& request.getQueryString().contains("typeid")) {
					tempArray.clear();
					int position = checkUpdate(request.getParameter("arttype"),
							request.getParameter("aid"),
							request.getParameter("typeid"), 0);
					// if has newest data that do query and response
					if (position > 0) {
						sql = createSQLForArcType(
								request.getParameter("arttype"), "0", ""
										+ position);
						// data = handleData(mJDBCHandler.query(sql),
						// request.getParameter("arttype"));
						data = tempArray.toString();
						tempArray.clear();
					}
				}

			}
		}
		LogUtil.print("}---------doGet-------------");
		// data = URLDecoder.decode(data, "utf-8");
		byte g[] = data.getBytes();
		// response.setHeader("Content-Encoding", "gzip");
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Content-Length", g.length + "");
		response.getOutputStream().write(g);
	}

	public int checkUpdate(String arttype, String aid, String typeid,
			int checkpos) {
		LogUtil.print("---checkUpdate num---" + checkpos / 5);

		String sql = createSQLForArcType(arttype, "" + checkpos, ""
				+ (checkpos + 5));
		String data = handleData(mJDBCHandler.query(sql, hasCondition), arttype);
		LogUtil.print("---data---" + data);

		JSONArray array = JSONArray.fromObject(data);
		// aid=1&typeid=7
		int position = -1;

		for (int i = 0; i < array.size(); i++) {
			JSONObject object = array.getJSONObject(i);
			if (object.getString("id").equals(aid)
					&& object.getString("typeid").equals(typeid)) {
				position = i;
				LogUtil.print("---position---" + position);
				break;
			}
			tempArray.add(object);
		}
		if (position == -1 && checkpos < 100) {
			position = checkUpdate(arttype, aid, typeid, checkpos + 5);
		}
		return position;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private String handleData(String data, String reid) {
		if (!reid.equals("0")) {
			JSONArray jsonArray = JSONArray.fromObject(data);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				object.put("flag", attrMap.get(object.getString("flag")));
			}
			data = jsonArray.toString();
		}
		return data;
	}

	private String createSQLForAddonarticle(String aid, String typeid) {
		String sql = "SELECT body,flash,shipin,tx  FROM lol_addonarticle Where aid="
				+ aid + " AND " + "typeid=" + typeid;
		return sql;
	}

	boolean hasCondition;

	private String createSQLForArcType(String reid, String param1, String param2) {
		String sql = "";
		// sql =
		// "select * from lol_archives where FIND_IN_SET(typeid, queryChildrenAreaInfo(6));";
		// if (false) {
		switch (reid) {
		case "0":
			hasCondition = false;
			sql = "SELECT ID,typename  FROM lol_arctype Where reID = 0";
			break;

		default:
			hasCondition = true;
			String condition = "SET @cond = queryChildrenAreaInfo(" + reid
					+ ")";
			mJDBCHandler.update(condition);
			if (param1 == null || param2 == null) {
				sql = "SELECT lol_archives.id,lol_archives.typeid,lol_archives.title,lol_archives.senddate,"
						+ "lol_archives.shorttitle,lol_archives.writer,lol_archives.description,"
						+ "lol_archives.flag,lol_archives.keywords,lol_archives.litpic"
						+ " FROM lol_archives"
						+ " Where "
						+ "FIND_IN_SET(typeid,@cond)"
						+ " ORDER BY lol_archives.senddate DESC";
			} else {
				sql = "SELECT lol_archives.id,lol_archives.typeid,lol_archives.title,lol_archives.senddate,"
						+ "lol_archives.shorttitle,lol_archives.writer,lol_archives.description,"
						+ "lol_archives.flag,lol_archives.keywords,lol_archives.litpic"
						+ " FROM lol_archives"
						+ " Where "
						+ "FIND_IN_SET(typeid,@cond)"
						+ " ORDER BY lol_archives.senddate DESC"
						+ " LIMIT "
						+ param1 + "," + param2;
			}

			break;
		}
		// }
		return sql;
	}

	private String queryChildrenAreaInfo() {
		String function = "DROP FUNCTION IF EXISTS queryChildrenAreaInfo;"
				+ "CREATE FUNCTION queryChildrenAreaInfo(areaId INT)\r\n"
				+ "RETURNS VARCHAR(4000)\r\n"
				+ "BEGIN \r\n"
				+ "DECLARE sTemp VARCHAR(4000);"
				+ "DECLARE sTempChd VARCHAR(4000);"
				+ "SET sTemp = '$';"
				+ "SET sTempChd = cast(areaId as char);"
				+ "WHILE sTempChd is not NULL DO "
				+ "SET sTemp = CONCAT(sTemp,',',sTempChd);"
				+ "SELECT group_concat(id) INTO sTempChd FROM lol_arctype where FIND_IN_SET(reid,sTempChd)>0;"
				+ "END WHILE;" + "return sTemp;" + "END;";
		return function;
	}
}
