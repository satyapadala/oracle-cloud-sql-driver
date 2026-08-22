package com.oraclefusionhub.jdbc;

import java.sql.*;
import java.util.*;
import com.oraclefusionhub.jdbc.metadata.FinancialsMetadata;

public class OFHDatabaseMetaData implements DatabaseMetaData {
    private OFHConnection connection;
    private FinancialsMetadata metadata = FinancialsMetadata.getInstance();

    public OFHDatabaseMetaData(OFHConnection connection) {
        this.connection = connection;
    }

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
				return null;
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
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        List<String> header = Arrays.asList("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS", "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION");
        List<List<String>> rows = new ArrayList<>();
        
        Set<String> typeFilters = new HashSet<>();
        if (types != null) {
            for (String t : types) typeFilters.add(t.toUpperCase());
        }

        String regex = null;
        if (tableNamePattern != null && !tableNamePattern.isEmpty() && !tableNamePattern.equals("%")) {
            regex = tableNamePattern.replace("%", ".*").replace("_", ".");
        }

        for (FinancialsMetadata.MetadataObject obj : metadata.getObjects()) {
            if (!typeFilters.isEmpty() && !typeFilters.contains(obj.type != null ? obj.type.toUpperCase() : "TABLE")) {
                continue;
            }
            if (regex != null && !obj.name.toUpperCase().matches(regex.toUpperCase())) {
                continue;
            }
            
            rows.add(Arrays.asList(
                null, // TABLE_CAT
                null, // TABLE_SCHEM
                obj.name,
                obj.type != null ? obj.type : "TABLE",
                obj.description,
                null, null, null, null, null
            ));
        }

        return new OFHResultSet(header, rows);
    }


			
    @Override
    public ResultSet getSchemas() throws SQLException {
        return new OFHResultSet(Arrays.asList("TABLE_SCHEM", "TABLE_CATALOG"), new ArrayList<>());
    }


			
    @Override
    public ResultSet getCatalogs() throws SQLException {
        return new OFHResultSet(Arrays.asList("TABLE_CAT"), new ArrayList<>());
    }


			
    @Override
    public ResultSet getTableTypes() throws SQLException {
        List<String> header = Arrays.asList("TABLE_TYPE");
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("TABLE"));
        rows.add(Arrays.asList("VIEW"));
        return new OFHResultSet(header, rows);
    }


			
    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        List<String> header = Arrays.asList(
            "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", 
            "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", 
            "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", 
            "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG", 
            "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN"
        );
        List<List<String>> rows = new ArrayList<>();

        if (tableNamePattern == null) tableNamePattern = "%";
        String tblRegex = tableNamePattern.replace("%", ".*").replace("_", ".");
        
        String colRegex = null;
        if (columnNamePattern != null && !columnNamePattern.equals("%")) {
            colRegex = columnNamePattern.replace("%", ".*").replace("_", ".");
        }

        for (FinancialsMetadata.MetadataObject obj : metadata.getObjects()) {
            if (!obj.name.toUpperCase().matches(tblRegex.toUpperCase())) continue;
            
            if (obj.columns != null) {
                for (FinancialsMetadata.Column col : obj.columns) {
                    if (colRegex != null && !col.name.toUpperCase().matches(colRegex.toUpperCase())) continue;
                    
                    int dataType = Types.VARCHAR;
                    if (col.dataType != null) {
                        String dt = col.dataType.toUpperCase();
                        if (dt.contains("NUMBER")) dataType = Types.NUMERIC;
                        else if (dt.contains("DATE") || dt.contains("TIMESTAMP")) dataType = Types.TIMESTAMP;
                    }

                    rows.add(Arrays.asList(
                        null, null, obj.name, col.name, 
                        String.valueOf(dataType), 
                        col.dataType != null ? col.dataType : "VARCHAR2", 
                        null, null, null, "10",
                        col.nullable ? String.valueOf(DatabaseMetaData.columnNullable) : String.valueOf(DatabaseMetaData.columnNoNulls),
                        col.comment, null, null, null, null, 
                        String.valueOf(col.ordinal), 
                        col.nullable ? "YES" : "NO", 
                        null, null, null, null, "NO", "NO"
                    ));
                }
            }
        }
        return new OFHResultSet(header, rows);
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
        List<String> header = Arrays.asList("TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "KEY_SEQ", "PK_NAME");
        List<List<String>> rows = new ArrayList<>();
        
        if (table != null) {
            FinancialsMetadata.MetadataObject obj = metadata.getObject(table);
            if (obj != null && obj.primaryKeys != null) {
                int seq = 1;
                for (String pk : obj.primaryKeys) {
                    rows.add(Arrays.asList(
                        null, null, obj.name, pk, String.valueOf(seq++), "PK_" + obj.name
                    ));
                }
            }
        }
        return new OFHResultSet(header, rows);
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
        return getSchemas();
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
}
