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
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.csv.CSVRecord;

public class OFHStatement implements Statement {
	private final HttpURLConnection httpURLConnection;
	private final String payload;
	private ResultSet rs;
	private final String reportPath;

	OFHStatement(HttpURLConnection connection, String reportPath) {
		this.httpURLConnection = connection;
		this.reportPath = reportPath;
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
		String query = encodeXML(s);
		Object[] params = new Object[] { query, this.reportPath };
		String finalPayload = MessageFormat.format(this.payload, params);

		HttpURLConnection conn = this.httpURLConnection;
		conn.setDoOutput(true);
		OutputStream os = null;
		int responseCode = 0;
		try {
			os = conn.getOutputStream();
			os.write(finalPayload.getBytes(StandardCharsets.UTF_8));
			os.flush();
			os.close();
			responseCode = conn.getResponseCode();
		} catch (IOException e) {
			System.out.println("error while getting outstream: " + e.getStackTrace());
		}

		StringBuffer response = new StringBuffer();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			try {
				getOutput(conn, response, responseCode);
			} catch (IOException e) {
				System.out.println("error while getting instream response: " + e.getStackTrace());
			}

		} else {
			StringBuffer errorResponse = new StringBuffer();
			String errorResponseReason = null;
			try {
				getOutput(conn, errorResponse, responseCode);
				System.out.println("Error Response: " + errorResponse.toString());

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				try {
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(new StringReader(errorResponse.toString())));
					errorResponseReason = doc.getElementsByTagName("env:Envelope").item(0).getChildNodes().item(1)
							.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getTextContent();

				} catch (ParserConfigurationException e) {
					throw new SQLException("Parsing Error: " + e.getMessage());
				} catch (IOException e) {
					throw new SQLException("Parsing Error: " + e.getMessage());
				} catch (SAXException e) {
					throw new SQLException("Parsing Error: " + e.getMessage());
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new SQLException("WebService Error: " + e.getMessage());
			}
			throw new SQLException("WebService Error: Response Code: " + responseCode + " - " + errorResponseReason);
		}
		conn.disconnect();

		String responseCsv = null;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(response.toString())));
			responseCsv = getResponseCSVString(doc);
		} catch (ParserConfigurationException e) {
			throw new SQLException("Parsing Error: " + e.getMessage());
		} catch (IOException e) {
			throw new SQLException("Parsing Error: " + e.getMessage());
		} catch (SAXException e) {
			throw new SQLException("Parsing Error: " + e.getMessage());
		}

		Iterable<CSVRecord> records;

		try {
			records = CSVFormat.DEFAULT.parse(new StringReader(responseCsv));
		} catch (IOException e) {
			throw new SQLException("CSV Parsing Error: " + e.getMessage());
		}
		return new OFHResultSet(records);

	}

	private String getResponseCSVString(Document doc) {
		String base64Content = getContentDataFromDoc(doc);

		byte[] decodedBytes = Base64.getDecoder().decode(base64Content);

		String responseCsv = new String(decodedBytes);

		return responseCsv;
	}

	private String getContentDataFromDoc(Document doc) {

		NodeList envelopeNodeList = doc.getElementsByTagName("env:Envelope");

		if (envelopeNodeList.getLength() == 0) {
			throw new RuntimeException("No Envelope found");
		}

		Node envelope = envelopeNodeList.item(0);

		NodeList envelopeChildNodeList = envelope.getChildNodes();

		if (envelopeChildNodeList.getLength() == 0) {
			throw new RuntimeException("No Envelope child nodes found");
		}

		Node body = null;

		for (Node node : iterable(envelopeChildNodeList)) {
			if (node.getNodeName().equals("env:Body")) {
				body = node;
				break;
			}
		}

		if (body == null) {
			throw new RuntimeException("No Body found");
		}

		if (body.getChildNodes().getLength() == 0) {
			throw new RuntimeException("No Body child nodes found");
		}

		Node runReportResponse = null;

		for (Node node : iterable(body.getChildNodes())) {
			if (node.getNodeName().equals("runReportResponse")) {
				runReportResponse = node;
				break;
			}
		}

		if (runReportResponse == null) {
			throw new RuntimeException("No runReportResponse found");
		}

		if (runReportResponse.getChildNodes().getLength() == 0) {
			throw new RuntimeException("No runReportResponse child nodes found");
		}

		Node runReportReturn = null;

		for (Node node : iterable(runReportResponse.getChildNodes())) {
			if (node.getNodeName().equals("runReportReturn")) {
				runReportReturn = node;
				break;
			}
		}

		if (runReportReturn == null) {
			throw new RuntimeException("No runReportReturn found");
		}

		if (runReportReturn.getChildNodes().getLength() == 0) {
			throw new RuntimeException("No runReportReturn child nodes found");
		}

		Node reportBytes = null;

		for (Node node : iterable(runReportReturn.getChildNodes())) {
			if (node.getNodeName().equals("reportBytes")) {
				reportBytes = node;
				break;
			}
		}

		if (reportBytes == null) {
			throw new RuntimeException("No reportBytes found");
		}

		String base64Content = reportBytes.getTextContent();

		return base64Content;
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
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}

		in.close();
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
		return 0;
	}

	@Override
	public void setMaxRows(int i) throws SQLException {

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
