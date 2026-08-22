import com.oraclefusionhub.jdbc.OFHDriver;

module OFHSqlDriver {
	requires java.sql;
    requires commons.csv;
    requires org.jsoup;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    
    exports com.oraclefusionhub.jdbc.metadata;
    opens com.oraclefusionhub.jdbc.metadata to com.fasterxml.jackson.databind;
    
    provides java.sql.Driver with OFHDriver;
}