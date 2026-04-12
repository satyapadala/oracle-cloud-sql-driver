# Oracle Cloud SQL Driver

A lightweight, robust JDBC driver designed explicitly to securely execute SQL queries against Oracle Cloud ERP / HCM environments without direct database-tier access. 

## 🚀 Key Features
https://github.com/user-attachments/assets/4afdc1b8-6ed8-4239-b795-92291a52ef03

* **Seamless JDBC Integration:** Run native SQL via any standard Java database querying tool. The driver seamlessly connects to your Oracle Cloud instance and effortlessly processes the complex data responses into standard JDBC `ResultSet` outputs.
* **Offline Schema Metastore:** Comes bundled with a comprehensive metadata snapshot mapping over 4,000+ Oracle Financials Tables, Views, Columns, Primary Keys, and Descriptions natively to the JDBC Driver.
* **IDE Autosuggestions:** Because of the bundled metastore, tools like DBeaver, DataGrip, or DbVisualizer will offer lightning-fast, highly accurate intellisense and schema autosuggestions—without ever burning an API call to Oracle.
* **Robust Error Handling:** Natively unpacks Oracle Cloud exceptions and HTTP errors, throwing clean, readable `SQLExceptions` that explicitly outline syntax errors or column type mismatches directly in your IDE.-

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
- Under the `Driver artifacts and jar files` section, click `Add files` and select the `OFHSqlDriver-1.0-SNAPSHOT-shaded.jar`.
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
- In the **Libraries** tab, click **Add File** and select the `OFHSqlDriver-1.0-SNAPSHOT-shaded.jar`.
- Click **OK** to save the driver.
- Click **New Database Connection**, search for your new `Oracle Cloud SQL Driver`, and click Next.
- **Host:** `https://xxx.xxx.us2.oracle.com` (Your Oracle Cloud environment URL).
- **Username / Password:** Your valid BI User credentials.
- Click **Finish** and open a new SQL Script to start querying!

---

## ⚠️ Caution & Limitations

This driver is specifically engineered to improve developer velocity, schema exploration, and lightweight diagnostic data extraction. 
* **It has not been tested for massive ETL performance or extreme result sets.** Attempting to download gigabytes of raw transaction data will result in JDBC memory or timeout exceptions.
* Please ensure you construct disciplined queries utilizing `WHERE` clauses appropriately. 

## 📞 Contact

Please reach out to `satya@oraclefusionhub.com` for inquiries, feature requests, or enterprise support.
