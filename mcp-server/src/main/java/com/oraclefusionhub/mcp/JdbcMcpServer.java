package com.oraclefusionhub.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class JdbcMcpServer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static volatile Connection connection;

    public static void main(String[] args) throws Exception {
        String jdbcUrl = System.getenv("JDBC_URL");
        String user = System.getenv("JDBC_USER");
        String pass = System.getenv("JDBC_PASSWORD");

        if (jdbcUrl == null || user == null || pass == null) {
            System.err.println("Error: JDBC_URL, JDBC_USER, and JDBC_PASSWORD environment variables are required.");
            System.err.println("Example JDBC_URL: jdbc:ofh://https://your-oracle-cloud-url");
            System.exit(1);
        }

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("safetyGuard", System.getenv().getOrDefault("JDBC_SAFETY_GUARD", "true"));
        props.setProperty("debug", System.getenv().getOrDefault("JDBC_DEBUG", "false"));

        StdioServerTransportProvider transport = new StdioServerTransportProvider(McpJsonDefaults.getMapper());

        Map<String, Object> executeSqlSchema = MAPPER.readValue("""
                {"type":"object","properties":{"query":{"type":"string","description":"The SQL SELECT or WITH (CTE) statement to execute"}},"required":["query"],"additionalProperties":false}
                """, new TypeReference<Map<String, Object>>() {});

        Map<String, Object> getTableSchemaSchema = MAPPER.readValue("""
                {"type":"object","properties":{"tableName":{"type":"string","description":"The exact table or view name (e.g., AP_INVOICES_ALL)"}},"required":["tableName"],"additionalProperties":false}
                """, new TypeReference<Map<String, Object>>() {});

        McpSyncServer server = McpServer.sync(transport)
                .serverInfo("ofh-mcp-server", "1.0.0")
                .capabilities(ServerCapabilities.builder()
                        .tools(true)
                        .build())
                .toolCall(
                        Tool.builder("execute_sql", executeSqlSchema)
                                .description("Executes a read-only SELECT or WITH (CTE) SQL query against the Oracle Cloud ERP/HCM database via the OFH JDBC driver and returns rows as JSON. The driver's safety guard enforces WHERE clauses or row limiters to prevent blind queries.")
                                .build(),
                        (exchange, request) -> executeSql(request))
                .toolCall(
                        Tool.builder("get_table_schema", getTableSchemaSchema)
                                .description("Fetches column names, data types, nullability, comments, and primary key information for a given Oracle Cloud database table or view using the driver's bundled offline metadata metastore. No network calls are made to Oracle Cloud.")
                                .build(),
                        (exchange, request) -> getTableSchema(request))
                .build();

        initConnectionAsync(jdbcUrl, props);

        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.closeGracefully();
            latch.countDown();
        }));

        latch.await();
    }

    private static void initConnectionAsync(String jdbcUrl, Properties props) {
        Thread t = new Thread(() -> {
            try {
                connection = DriverManager.getConnection(jdbcUrl, props);
            } catch (SQLException e) {
                System.err.println("JDBC connection failed: " + e.getMessage());
            }
        }, "jdbc-init");
        t.setDaemon(true);
        t.start();
    }

    private static Connection getConnection() throws SQLException {
        Connection conn = connection;
        if (conn != null) {
            return conn;
        }
        int waited = 0;
        while (connection == null && waited < 30000) {
            try {
                Thread.sleep(100);
                waited += 100;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for JDBC connection");
            }
        }
        if (connection == null) {
            throw new SQLException("JDBC connection is not available yet. Try again later.");
        }
        return connection;
    }

    private static CallToolResult executeSql(CallToolRequest request) {
        String query = (String) request.arguments().get("query");
        if (query == null || query.isBlank()) {
            return errorResult("Error: No query provided.");
        }

        String upper = query.trim().toUpperCase();
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            return errorResult("Error: Only SELECT or WITH (CTE) queries are permitted for safety.");
        }

        try (Statement stmt = getConnection().createStatement()) {
            stmt.setMaxRows(100);
            try (ResultSet rs = stmt.executeQuery(query)) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                List<Map<String, Object>> rows = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getString(i));
                    }
                    rows.add(row);
                }

                String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rows);
                return CallToolResult.builder()
                        .content(List.of(new TextContent(json)))
                        .isError(false)
                        .build();
            }
        } catch (SQLException e) {
            return errorResult("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            return errorResult("Error: " + e.getMessage());
        }
    }

    private static CallToolResult getTableSchema(CallToolRequest request) {
        String tableName = (String) request.arguments().get("tableName");
        if (tableName == null || tableName.isBlank()) {
            return errorResult("Error: No table name provided.");
        }

        try {
            DatabaseMetaData meta = getConnection().getMetaData();

            Set<String> primaryKeys = new HashSet<>();
            try (ResultSet pkRs = meta.getPrimaryKeys(null, null, tableName)) {
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
            }

            List<Map<String, Object>> columns = new ArrayList<>();
            try (ResultSet rs = meta.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("name", rs.getString("COLUMN_NAME"));
                    col.put("type", rs.getString("TYPE_NAME"));
                    col.put("size", rs.getString("COLUMN_SIZE"));
                    col.put("nullable", "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
                    col.put("comment", rs.getString("REMARKS"));
                    col.put("primaryKey", primaryKeys.contains(rs.getString("COLUMN_NAME")));
                    col.put("ordinal", rs.getString("ORDINAL_POSITION"));
                    columns.add(col);
                }
            }

            if (columns.isEmpty()) {
                return errorResult("No table found with name: " + tableName);
            }

            String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(columns);
            return CallToolResult.builder()
                    .content(List.of(new TextContent(json)))
                    .isError(false)
                    .build();
        } catch (SQLException e) {
            return errorResult("Metadata Error: " + e.getMessage());
        } catch (Exception e) {
            return errorResult("Error: " + e.getMessage());
        }
    }

    private static CallToolResult errorResult(String message) {
        return CallToolResult.builder()
                .content(List.of(new TextContent(message)))
                .isError(true)
                .build();
    }
}
