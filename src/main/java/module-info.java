import com.oraclefusionhub.jdbc.OFHDriver;

module OFHSqlDriver {
	requires java.sql;
    requires commons.csv;
    provides java.sql.Driver with OFHDriver;
}