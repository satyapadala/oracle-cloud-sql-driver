# Oracle Cloud ERP/HCM SQL Driver & MCP Server

A lightweight, robust JDBC driver and MCP (Model Context Protocol) server that securely executes SQL queries against Oracle Cloud ERP / HCM environments without direct database-tier access. Use the JDBC driver with any standard database IDE (DBeaver, DbVisualizer, DataGrip), or use the MCP server to give AI assistants like Claude direct, read-only query access to your Oracle Cloud data.

## 🚀 Key Features
https://github.com/user-attachments/assets/4afdc1b8-6ed8-4239-b795-92291a52ef03

### JDBC Driver
* **Seamless JDBC Integration:** Run native SQL via any standard Java database querying tool. The driver seamlessly connects to your Oracle Cloud instance and effortlessly processes the complex data responses into standard JDBC `ResultSet` outputs.
* **Offline Schema Metastore:** Comes bundled with a comprehensive metadata snapshot mapping over 4,000+ Oracle Financials Tables, Views, Columns, Primary Keys, and Descriptions natively to the JDBC Driver.
* **IDE Autosuggestions:** Because of the bundled metastore, tools like DBeaver, DataGrip, or DbVisualizer will offer lightning-fast, highly accurate intellisense and schema autosuggestions—without ever burning an API call to Oracle.
* **Robust Error Handling:** Natively unpacks Oracle Cloud exceptions and HTTP errors, throwing clean, readable `SQLExceptions` that explicitly outline syntax errors or column type mismatches directly in your IDE.

### MCP Server
* **AI Assistant Integration:** Exposes the driver's query capabilities over the Model Context Protocol (MCP) so AI assistants like Claude can run read-only SQL queries against your Oracle Cloud ERP/HCM data.
* **Schema-Aware Tools:** AI assistants can introspect table schemas and execute SELECT queries through two MCP tools: `get_table_schema` and `execute_sql`.
* **Safety Guard Built-In:** The same safety guard that protects the JDBC driver applies to MCP queries, preventing blind full-table scans.

## 🛠️ Installation & Setup

### 1. Oracle BI Server Configuration
- Download the bundled release zip file from the [Releases Page](https://github.com/satyapadala/oracle-cloud-sql-driver/releases).
- Login to Oracle BI as a BI Administrator. 
- Upload and unarchive the packaged `OracleBICatalog/OracleCloudSQLQuery.catalog` directly into your `/Custom` folder.
- Ensure you change the data source assigned to the generated data model to point to your respective Cloud data source.

### 2. Connect Your IDE

#### Option A: DbVisualizer
- Download and install [DbVisualizer](https://www.dbvis.com/download/).
- Go to `Tools` -> `Driver Manager`, click the `+` button and create a new **Custom Driver**.
- **Name:** `Oracle Cloud SQL Driver`
- **URL Format:** `jdbc:ofh://${Server}`
- Under the `Driver artifacts and jar files` section, click `Add files` and select the `OFHSqlDriver-driver-1.0-SNAPSHOT-shaded.jar` from the `OFHSqlDriver_jar` folder in the release zip.
- Close the Driver Manager and create a new database connection:
  - **Database Server:** `https://xxx.xxx.us2.oracle.com` (Your Oracle Cloud environment URL).
  - **Authentication:** Use a valid BI User username and password.
- Click **Connect** and open your SQL Commander to begin querying immediately!

#### Option B: DBeaver
- Download and install [DBeaver](https://dbeaver.io/download/).
- Go to `Database` -> `Driver Manager` and click **New**.
- In the **Settings** tab:
  - **Driver Name:** `Oracle Cloud SQL Driver`
  - **Class Name:** `com.oraclefusionhub.jdbc.OFHDriver`
  - **URL Template:** `jdbc:ofh://{host}`
- In the **Libraries** tab, click **Add File** and select the `OFHSqlDriver-driver-1.0-SNAPSHOT-shaded.jar` from the `OFHSqlDriver_jar` folder in the release zip.
- Click **OK** to save the driver.
- Click **New Database Connection**, search for your new `Oracle Cloud SQL Driver`, and click Next.
- **Host:** `https://xxx.xxx.us2.oracle.com` (Your Oracle Cloud environment URL).
- **Username / Password:** Your valid BI User credentials.
- Click **Finish** and open a new SQL Script to start querying!

#### Option C: MCP Server (AI Assistants like Claude)
- Download the release zip from the [Releases Page](https://github.com/satyapadala/oracle-cloud-sql-driver/releases) and extract it.
- Locate `OFHMcpServer-1.0-SNAPSHOT-shaded.jar` in the `OFHMcpServer_jar` folder.
- Configure your AI assistant's MCP client (e.g., Claude Desktop's `claude_desktop_config.json`) with a stdio server entry:

```json
{
  "mcpServers": {
    "oracle-cloud-erp": {
      "command": "java",
      "args": ["-jar", "/path/to/OFHMcpServer-1.0-SNAPSHOT-shaded.jar"],
      "env": {
        "JDBC_URL": "jdbc:ofh://https://xxx.xxx.us2.oracle.com",
        "JDBC_USER": "your_bi_username",
        "JDBC_PASSWORD": "your_bi_password",
        "JDBC_SAFETY_GUARD": "true",
        "JDBC_DEBUG": "false"
      }
    }
  }
}
```

- Restart your AI assistant. It will now have two tools available:
  - **`execute_sql`** — Run read-only SELECT or WITH (CTE) queries. Results are returned as JSON (max 100 rows).
  - **`get_table_schema`** — Fetch column names, types, nullability, comments, and primary keys for any table or view using the bundled offline metastore (no API calls to Oracle).
- Both tools are protected by the driver's safety guard, which rejects queries without `WHERE` clauses or row limiters.

---

## ⚙️ Configuration Properties

When configuring your connection, the driver exposes several custom properties that can be adjusted within your IDE's "Driver Properties" or "Connection Properties" tab:

| Property | Default Value | Description |
| :--- | :--- | :--- |
| **`safetyGuard`** | `true` | **Recommended:** Blocks the execution of massive "blind" SQL queries that do not contain a `WHERE` clause, `ROWNUM` limiter, or `FETCH FIRST` statement. This prevents accidental memory exhaustion of the Oracle Cloud environment and your local JVM. Set to `false` to bypass. **Note:** If `fetchSize` is set (e.g., via DBeaver's result set fetch size), the driver automatically injects a `FETCH FIRST N ROWS ONLY` clause, which satisfies the safety guard. |
| **`debug`** | `false` | Enables verbose diagnostic logging in the IDE console output. Useful for troubleshooting connection, parser, and schema extraction issues. |
| **`reportPath`** | `/Custom/OracleCloudSQLQuery/SQLQuery.xdo` | The absolute path to the uploaded Oracle BI Publisher Custom Report Data Model. |

### Fetch Size & Server-Side Row Limiting

The driver honors the JDBC `Statement.setFetchSize(n)` hint (commonly set by tools like DBeaver via the "Result Set Fetch Size" setting). When `fetchSize` is set to a positive value:

- The driver wraps the query as `SELECT * FROM (<original_sql>) FETCH FIRST n ROWS ONLY` before sending it to Oracle Cloud, reducing network transfer and memory usage.
- Queries that already contain `ROWNUM` or `FETCH FIRST` are **not** double-wrapped — the user's explicit limit is respected.
- Trivial `FROM DUAL` queries are skipped (they return at most 1 row).
- If both `fetchSize` and `maxRows` are set, the smaller value is used.

---

## ⚠️ Caution & Limitations

This driver is specifically engineered to improve developer velocity, schema exploration, and lightweight diagnostic data extraction. 
* **It has not been tested for massive ETL performance or extreme result sets.** Attempting to download gigabytes of raw transaction data will result in JDBC memory or timeout exceptions.
* Please ensure you construct disciplined queries utilizing `WHERE` clauses appropriately. 

## 📞 Contact

Please reach out to `satya@oraclefusionhub.com` for inquiries, feature requests, or enterprise support.
