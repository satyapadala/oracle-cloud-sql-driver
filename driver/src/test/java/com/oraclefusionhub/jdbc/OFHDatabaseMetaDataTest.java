package com.oraclefusionhub.jdbc;

import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.util.Properties;

import static org.junit.Assert.*;

public class OFHDatabaseMetaDataTest {

    private OFHConnection connection;
    private OFHDatabaseMetaData metaData;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("user", "test");
        props.setProperty("password", "test");
        connection = new OFHConnection("http://localhost", props);
        metaData = (OFHDatabaseMetaData) connection.getMetaData();
    }

    @Test
    public void testMetaDataSetup() throws Exception {
        assertNotNull("Metadata should not be null", metaData);
        assertNotNull("getTables should not crash", metaData.getTables(null, null, "%", null));
        assertNotNull("getColumns should not crash", metaData.getColumns(null, null, "%", "%"));
        assertNotNull("getTableTypes should not crash", metaData.getTableTypes());
    }
}
