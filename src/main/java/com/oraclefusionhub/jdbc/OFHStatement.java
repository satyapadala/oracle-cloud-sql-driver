package com.oraclefusionhub.jdbc;

import org.apache.commons.csv.CSVFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVRecord;

public class OFHStatement implements Statement {
	private final HttpURLConnection httpURLConnection;
	private final String payload;
	private final boolean debugEnabled;
	private final boolean safetyGuardEnabled;
	private ResultSet rs;
	private final String reportPath;
	private int maxRows = 0;

	OFHStatement(HttpURLConnection connection, String reportPath, boolean debugEnabled, boolean safetyGuardEnabled) {
		this.httpURLConnection = connection;
		this.reportPath = reportPath;
		this.debugEnabled = debugEnabled;
		this.safetyGuardEnabled = safetyGuardEnabled;
		this.payload = "<soap:Envelope xmlns:soap= \"http://www.w3.org/2003/05/soap-envelope\" xmlns:pub= \"http://xmlns.oracle.com/oxp/service/PublicReportService\">\n"
				+
				"    <soap:Body>\n" +
				"        <pub:runReport>\n" +
				"            <pub:reportRequest>\n" +
				"                <pub:parameterNameValues>\n" +
				"                    <pub:item>\n" +
				"                        <pub:name>p_query</pub:name>\n" +
				"                        <pub:values>\n" +
				"                            <pub:item>{0}</pub:item>\n" +
				"                        </pub:values>\n" +
				"                    </pub:item>\n" +
				"                </pub:parameterNameValues>\n" +
				"                <pub:reportAbsolutePath>{1}</pub:reportAbsolutePath>\n" +
				"                <pub:byPassCache>true</pub:byPassCache>\n" +
				"                <pub:sizeOfDataChunkDownload>-1</pub:sizeOfDataChunkDownload>\n" +
				"            </pub:reportRequest>\n" +
				"        </pub:runReport>\n" +
				"    </soap:Body>\n" +
				"</soap:Envelope>";

	}

	@Override
	public ResultSet executeQuery(String s) throws SQLException {
		checkQuerySafetyGuard(s);
		debug("OFHStatement.executeQuery SQL: " + s);
		String query = encodeXML(s);
		Object[] params = new Object[] { query, this.reportPath };
		String finalPayload = MessageFormat.format(this.payload, params);

		HttpURLConnection conn = this.httpURLConnection;
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept-Encoding", "gzip");
		OutputStream os = null;
		int responseCode = 0;
		try {
			os = conn.getOutputStream();
			os.write(finalPayload.getBytes(StandardCharsets.UTF_8));
			os.flush();
			os.close();
			responseCode = conn.getResponseCode();
		} catch (IOException e) {
			debug("error while getting outstream: " + e.getStackTrace());
		}

		StringBuffer response = new StringBuffer();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			try {
				getOutput(conn, response, responseCode);
				debug("OFHStatement HTTP 200 response length: " + response.length());
			} catch (IOException e) {
				debug("error while getting instream response: " + e.getStackTrace());
			}

		} else {
			StringBuffer errorResponse = new StringBuffer();
			String errorResponseReason = null;
			try {
				getOutput(conn, errorResponse, responseCode);
				debug("Error Response: " + errorResponse.toString());

				try {
					errorResponseReason = extractSoapFaultReasonStax(errorResponse.toString());
					if (errorResponseReason == null) {
					    errorResponseReason = "Unknown error: " + errorResponse.toString();
					}
				} catch (XMLStreamException e) {
					throw new SQLException("Parsing Error: " + e.getMessage());
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new SQLException("WebService Error: " + e.getMessage());
			}
			throw new SQLException("WebService Error: Response Code: " + responseCode + " - " + errorResponseReason);
		}
		conn.disconnect();

		String base64Content = null;
		try {
			base64Content = extractReportBytesStax(response.toString());
		} catch (XMLStreamException | RuntimeException e) {
			throw new SQLException("Parsing Error: " + e.getMessage(), e);
		}

		debug("OFHStatement reportBytes base64 length: " + base64Content.length());
		byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
		debug("OFHStatement decoded payload bytes: " + decodedBytes.length);

		String decodedContent = new String(decodedBytes, StandardCharsets.UTF_8);
		debug("OFHStatement decoded payload preview: " + preview(decodedContent));
		
		return buildResultSet(decodedContent);

	}

	private void checkQuerySafetyGuard(String sql) throws SQLException {
		if (!this.safetyGuardEnabled) {
			debug("Safety Guard is disabled. Proceeding with query.");
			return;
		}
		
		if (sql == null || sql.trim().isEmpty()) {
			return;
		}
		
		String upper = sql.toUpperCase();
		
		// Allow simple queries hitting DUAL
		if (upper.contains(" FROM DUAL") || upper.contains(" FROM SYS.DUAL")) {
			return;
		}

		if (!upper.contains("WHERE") && 
			!upper.contains("ROWNUM") && 
			!upper.contains("FETCH FIRST") && 
			!upper.contains("LIMIT")) {
			throw new SQLException("Safety Guard: Refusing to execute a massive blind query. " +
					"Please explicitly include a 'WHERE' clause, 'ROWNUM' limiter, or 'FETCH FIRST x ROWS ONLY' " +
					"to prevent Oracle Cloud extraction exhaustion.");
		}
	}

	private String extractSoapFaultReasonStax(String xmlContent) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlContent));
		StringBuilder faultReason = new StringBuilder();
		boolean inFault = false;
		boolean inReasonText = false;
		boolean inFaultString = false;

		while (reader.hasNext()) {
			int streamEvent = reader.next();
			if (streamEvent == XMLStreamConstants.START_ELEMENT) {
				String localName = reader.getLocalName();
				if ("Fault".equals(localName)) {
					inFault = true;
				} else if (inFault && "Text".equals(localName)) {
					inReasonText = true;
				} else if (inFault && "faultstring".equals(localName)) {
					inFaultString = true;
				}
			} else if (streamEvent == XMLStreamConstants.CHARACTERS) {
				if (inReasonText || inFaultString) {
					faultReason.append(reader.getText());
				}
			} else if (streamEvent == XMLStreamConstants.END_ELEMENT) {
				String localName = reader.getLocalName();
				if ("Text".equals(localName)) {
					inReasonText = false;
				} else if ("faultstring".equals(localName)) {
					inFaultString = false;
				} else if ("Fault".equals(localName)) {
					break;
				}
			}
		}
		String reason = faultReason.toString().trim();
		return reason.isEmpty() ? null : reason;
	}

	private String extractReportBytesStax(String xmlContent) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlContent));
		StringBuilder base64 = new StringBuilder();
		boolean inReportBytes = false;

		StringBuilder faultReason = null;
		boolean inFault = false;
		boolean inReasonText = false;
		boolean inFaultString = false;

		while (reader.hasNext()) {
			int streamEvent = reader.next();
			if (streamEvent == XMLStreamConstants.START_ELEMENT) {
				String localName = reader.getLocalName();
				if ("reportBytes".equals(localName)) {
					inReportBytes = true;
				} else if ("Fault".equals(localName)) {
					inFault = true;
					faultReason = new StringBuilder();
				} else if (inFault && "Text".equals(localName)) {
					inReasonText = true;
				} else if (inFault && "faultstring".equals(localName)) {
					inFaultString = true;
				}
			} else if (streamEvent == XMLStreamConstants.CHARACTERS) {
				if (inReportBytes) {
					base64.append(reader.getText());
				} else if (inReasonText || inFaultString) {
					faultReason.append(reader.getText());
				}
			} else if (streamEvent == XMLStreamConstants.END_ELEMENT) {
				String localName = reader.getLocalName();
				if ("reportBytes".equals(localName)) {
					break;
				} else if ("Text".equals(localName)) {
					inReasonText = false;
				} else if ("faultstring".equals(localName)) {
					inFaultString = false;
				} else if ("Fault".equals(localName)) {
					if (faultReason != null && faultReason.length() > 0) {
						throw new RuntimeException("SOAP Fault: " + faultReason.toString().trim());
					} else {
						throw new RuntimeException("SOAP Fault: Unknown SOAP fault");
					}
				}
			}
		}

		if (base64.length() == 0) {
			throw new RuntimeException("No reportBytes found or element is empty");
		}
		return base64.toString();
	}

	private ResultSet buildResultSet(String responseContent) throws SQLException {
		String trimmedContent = stripBom(responseContent == null ? "" : responseContent).trim();
		debug("OFHStatement trimmed payload preview: " + preview(trimmedContent));
		if (trimmedContent.startsWith("<")) {
			return buildXmlPayloadResultSet(trimmedContent);
		}

		return buildCsvResultSet(responseContent);
	}

	private ResultSet buildXmlPayloadResultSet(String xmlContent) throws SQLException {
		String embeddedRowset = extractEmbeddedRowsetFromPayload(xmlContent);
		if (embeddedRowset != null) {
			debug("OFHStatement parser branch: embedded XML ROWSET");
			return buildXmlResultSetStax(embeddedRowset);
		}

		if (!xmlContent.contains("<ROW>")) {
			debug("OFHStatement parser branch: empty XML result (no rows)");
			return emptyResultSet();
		}

		debug("OFHStatement parser branch: direct XML parsing");
		return buildXmlResultSetStax(xmlContent);
	}

	private String extractEmbeddedRowsetFromPayload(String xmlContent) {
		String normalizedContent = stripBom(xmlContent);
		if (normalizedContent == null || !normalizedContent.contains("<RESULT>")) {
			return null;
		}

		int resultStart = normalizedContent.indexOf("<RESULT>");
		int resultEnd = normalizedContent.indexOf("</RESULT>", resultStart);
		if (resultStart < 0 || resultEnd < 0) {
			return null;
		}

		String escapedRowset = normalizedContent.substring(resultStart + "<RESULT>".length(), resultEnd);
		String unescapedRowset = unescapeXml(escapedRowset).trim();
		if (unescapedRowset.startsWith("<ROWSET") && unescapedRowset.endsWith("</ROWSET>")) {
			debug("OFHStatement embedded ROWSET preview: " + preview(unescapedRowset));
			return unescapedRowset;
		}

		return null;
	}

	private ResultSet buildCsvResultSet(String responseContent) throws SQLException {
		try {
			Iterable<CSVRecord> parsedRecords = CSVFormat.DEFAULT.parse(new StringReader(responseContent));
			List<CSVRecord> records = new ArrayList<>();
			for (CSVRecord record : parsedRecords) {
				records.add(record);
			}

			if (records.isEmpty()) {
				debug("OFHStatement CSV parser found no records");
				return emptyResultSet();
			}

			CSVRecord header = records.get(0);
			debug("OFHStatement parser branch: CSV. Header columns: " + header.size() + " -> " + header);
			if (records.size() > 1) {
				CSVRecord firstRow = records.get(1);
				debug("OFHStatement CSV first row columns: " + firstRow.size() + " preview: " + preview(firstRow.toString()));
			} else {
				debug("OFHStatement CSV has header only");
			}

			if (maxRows > 0 && records.size() > maxRows + 1) {
				debug("OFHStatement applying maxRows to CSV result: " + maxRows);
				records = new ArrayList<>(records.subList(0, maxRows + 1));
			}

			return new OFHResultSet(records);
		} catch (IOException e) {
			throw new SQLException("CSV Parsing Error: " + e.getMessage());
		}
	}

	private ResultSet buildXmlResultSetStax(String xmlContent) throws SQLException {
		List<String> headers = new ArrayList<>();
		LinkedHashSet<String> headerNames = new LinkedHashSet<>();
		List<List<String>> rows = new ArrayList<>();

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlContent));
			
			boolean inRow = false;
			String currentColumnName = null;
			Map<String, String> rowValues = null;
			StringBuilder currentValue = null;

			while (reader.hasNext()) {
				int event = reader.next();
				
				if (event == XMLStreamConstants.START_ELEMENT) {
					String localName = reader.getLocalName();
					
					if ("ROW".equals(localName)) {
						inRow = true;
						rowValues = new LinkedHashMap<>();
					} else if (inRow) {
						currentColumnName = localName;
						currentValue = new StringBuilder();
						if (headerNames.add(currentColumnName)) {
							headers.add(currentColumnName);
						}
					}
				} else if (event == XMLStreamConstants.CHARACTERS) {
					if (inRow && currentColumnName != null) {
						currentValue.append(reader.getText());
					}
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					String localName = reader.getLocalName();
					
					if ("ROW".equals(localName)) {
						inRow = false;
						List<String> row = new ArrayList<>(headers.size());
						for (String header : headers) {
							row.add(rowValues.get(header));
						}
						rows.add(row);
						
						if (maxRows > 0 && rows.size() >= maxRows) {
							break;
						}
					} else if (inRow && currentColumnName != null && currentColumnName.equals(localName)) {
						rowValues.put(currentColumnName, currentValue.toString());
						currentColumnName = null;
						currentValue = null;
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new SQLException("XML Result Parsing Error: " + e.getMessage(), e);
		}

		debug("OFHStatement XML ROWSET parsed rows: " + rows.size() + " columns: " + headers.size() + " headers: " + headers);

		return new OFHResultSet(headers, rows);
	}

	private String unescapeXml(String value) {
		return value
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&apos;", "'")
				.replace("&amp;", "&");
	}

	private String stripBom(String value) {
		if (value == null) {
			return null;
		}
		if (!value.isEmpty() && value.charAt(0) == '\ufeff') {
			return value.substring(1);
		}
		return value;
	}



	private ResultSet emptyResultSet() {
		return new OFHResultSet(new ArrayList<>(), new ArrayList<>());
	}

	private String preview(String value) {
		if (value == null) {
			return "null";
		}

		String normalized = value
				.replace("\ufeff", "\\uFEFF")
				.replace("\r", "\\r")
				.replace("\n", "\\n");

		int maxLength = 240;
		if (normalized.length() <= maxLength) {
			return normalized;
		}

		return normalized.substring(0, maxLength) + "...";
	}

	private void debug(String message) {
		if (debugEnabled) {
			System.out.println(message);
		}
	}

	private String getContentDataFromDoc(Document doc) {

		Node envelope = findFirstChild(doc, "Envelope");

		if (envelope == null) {
			throw new RuntimeException("No Envelope found");
		}

		Node body = findFirstChild(envelope, "Body");

		if (body == null) {
			throw new RuntimeException("No Body found");
		}

		Node fault = findFirstChild(body, "Fault");

		if (fault != null) {
			String faultReason = extractSoapFaultReason(fault);
			throw new RuntimeException("SOAP Fault: " + faultReason);
		}

		Node runReportResponse = findFirstChild(body, "runReportResponse");

		if (runReportResponse == null) {
			throw new RuntimeException("No runReportResponse found");
		}

		Node runReportReturn = findFirstChild(runReportResponse, "runReportReturn");

		if (runReportReturn == null) {
			throw new RuntimeException("No runReportReturn found");
		}

		Node reportBytes = findFirstChild(runReportReturn, "reportBytes");

		if (reportBytes == null) {
			throw new RuntimeException("No reportBytes found");
		}

		return reportBytes.getTextContent();
	}

	private Node findFirstChild(Node parent, String nodeName) {
		NodeList childNodes = parent.getChildNodes();
		for (Node node : iterable(childNodes)) {
			if (matchesNodeName(node, nodeName)) {
				return node;
			}
		}
		return null;
	}

	private boolean matchesNodeName(Node node, String expectedName) {
		if (node == null) {
			return false;
		}

		String localName = node.getLocalName();
		if (expectedName.equals(localName)) {
			return true;
		}

		String nodeName = node.getNodeName();
		return expectedName.equals(nodeName) || nodeName.endsWith(":" + expectedName);
	}

	private String extractSoapFaultReason(Node fault) {
		Node reason = findFirstChild(fault, "Reason");
		if (reason != null) {
			Node text = findFirstChild(reason, "Text");
			if (text != null && text.getTextContent() != null && !text.getTextContent().isEmpty()) {
				return text.getTextContent();
			}
		}

		Node faultString = findFirstChild(fault, "faultstring");
		if (faultString != null && faultString.getTextContent() != null && !faultString.getTextContent().isEmpty()) {
			return faultString.getTextContent();
		}

		String fallback = fault.getTextContent();
		if (fallback == null || fallback.isEmpty()) {
			return "Unknown SOAP fault";
		}
		return fallback.trim();
	}

	public static Iterable<Node> iterable(final NodeList nodeList) {
		return () -> new Iterator<Node>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < nodeList.getLength();
			}

			@Override
			public Node next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return nodeList.item(index++);
			}
		};
	}

	private void getOutput(HttpURLConnection conn, StringBuffer response, int responseCode) throws IOException {
		InputStream is;

		if (responseCode >= 400) {
			is = conn.getErrorStream();
		} else {
			is = conn.getInputStream();
		}

		if (is == null) {
			return;
		}

		byte[] responseBytes = readAllBytes(is);
		byte[] decodedBytes = maybeDecompressResponse(conn, responseBytes);
		response.append(new String(decodedBytes, StandardCharsets.UTF_8));
		is.close();
	}

	private byte[] maybeDecompressResponse(HttpURLConnection conn, byte[] responseBytes) throws IOException {
		String contentEncoding = conn.getContentEncoding();
		boolean gzipEncoded = contentEncoding != null && contentEncoding.toLowerCase().contains("gzip");
		boolean gzipMagic = responseBytes.length >= 2
				&& (responseBytes[0] & 0xff) == 0x1f
				&& (responseBytes[1] & 0xff) == 0x8b;

		if (!gzipEncoded && !gzipMagic) {
			return responseBytes;
		}

		debug("OFHStatement decompressing gzip response body");
		try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(responseBytes))) {
			return readAllBytes(gzipInputStream);
		}
	}

	private byte[] readAllBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		return outputStream.toByteArray();
	}

	private static String encodeXML(CharSequence s) {
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int c = s.charAt(i);
			if (c >= 0xd800 && c <= 0xdbff && i + 1 < len) {
				c = ((c - 0xd7c0) << 10) | (s.charAt(++i) & 0x3ff); // UTF16 decode
			}
			if (c < 0x80) { // ASCII range: test most common case first
				if (c < 0x20 && (c != '\t' && c != '\r' && c != '\n')) {
					// Illegal XML character, even encoded. Skip or substitute
					sb.append("&#xfffd;"); // Unicode replacement character
				} else {
					switch (c) {
						case '&':
							sb.append("&amp;");
							break;
						case '>':
							sb.append("&gt;");
							break;
						case '<':
							sb.append("&lt;");
							break;
						// Uncomment next two if encoding for an XML attribute
						// case '\'' sb.append("&apos;"); break;
						// case '\"' sb.append("&quot;"); break;
						// Uncomment next three if you prefer, but not required
						// case '\n' sb.append("&#10;"); break;
						// case '\r' sb.append("&#13;"); break;
						// case '\t' sb.append("&#9;"); break;

						default:
							sb.append((char) c);
					}
				}
			} else if ((c >= 0xd800 && c <= 0xdfff) || c == 0xfffe || c == 0xffff) {
				// Illegal XML character, even encoded. Skip or substitute
				sb.append("&#xfffd;"); // Unicode replacement character
			} else {
				sb.append("&#x");
				sb.append(Integer.toHexString(c));
				sb.append(';');
			}
		}
		return sb.toString();
	}

	@Override
	public int executeUpdate(String s) throws SQLException {
		return 0;
	}

	@Override
	public void close() throws SQLException {
		if (this.httpURLConnection != null) {
			try {
				httpURLConnection.disconnect();
			} catch (Exception ignore) {
			}
		}
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return 0;
	}

	@Override
	public void setMaxFieldSize(int i) throws SQLException {

	}

	@Override
	public int getMaxRows() throws SQLException {
		return maxRows;
	}

	@Override
	public void setMaxRows(int i) throws SQLException {
		if (i < 0) {
			throw new SQLException("maxRows cannot be negative");
		}
		this.maxRows = i;
	}

	@Override
	public void setEscapeProcessing(boolean b) throws SQLException {

	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return 0;
	}

	@Override
	public void setQueryTimeout(int i) throws SQLException {

	}

	@Override
	public void cancel() throws SQLException {

	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {

	}

	@Override
	public void setCursorName(String s) throws SQLException {

	}

	@Override
	public boolean execute(String s) throws SQLException {
		ResultSet resultSet = executeQuery(s);
		this.rs = resultSet;
		return true;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return this.rs;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		return false;
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
	public int getResultSetConcurrency() throws SQLException {
		return 0;
	}

	@Override
	public int getResultSetType() throws SQLException {
		return 0;
	}

	@Override
	public void addBatch(String s) throws SQLException {

	}

	@Override
	public void clearBatch() throws SQLException {

	}

	@Override
	public int[] executeBatch() throws SQLException {
		return new int[0];
	}

	@Override
	public Connection getConnection() throws SQLException {
		return null;
	}

	@Override
	public boolean getMoreResults(int i) throws SQLException {
		return false;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return null;
	}

	@Override
	public int executeUpdate(String s, int i) throws SQLException {
		return 0;
	}

	@Override
	public int executeUpdate(String s, int[] ints) throws SQLException {
		return 0;
	}

	@Override
	public int executeUpdate(String s, String[] strings) throws SQLException {
		return 0;
	}

	@Override
	public boolean execute(String s, int i) throws SQLException {
		return false;
	}

	@Override
	public boolean execute(String s, int[] ints) throws SQLException {
		return false;
	}

	@Override
	public boolean execute(String s, String[] strings) throws SQLException {
		return false;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return 0;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public void setPoolable(boolean b) throws SQLException {

	}

	@Override
	public boolean isPoolable() throws SQLException {
		return false;
	}

	@Override
	public void closeOnCompletion() throws SQLException {

	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return false;
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
