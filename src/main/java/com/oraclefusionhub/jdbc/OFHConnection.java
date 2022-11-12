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

	public OFHConnection(String url, Properties properties) throws SQLException {

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
		return new OFHStatement(conn, reportPath);
	}

	@Override
	public PreparedStatement prepareStatement(String s) throws SQLException {
		System.out.println("PrepareStatement: " + s);
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
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return new DatabaseMetaData() {
			@Override
			public boolean allProceduresAreCallable() throws SQLException {
				return false;
			}

			@Override
			public boolean allTablesAreSelectable() throws SQLException {
				return false;
			}

			@Override
			public String getURL() throws SQLException {
				return fullURL;
			}

			@Override
			public String getUserName() throws SQLException {
				return null;
			}

			@Override
			public boolean isReadOnly() throws SQLException {
				return true;
			}

			@Override
			public boolean nullsAreSortedHigh() throws SQLException {
				return false;
			}

			@Override
			public boolean nullsAreSortedLow() throws SQLException {
				return false;
			}

			@Override
			public boolean nullsAreSortedAtStart() throws SQLException {
				return false;
			}

			@Override
			public boolean nullsAreSortedAtEnd() throws SQLException {
				return false;
			}

			@Override
			public String getDatabaseProductName() throws SQLException {
				return null;
			}

			@Override
			public String getDatabaseProductVersion() throws SQLException {
				return null;
			}

			@Override
			public String getDriverName() throws SQLException {
				return null;
			}

			@Override
			public String getDriverVersion() throws SQLException {
				return null;
			}

			@Override
			public int getDriverMajorVersion() {
				return 0;
			}

			@Override
			public int getDriverMinorVersion() {
				return 0;
			}

			@Override
			public boolean usesLocalFiles() throws SQLException {
				return false;
			}

			@Override
			public boolean usesLocalFilePerTable() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsMixedCaseIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public boolean storesUpperCaseIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public boolean storesLowerCaseIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public boolean storesMixedCaseIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
				return false;
			}

			@Override
			public String getIdentifierQuoteString() throws SQLException {
				return null;
			}

			@Override
			public String getSQLKeywords() throws SQLException {
				return null;
			}

			@Override
			public String getNumericFunctions() throws SQLException {
				return null;
			}

			@Override
			public String getStringFunctions() throws SQLException {
				return null;
			}

			@Override
			public String getSystemFunctions() throws SQLException {
				return null;
			}

			@Override
			public String getTimeDateFunctions() throws SQLException {
				return null;
			}

			@Override
			public String getSearchStringEscape() throws SQLException {
				return null;
			}

			@Override
			public String getExtraNameCharacters() throws SQLException {
				return null;
			}

			@Override
			public boolean supportsAlterTableWithAddColumn() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsAlterTableWithDropColumn() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsColumnAliasing() throws SQLException {
				return false;
			}

			@Override
			public boolean nullPlusNonNullIsNull() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsConvert() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsConvert(int fromType, int toType) throws SQLException {
				return false;
			}

			@Override
			public boolean supportsTableCorrelationNames() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsDifferentTableCorrelationNames() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsExpressionsInOrderBy() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsOrderByUnrelated() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsGroupBy() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsGroupByUnrelated() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsGroupByBeyondSelect() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsLikeEscapeClause() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsMultipleResultSets() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsMultipleTransactions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsNonNullableColumns() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsMinimumSQLGrammar() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsCoreSQLGrammar() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsExtendedSQLGrammar() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsANSI92EntryLevelSQL() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsANSI92IntermediateSQL() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsANSI92FullSQL() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsIntegrityEnhancementFacility() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsOuterJoins() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsFullOuterJoins() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsLimitedOuterJoins() throws SQLException {
				return false;
			}

			@Override
			public String getSchemaTerm() throws SQLException {
				return null;
			}

			@Override
			public String getProcedureTerm() throws SQLException {
				return null;
			}

			@Override
			public String getCatalogTerm() throws SQLException {
				return null;
			}

			@Override
			public boolean isCatalogAtStart() throws SQLException {
				return false;
			}

			@Override
			public String getCatalogSeparator() throws SQLException {
				return null;
			}

			@Override
			public boolean supportsSchemasInDataManipulation() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSchemasInProcedureCalls() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSchemasInTableDefinitions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSchemasInIndexDefinitions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsCatalogsInDataManipulation() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsCatalogsInProcedureCalls() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsCatalogsInTableDefinitions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsPositionedDelete() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsPositionedUpdate() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSelectForUpdate() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsStoredProcedures() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSubqueriesInComparisons() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSubqueriesInExists() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSubqueriesInIns() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsSubqueriesInQuantifieds() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsCorrelatedSubqueries() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsUnion() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsUnionAll() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
				return false;
			}

			@Override
			public int getMaxBinaryLiteralLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxCharLiteralLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxColumnNameLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxColumnsInGroupBy() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxColumnsInIndex() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxColumnsInOrderBy() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxColumnsInSelect() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxColumnsInTable() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxConnections() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxCursorNameLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxIndexLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxSchemaNameLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxProcedureNameLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxCatalogNameLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxRowSize() throws SQLException {
				return 0;
			}

			@Override
			public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
				return false;
			}

			@Override
			public int getMaxStatementLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxStatements() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxTableNameLength() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxTablesInSelect() throws SQLException {
				return 0;
			}

			@Override
			public int getMaxUserNameLength() throws SQLException {
				return 0;
			}

			@Override
			public int getDefaultTransactionIsolation() throws SQLException {
				return 0;
			}

			@Override
			public boolean supportsTransactions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
				return false;
			}

			@Override
			public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
				return false;
			}

			@Override
			public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
				return false;
			}

			@Override
			public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
				return false;
			}

			@Override
			public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
					throws SQLException {
				return null;
			}

			@Override
			public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
					String columnNamePattern) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
					throws SQLException {
				return null;
			}

			@Override
			public ResultSet getSchemas() throws SQLException {
				return null;
			}

			@Override
			public ResultSet getCatalogs() throws SQLException {
				return null;
			}

			@Override
			public ResultSet getTableTypes() throws SQLException {
				return null;
			}

			@Override
			public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern,
					String columnNamePattern) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
					throws SQLException {
				return null;
			}

			@Override
			public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
					throws SQLException {
				return null;
			}

			@Override
			public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope,
					boolean nullable) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable,
					String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getTypeInfo() throws SQLException {
				return null;
			}

			@Override
			public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique,
					boolean approximate) throws SQLException {
				return null;
			}

			@Override
			public boolean supportsResultSetType(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
				return false;
			}

			@Override
			public boolean ownUpdatesAreVisible(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean ownDeletesAreVisible(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean ownInsertsAreVisible(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean othersUpdatesAreVisible(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean othersDeletesAreVisible(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean othersInsertsAreVisible(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean updatesAreDetected(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean deletesAreDetected(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean insertsAreDetected(int type) throws SQLException {
				return false;
			}

			@Override
			public boolean supportsBatchUpdates() throws SQLException {
				return false;
			}

			@Override
			public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
					throws SQLException {
				return null;
			}

			@Override
			public Connection getConnection() throws SQLException {
				return null;
			}

			@Override
			public boolean supportsSavepoints() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsNamedParameters() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsMultipleOpenResults() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsGetGeneratedKeys() throws SQLException {
				return false;
			}

			@Override
			public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)
					throws SQLException {
				return null;
			}

			@Override
			public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern)
					throws SQLException {
				return null;
			}

			@Override
			public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
					String attributeNamePattern) throws SQLException {
				return null;
			}

			@Override
			public boolean supportsResultSetHoldability(int holdability) throws SQLException {
				return false;
			}

			@Override
			public int getResultSetHoldability() throws SQLException {
				return 0;
			}

			@Override
			public int getDatabaseMajorVersion() throws SQLException {
				return 0;
			}

			@Override
			public int getDatabaseMinorVersion() throws SQLException {
				return 0;
			}

			@Override
			public int getJDBCMajorVersion() throws SQLException {
				return 0;
			}

			@Override
			public int getJDBCMinorVersion() throws SQLException {
				return 0;
			}

			@Override
			public int getSQLStateType() throws SQLException {
				return 0;
			}

			@Override
			public boolean locatorsUpdateCopy() throws SQLException {
				return false;
			}

			@Override
			public boolean supportsStatementPooling() throws SQLException {
				return false;
			}

			@Override
			public RowIdLifetime getRowIdLifetime() throws SQLException {
				return null;
			}

			@Override
			public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
				return null;
			}

			@Override
			public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
				return false;
			}

			@Override
			public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
				return false;
			}

			@Override
			public ResultSet getClientInfoProperties() throws SQLException {
				return null;
			}

			@Override
			public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
					throws SQLException {
				return null;
			}

			@Override
			public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern,
					String columnNamePattern) throws SQLException {
				return null;
			}

			@Override
			public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
					String columnNamePattern) throws SQLException {
				return null;
			}

			@Override
			public boolean generatedKeyAlwaysReturned() throws SQLException {
				return false;
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}
		};
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
		return null;
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
		return null;
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
		System.out.println("isvalid: " + i);
		return true;
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
