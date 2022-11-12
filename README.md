# Oracle Cloud SQL Driver

JDBC driver to query Oracle Cloud database

## Installation

- Download the zip file [here](https://github.com/satyapadala/oracle-cloud-sql-driver/releases/download/v0.0.2/releaseArtifcats.zip)
- Login to Oracle BI as BI Administrator and upload (unarchive) the file `OracleBICatalog/OracleCloudSQLQuery.catalog` to `/Custom` folder
- Change the data source for the generated data model to your respective cloud data source
- Download and install Db Visualizer [here](https://www.dbvis.com/download/)
- Open Db Visualizer and follow the below steps:
    - Go to `Tools` --> `Driver Manager` and click `+` button and select `custom` driver. 
    - In the `Name`, enter `Oracle Cloud SQL Driver`
    - In `URL Format`, enter `jdbc:ofh://${Server}`
    - Click on `+` button in `Driver artifacts and jar files` section and click on `Add files`
    - File window opens up and select `OFHSqlDriver_jar` folder that you downloaded earlier.
    - Close the `Driver Manager` window.
    - In the main window of Db Visualizer, click on `+` button to `Create new database connection`
    - Select driver name as `Oracle Cloud SQL Driver`
    - In `Database Server` field, enter your Oracle Cloud URL. Example: `https://xxx.xxx.us2.oracle.com`
    - In `Authentication` tab, enter valid BI User username in `Database UserId` and enter valid BI User password in `Database Password`.
    - Click on `Connect`. If this doesn't give you any error, a successful connection has been made.
    - You can now click on `SQL Commander` --> `New SQL Commander` window and happy querying!!

# Caution
Please do not query for large number of result sets. This driver has not been tested for performance. The intended purpose of this driver is to help developers query cloud schema to save some productive hours.

# Contact
Please reach out to `satya@oraclefusionhub.com` for any queries.
