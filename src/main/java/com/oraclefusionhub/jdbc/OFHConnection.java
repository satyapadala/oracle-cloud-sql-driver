package com.oraclefusionhub.jdbc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class OFHConnection implements java.sql.Connection {
	private static final String reportPath = "/Custom/OracleCloudSQLQuery/SQLQuery.xdo";
	private String fullURL;
	private String basicAuth;
	private Boolean closed;
	private final boolean debugEnabled;

	public OFHConnection(String url, Properties properties) throws SQLException {

		this.closed = false;
		this.debugEnabled = isDebugEnabled(properties);

		try {
			String user;
			if (properties.containsKey("user") && properties.get("user") != null) {
				user = (String) properties.get("user");
			} else {
				throw new SQLException("Failed to extract user information");
			}

			String password;
			if (properties.containsKey("password") && properties.get("password") != null) {
				password = (String) properties.get("password");
			} else {
				throw new SQLException("Failed to extract password information");
			}

			String userpass = user + ":" + password;
			String basicAuth = "Basic "
					+ new String(Base64.getEncoder().encode(userpass.getBytes(StandardCharsets.UTF_8)));

			this.fullURL = url + "/xmlpserver/services/ExternalReportWSSService";
			this.basicAuth = basicAuth;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new SQLException("Unable to create connection: " + ex.getMessage());
		}

	}

	@Override
	public Statement createStatement() throws SQLException {
		URL url;
		try {
			url = new URL(this.fullURL);
		} catch (MalformedURLException e) {
			throw new SQLException("invalid URL: " + fullURL);
		}

		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
		} catch (IOException e) {
			throw new SQLException("error while opening connection: " + e.getStackTrace());
		}

		conn.setRequestProperty("SOAPAction", "runReport");
		conn.setRequestProperty("Content-Type", "application/soap+xml");
		conn.setRequestProperty("SOAPAction", "runReport");
		conn.setRequestProperty("Authorization", this.basicAuth);
		return new OFHStatement(conn, reportPath, debugEnabled);
	}

	@Override
	public PreparedStatement prepareStatement(String s) throws SQLException {
		debug("PrepareStatement: " + s);
		return null;
	}

	@Override
	public CallableStatement prepareCall(String s) throws SQLException {
		return null;
	}

	@Override
	public String nativeSQL(String s) throws SQLException {
		return null;
	}

	@Override
	public void setAutoCommit(boolean b) throws SQLException {

	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return false;
	}

	@Override
	public void commit() throws SQLException {

	}

	@Override
	public void rollback() throws SQLException {

	}

	@Override
	public void close() throws SQLException {
		this.closed = true;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return this.closed;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return new OFHDatabaseMetaData(this);

	}

	@Override
	public void setReadOnly(boolean b) throws SQLException {

	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return true;
	}

	@Override
	public void setCatalog(String s) throws SQLException {

	}

	@Override
	public String getCatalog() throws SQLException {
		return null;
	}

	@Override
	public void setTransactionIsolation(int i) throws SQLException {

	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return 0;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {

	}

	@Override
	public Statement createStatement(int i, int i1) throws SQLException {
		return createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String s, int i, int i1) throws SQLException {
		return null;
	}

	@Override
	public CallableStatement prepareCall(String s, int i, int i1) throws SQLException {
		return null;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return null;
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

	}

	@Override
	public void setHoldability(int i) throws SQLException {

	}

	@Override
	public int getHoldability() throws SQLException {
		return 0;
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return null;
	}

	@Override
	public Savepoint setSavepoint(String s) throws SQLException {
		return null;
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {

	}

	@Override
	public Statement createStatement(int i, int i1, int i2) throws SQLException {
		return createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String s, int i, int i1, int i2) throws SQLException {
		return null;
	}

	@Override
	public CallableStatement prepareCall(String s, int i, int i1, int i2) throws SQLException {
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String s, int i) throws SQLException {
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String s, int[] ints) throws SQLException {
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String s, String[] strings) throws SQLException {
		return null;
	}

	@Override
	public Clob createClob() throws SQLException {
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return null;
	}

	@Override
	public boolean isValid(int i) throws SQLException {
		debug("isvalid: " + i);
		return true;
	}

	private boolean isDebugEnabled(Properties properties) {
		Object debugProperty = properties.get("debug");
		if (debugProperty != null) {
			return Boolean.parseBoolean(String.valueOf(debugProperty));
		}

		return Boolean.parseBoolean(System.getProperty("ofh.debug", "false"));
	}

	private void debug(String message) {
		if (debugEnabled) {
			System.out.println(message);
		}
	}

	@Override
	public void setClientInfo(String s, String s1) throws SQLClientInfoException {

	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {

	}

	@Override
	public String getClientInfo(String s) throws SQLException {
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return null;
	}

	@Override
	public Array createArrayOf(String s, Object[] objects) throws SQLException {
		return null;
	}

	@Override
	public Struct createStruct(String s, Object[] objects) throws SQLException {
		return null;
	}

	@Override
	public void setSchema(String s) throws SQLException {

	}

	@Override
	public String getSchema() throws SQLException {
		return null;
	}

	@Override
	public void abort(Executor executor) throws SQLException {

	}

	@Override
	public void setNetworkTimeout(Executor executor, int i) throws SQLException {

	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return 0;
	}

	@Override
	public <T> T unwrap(Class<T> aClass) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> aClass) throws SQLException {
		return false;
	}
}
