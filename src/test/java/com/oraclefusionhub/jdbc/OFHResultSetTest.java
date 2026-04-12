package com.oraclefusionhub.jdbc;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

public class OFHResultSetTest {

    @Test
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

        rs.close();
    }

    @Test
    public void testResultSetFromRows() throws SQLException {
        ResultSet rs = new OFHResultSet(
                Arrays.asList("SYSDATE"),
                Collections.singletonList(Collections.singletonList("2026-04-12"))
        );

        assertEquals("SYSDATE", rs.getMetaData().getColumnLabel(1));
        assertEquals(1, rs.findColumn("SYSDATE"));

        rs.next();
        assertEquals("2026-04-12", rs.getString(1));
        assertEquals("2026-04-12", rs.getString("SYSDATE"));
        assertEquals("2026-04-12", rs.getObject(1));
    }

    @Test
    public void testEmptyResultSet() throws SQLException {
        ResultSet rs = new OFHResultSet(Collections.emptyList(), Collections.emptyList());

        assertEquals(0, rs.getMetaData().getColumnCount());
        assertFalse(rs.next());
        assertFalse(rs.first());
        assertFalse(rs.last());
        assertFalse(rs.isBeforeFirst());
        assertFalse(rs.isAfterLast());
    }

    @Test
    public void testCursorNavigationAndWasNull() throws SQLException {
        ResultSet rs = new OFHResultSet(
                Arrays.asList("A", "B"),
                Collections.singletonList(Collections.singletonList("a1"))
        );

        assertTrue(rs.next());
        assertEquals("a1", rs.getString(1));
        assertFalse(rs.wasNull());
        assertEquals(null, rs.getString(2));
        assertTrue(rs.wasNull());
        assertTrue(rs.isFirst());
        assertTrue(rs.isLast());
        assertEquals(1, rs.getRow());
        assertTrue(rs.first());
        rs.afterLast();
        assertTrue(rs.isAfterLast());
        rs.beforeFirst();
        assertTrue(rs.isBeforeFirst());
    }

    @Test
    public void testMetadataTypeDefaults() throws SQLException {
        ResultSet rs = new OFHResultSet(
                Collections.singletonList("A"),
                Collections.singletonList(Collections.singletonList("a1"))
        );

        assertEquals(Types.VARCHAR, rs.getMetaData().getColumnType(1));
        assertEquals("VARCHAR", rs.getMetaData().getColumnTypeName(1));
        assertEquals(String.class.getName(), rs.getMetaData().getColumnClassName(1));
    }

}
