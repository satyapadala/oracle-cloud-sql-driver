package com.oraclefusionhub.jdbc;

import junit.framework.TestCase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OFHResultSetTest extends TestCase {

    public void testResultSet() throws IOException, SQLException {
        Reader in = new StringReader("a,b\na1,b1\na2,\"b2 and b2 something\"");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);

        ResultSet rs = new OFHResultSet(records);

        assertEquals(2, rs.getMetaData().getColumnCount());
        assertEquals("a", rs.getMetaData().getColumnLabel(1));
        assertEquals("b", rs.getMetaData().getColumnLabel(2));

        rs.next();
        assertEquals("a1", rs.getString(1));
        assertEquals("b1", rs.getString(2));

        rs.next();
        assertEquals("a2", rs.getString(1));
        assertEquals("b2 and b2 something", rs.getString(2));
    }

}