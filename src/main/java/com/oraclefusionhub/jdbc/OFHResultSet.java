package com.oraclefusionhub.jdbc;

import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

public class OFHResultSet implements ResultSet {
	private final List<String> header;
	private final List<List<String>> rows;
	private final Map<String, Integer> columnIndexByName;
	private int currentRowIndex = -1;
	private List<String> record = null;
	private boolean closed = false;
	private boolean lastWasNull = false;

	public OFHResultSet(Iterable<CSVRecord> s) {
		Iterator<CSVRecord> iterator = s.iterator();
		this.header = new ArrayList<>();
		this.rows = new ArrayList<>();
		if (iterator.hasNext()) {
			CSVRecord headerRecord = iterator.next();
			for (String value : headerRecord) {
				this.header.add(value);
			}

			while (iterator.hasNext()) {
				CSVRecord csvRecord = iterator.next();
				List<String> row = new ArrayList<>();
				for (String value : csvRecord) {
					row.add(value);
				}
				this.rows.add(row);
			}
		}

		this.columnIndexByName = createColumnIndex(this.header);
	}

	public OFHResultSet(List<String> header, List<List<String>> rows) {
		this.header = new ArrayList<>(header);
		this.rows = new ArrayList<>();
		for (List<String> row : rows) {
			this.rows.add(new ArrayList<>(row));
		}
		this.columnIndexByName = createColumnIndex(this.header);
	}
	@Override
	public boolean next() throws SQLException {
		ensureOpen();
		if (currentRowIndex + 1 < rows.size()) {
			currentRowIndex++;
			record = rows.get(currentRowIndex);
			return true;
		}
		record = null;
		currentRowIndex = rows.size();
		lastWasNull = false;
		return false;
	}

	@Override
	public void close() throws SQLException {
		closed = true;
		record = null;
	}

	@Override
	public boolean wasNull() throws SQLException {
		ensureOpen();
		return lastWasNull;
	}

	@Override
	public String getString(int i) throws SQLException {
		return getValue(i);
	}

	@Override
	public boolean getBoolean(int i) throws SQLException {
		return false;
	}

	@Override
	public byte getByte(int i) throws SQLException {
		return 0;
	}

	@Override
	public short getShort(int i) throws SQLException {
		return 0;
	}

	@Override
	public int getInt(int i) throws SQLException {
		return 0;
	}

	@Override
	public long getLong(int i) throws SQLException {
		return 0;
	}

	@Override
	public float getFloat(int i) throws SQLException {
		return 0;
	}

	@Override
	public double getDouble(int i) throws SQLException {
		return 0;
	}

	@Override
	public BigDecimal getBigDecimal(int i, int i1) throws SQLException {
		return null;
	}

	@Override
	public byte[] getBytes(int i) throws SQLException {
		return new byte[0];
	}

	@Override
	public Date getDate(int i) throws SQLException {
		return null;
	}

	@Override
	public Time getTime(int i) throws SQLException {
		return null;
	}

	@Override
	public Timestamp getTimestamp(int i) throws SQLException {
		return null;
	}

	@Override
	public InputStream getAsciiStream(int i) throws SQLException {
		return null;
	}

	@Override
	public InputStream getUnicodeStream(int i) throws SQLException {
		return null;
	}

	@Override
	public InputStream getBinaryStream(int i) throws SQLException {
		return null;
	}

	@Override
	public String getString(String s) throws SQLException {
		return getString(findColumn(s));
	}

	@Override
	public boolean getBoolean(String s) throws SQLException {
		return false;
	}

	@Override
	public byte getByte(String s) throws SQLException {
		return 0;
	}

	@Override
	public short getShort(String s) throws SQLException {
		return 0;
	}

	@Override
	public int getInt(String s) throws SQLException {
		return 0;
	}

	@Override
	public long getLong(String s) throws SQLException {
		return 0;
	}

	@Override
	public float getFloat(String s) throws SQLException {
		return 0;
	}

	@Override
	public double getDouble(String s) throws SQLException {
		return 0;
	}

	@Override
	public BigDecimal getBigDecimal(String s, int i) throws SQLException {
		return null;
	}

	@Override
	public byte[] getBytes(String s) throws SQLException {
		return new byte[0];
	}

	@Override
	public Date getDate(String s) throws SQLException {
		return null;
	}

	@Override
	public Time getTime(String s) throws SQLException {
		return null;
	}

	@Override
	public Timestamp getTimestamp(String s) throws SQLException {
		return null;
	}

	@Override
	public InputStream getAsciiStream(String s) throws SQLException {
		return null;
	}

	@Override
	public InputStream getUnicodeStream(String s) throws SQLException {
		return null;
	}

	@Override
	public InputStream getBinaryStream(String s) throws SQLException {
		return null;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {

	}

	@Override
	public String getCursorName() throws SQLException {
		return null;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		ensureOpen();
		return new ResultSetMetaData() {
			@Override
			public int getColumnCount() throws SQLException {
				return header.size();
			}

			@Override
			public boolean isAutoIncrement(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isCaseSensitive(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isSearchable(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isCurrency(int column) throws SQLException {
				return false;
			}

			@Override
			public int isNullable(int column) throws SQLException {
				return 0;
			}

			@Override
			public boolean isSigned(int column) throws SQLException {
				return false;
			}

			@Override
			public int getColumnDisplaySize(int column) throws SQLException {
				return 0;
			}

			@Override
			public String getColumnLabel(int column) throws SQLException {
				validateColumnIndex(column);
				return header.get(column - 1);
			}

			@Override
			public String getColumnName(int column) throws SQLException {
				return getColumnLabel(column);
			}

			@Override
			public String getSchemaName(int column) throws SQLException {
				return null;
			}

			@Override
			public int getPrecision(int column) throws SQLException {
				return 0;
			}

			@Override
			public int getScale(int column) throws SQLException {
				return 0;
			}

			@Override
			public String getTableName(int column) throws SQLException {
				return null;
			}

			@Override
			public String getCatalogName(int column) throws SQLException {
				return null;
			}

			@Override
			public int getColumnType(int column) throws SQLException {
				validateColumnIndex(column);
				return Types.VARCHAR;
			}

			@Override
			public String getColumnTypeName(int column) throws SQLException {
				validateColumnIndex(column);
				return "VARCHAR";
			}

			@Override
			public boolean isReadOnly(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isWritable(int column) throws SQLException {
				return false;
			}

			@Override
			public boolean isDefinitelyWritable(int column) throws SQLException {
				return false;
			}

			@Override
			public String getColumnClassName(int column) throws SQLException {
				validateColumnIndex(column);
				return String.class.getName();
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}
		};
	}

	@Override
	public Object getObject(int i) throws SQLException {
		return getString(i);
	}

	@Override
	public Object getObject(String s) throws SQLException {
		return getString(s);
	}

	@Override
	public int findColumn(String s) throws SQLException {
		ensureOpen();
		Integer index = columnIndexByName.get(s);
		if (index == null) {
			throw new SQLException("Column not found: " + s);
		}
		return index + 1;
	}

	@Override
	public Reader getCharacterStream(int i) throws SQLException {
		return null;
	}

	@Override
	public Reader getCharacterStream(String s) throws SQLException {
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(int i) throws SQLException {
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(String s) throws SQLException {
		return null;
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		ensureOpen();
		return currentRowIndex < 0 && !rows.isEmpty();
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		ensureOpen();
		return currentRowIndex >= rows.size() && !rows.isEmpty();
	}

	@Override
	public boolean isFirst() throws SQLException {
		ensureOpen();
		return currentRowIndex == 0 && record != null;
	}

	@Override
	public boolean isLast() throws SQLException {
		ensureOpen();
		return currentRowIndex == rows.size() - 1 && record != null;
	}

	@Override
	public void beforeFirst() throws SQLException {
		ensureOpen();
		currentRowIndex = -1;
		record = null;
		lastWasNull = false;
	}

	@Override
	public void afterLast() throws SQLException {
		ensureOpen();
		currentRowIndex = rows.size();
		record = null;
		lastWasNull = false;
	}

	@Override
	public boolean first() throws SQLException {
		ensureOpen();
		if (rows.isEmpty()) {
			record = null;
			currentRowIndex = -1;
			return false;
		}
		currentRowIndex = 0;
		record = rows.get(currentRowIndex);
		lastWasNull = false;
		return true;
	}

	@Override
	public boolean last() throws SQLException {
		ensureOpen();
		if (rows.isEmpty()) {
			record = null;
			currentRowIndex = -1;
			return false;
		}
		currentRowIndex = rows.size() - 1;
		record = rows.get(currentRowIndex);
		lastWasNull = false;
		return true;
	}

	@Override
	public int getRow() throws SQLException {
		ensureOpen();
		return record == null ? 0 : currentRowIndex + 1;
	}

	@Override
	public boolean absolute(int i) throws SQLException {
		return false;
	}

	@Override
	public boolean relative(int i) throws SQLException {
		return false;
	}

	@Override
	public boolean previous() throws SQLException {
		return false;
	}

	private Map<String, Integer> createColumnIndex(List<String> columns) {
		Map<String, Integer> indexByName = new LinkedHashMap<>();
		for (int i = 0; i < columns.size(); i++) {
			indexByName.put(columns.get(i), i);
		}
		return indexByName;
	}

	private String getValue(int columnIndex) throws SQLException {
		ensureOpen();
		if (record == null) {
			throw new SQLException("Cursor not positioned on a row");
		}

		validateColumnIndex(columnIndex);
		int zeroBasedIndex = columnIndex - 1;

		if (zeroBasedIndex >= record.size()) {
			lastWasNull = true;
			return null;
		}

		String value = record.get(zeroBasedIndex);
		lastWasNull = value == null;
		return value;
	}

	private void validateColumnIndex(int columnIndex) throws SQLException {
		if (columnIndex < 1 || columnIndex > header.size()) {
			throw new SQLException("Invalid column index: " + columnIndex);
		}
	}

	private void ensureOpen() throws SQLException {
		if (closed) {
			throw new SQLException("ResultSet is closed");
		}
	}

	@Override
	public void setFetchDirection(int i) throws SQLException {

	}

	@Override
	public int getFetchDirection() throws SQLException {
		return 0;
	}

	@Override
	public void setFetchSize(int i) throws SQLException {

	}

	@Override
	public int getFetchSize() throws SQLException {
		return 0;
	}

	@Override
	public int getType() throws SQLException {
		return 0;
	}

	@Override
	public int getConcurrency() throws SQLException {
		return 0;
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	@Override
	public void updateNull(int i) throws SQLException {

	}

	@Override
	public void updateBoolean(int i, boolean b) throws SQLException {

	}

	@Override
	public void updateByte(int i, byte b) throws SQLException {

	}

	@Override
	public void updateShort(int i, short i1) throws SQLException {

	}

	@Override
	public void updateInt(int i, int i1) throws SQLException {

	}

	@Override
	public void updateLong(int i, long l) throws SQLException {

	}

	@Override
	public void updateFloat(int i, float v) throws SQLException {

	}

	@Override
	public void updateDouble(int i, double v) throws SQLException {

	}

	@Override
	public void updateBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {

	}

	@Override
	public void updateString(int i, String s) throws SQLException {

	}

	@Override
	public void updateBytes(int i, byte[] bytes) throws SQLException {

	}

	@Override
	public void updateDate(int i, Date date) throws SQLException {

	}

	@Override
	public void updateTime(int i, Time time) throws SQLException {

	}

	@Override
	public void updateTimestamp(int i, Timestamp timestamp) throws SQLException {

	}

	@Override
	public void updateAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {

	}

	@Override
	public void updateBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {

	}

	@Override
	public void updateCharacterStream(int i, Reader reader, int i1) throws SQLException {

	}

	@Override
	public void updateObject(int i, Object o, int i1) throws SQLException {

	}

	@Override
	public void updateObject(int i, Object o) throws SQLException {

	}

	@Override
	public void updateNull(String s) throws SQLException {

	}

	@Override
	public void updateBoolean(String s, boolean b) throws SQLException {

	}

	@Override
	public void updateByte(String s, byte b) throws SQLException {

	}

	@Override
	public void updateShort(String s, short i) throws SQLException {

	}

	@Override
	public void updateInt(String s, int i) throws SQLException {

	}

	@Override
	public void updateLong(String s, long l) throws SQLException {

	}

	@Override
	public void updateFloat(String s, float v) throws SQLException {

	}

	@Override
	public void updateDouble(String s, double v) throws SQLException {

	}

	@Override
	public void updateBigDecimal(String s, BigDecimal bigDecimal) throws SQLException {

	}

	@Override
	public void updateString(String s, String s1) throws SQLException {

	}

	@Override
	public void updateBytes(String s, byte[] bytes) throws SQLException {

	}

	@Override
	public void updateDate(String s, Date date) throws SQLException {

	}

	@Override
	public void updateTime(String s, Time time) throws SQLException {

	}

	@Override
	public void updateTimestamp(String s, Timestamp timestamp) throws SQLException {

	}

	@Override
	public void updateAsciiStream(String s, InputStream inputStream, int i) throws SQLException {

	}

	@Override
	public void updateBinaryStream(String s, InputStream inputStream, int i) throws SQLException {

	}

	@Override
	public void updateCharacterStream(String s, Reader reader, int i) throws SQLException {

	}

	@Override
	public void updateObject(String s, Object o, int i) throws SQLException {

	}

	@Override
	public void updateObject(String s, Object o) throws SQLException {

	}

	@Override
	public void insertRow() throws SQLException {

	}

	@Override
	public void updateRow() throws SQLException {

	}

	@Override
	public void deleteRow() throws SQLException {

	}

	@Override
	public void refreshRow() throws SQLException {

	}

	@Override
	public void cancelRowUpdates() throws SQLException {

	}

	@Override
	public void moveToInsertRow() throws SQLException {

	}

	@Override
	public void moveToCurrentRow() throws SQLException {

	}

	@Override
	public Statement getStatement() throws SQLException {
		return null;
	}

	@Override
	public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
		return null;
	}

	@Override
	public Ref getRef(int i) throws SQLException {
		return null;
	}

	@Override
	public Blob getBlob(int i) throws SQLException {
		return null;
	}

	@Override
	public Clob getClob(int i) throws SQLException {
		return null;
	}

	@Override
	public Array getArray(int i) throws SQLException {
		return null;
	}

	@Override
	public Object getObject(String s, Map<String, Class<?>> map) throws SQLException {
		return null;
	}

	@Override
	public Ref getRef(String s) throws SQLException {
		return null;
	}

	@Override
	public Blob getBlob(String s) throws SQLException {
		return null;
	}

	@Override
	public Clob getClob(String s) throws SQLException {
		return null;
	}

	@Override
	public Array getArray(String s) throws SQLException {
		return null;
	}

	@Override
	public Date getDate(int i, Calendar calendar) throws SQLException {
		return null;
	}

	@Override
	public Date getDate(String s, Calendar calendar) throws SQLException {
		return null;
	}

	@Override
	public Time getTime(int i, Calendar calendar) throws SQLException {
		return null;
	}

	@Override
	public Time getTime(String s, Calendar calendar) throws SQLException {
		return null;
	}

	@Override
	public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {
		return null;
	}

	@Override
	public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {
		return null;
	}

	@Override
	public URL getURL(int i) throws SQLException {
		return null;
	}

	@Override
	public URL getURL(String s) throws SQLException {
		return null;
	}

	@Override
	public void updateRef(int i, Ref ref) throws SQLException {

	}

	@Override
	public void updateRef(String s, Ref ref) throws SQLException {

	}

	@Override
	public void updateBlob(int i, Blob blob) throws SQLException {

	}

	@Override
	public void updateBlob(String s, Blob blob) throws SQLException {

	}

	@Override
	public void updateClob(int i, Clob clob) throws SQLException {

	}

	@Override
	public void updateClob(String s, Clob clob) throws SQLException {

	}

	@Override
	public void updateArray(int i, Array array) throws SQLException {

	}

	@Override
	public void updateArray(String s, Array array) throws SQLException {

	}

	@Override
	public RowId getRowId(int i) throws SQLException {
		return null;
	}

	@Override
	public RowId getRowId(String s) throws SQLException {
		return null;
	}

	@Override
	public void updateRowId(int i, RowId rowId) throws SQLException {

	}

	@Override
	public void updateRowId(String s, RowId rowId) throws SQLException {

	}

	@Override
	public int getHoldability() throws SQLException {
		return ResultSet.HOLD_CURSORS_OVER_COMMIT;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	@Override
	public void updateNString(int i, String s) throws SQLException {

	}

	@Override
	public void updateNString(String s, String s1) throws SQLException {

	}

	@Override
	public void updateNClob(int i, NClob nClob) throws SQLException {

	}

	@Override
	public void updateNClob(String s, NClob nClob) throws SQLException {

	}

	@Override
	public NClob getNClob(int i) throws SQLException {
		return null;
	}

	@Override
	public NClob getNClob(String s) throws SQLException {
		return null;
	}

	@Override
	public SQLXML getSQLXML(int i) throws SQLException {
		return null;
	}

	@Override
	public SQLXML getSQLXML(String s) throws SQLException {
		return null;
	}

	@Override
	public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {

	}

	@Override
	public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {

	}

	@Override
	public String getNString(int i) throws SQLException {
		return null;
	}

	@Override
	public String getNString(String s) throws SQLException {
		return null;
	}

	@Override
	public Reader getNCharacterStream(int i) throws SQLException {
		return null;
	}

	@Override
	public Reader getNCharacterStream(String s) throws SQLException {
		return null;
	}

	@Override
	public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateNCharacterStream(String s, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateAsciiStream(int i, InputStream inputStream, long l) throws SQLException {

	}

	@Override
	public void updateBinaryStream(int i, InputStream inputStream, long l) throws SQLException {

	}

	@Override
	public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateAsciiStream(String s, InputStream inputStream, long l) throws SQLException {

	}

	@Override
	public void updateBinaryStream(String s, InputStream inputStream, long l) throws SQLException {

	}

	@Override
	public void updateCharacterStream(String s, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateBlob(int i, InputStream inputStream, long l) throws SQLException {

	}

	@Override
	public void updateBlob(String s, InputStream inputStream, long l) throws SQLException {

	}

	@Override
	public void updateClob(int i, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateClob(String s, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateNClob(int i, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateNClob(String s, Reader reader, long l) throws SQLException {

	}

	@Override
	public void updateNCharacterStream(int i, Reader reader) throws SQLException {

	}

	@Override
	public void updateNCharacterStream(String s, Reader reader) throws SQLException {

	}

	@Override
	public void updateAsciiStream(int i, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateBinaryStream(int i, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateCharacterStream(int i, Reader reader) throws SQLException {

	}

	@Override
	public void updateAsciiStream(String s, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateBinaryStream(String s, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateCharacterStream(String s, Reader reader) throws SQLException {

	}

	@Override
	public void updateBlob(int i, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateBlob(String s, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateClob(int i, Reader reader) throws SQLException {

	}

	@Override
	public void updateClob(String s, Reader reader) throws SQLException {

	}

	@Override
	public void updateNClob(int i, Reader reader) throws SQLException {

	}

	@Override
	public void updateNClob(String s, Reader reader) throws SQLException {

	}

	@Override
	public <T> T getObject(int i, Class<T> aClass) throws SQLException {
		return aClass.cast(getObject(i));
	}

	@Override
	public <T> T getObject(String s, Class<T> aClass) throws SQLException {
		return aClass.cast(getObject(s));
	}

	@Override
	public <T> T unwrap(Class<T> aClass) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> aClass) throws SQLException {
		return false;
	}
}
