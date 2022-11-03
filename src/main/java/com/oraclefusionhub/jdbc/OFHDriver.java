package com.oraclefusionhub.jdbc;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class OFHDriver implements Driver {
	private static final Driver INSTANCE = new OFHDriver();
	private static boolean registered;

	public static synchronized Driver load() {
		if (!registered) {
			registered = true;
			try {
				DriverManager.registerDriver(INSTANCE);
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}

		return INSTANCE;
	}

	static {
		load();
	}

	public OFHDriver() {}

	@Override
	public Connection connect(String url, Properties properties) throws SQLException {
		System.out.println("Props: " + properties);
		url = getURL(url);

		return new OFHConnection(url, properties);
	}

	@Override
	public boolean acceptsURL(String url) {
		url = getURL(url);
		return (url != null && (url.startsWith("http") || url.startsWith("https")));
	}

	private String getURL(String fullJDBCPath) {
		String jdbcPath = "jdbc:ofh://";
		return fullJDBCPath.substring(fullJDBCPath.lastIndexOf(jdbcPath) + 11);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException {
		DriverPropertyInfo reportPathProp = new DriverPropertyInfo("reportPath", "/Custom/OracleCloudSQLQuery/SQLQuery.xdo");
		DriverPropertyInfo[] dpi = new DriverPropertyInfo[1];
		dpi[0] = reportPathProp;

		return  dpi;
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public boolean jdbcCompliant() {
		return true;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}
}
