package com.oraclefusionhub.jdbc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class OFHDriverTest {

	private static Driver driver;

	@Rule
	public WireMockRule mockXMLPServer = new WireMockRule(8089);

	@BeforeClass
	public static void setUpClass() throws Exception {
		driver = new OFHDriver();

		DriverManager.registerDriver(driver);

	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		DriverManager.deregisterDriver(driver);
	}

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void test() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestResult.xml")));

		Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");

		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery("select * from ofh_user where rownum <= 100");

		assertTrue("resultset should contain data", resultSet.next());

		connection.close();

		assertTrue("connection should be closed", connection.isClosed());
	}

	@Test
	public void testSafetyGuardEnabled_RefusesBlindQuery() {
		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser", "testPassword");
			 Statement statement = connection.createStatement()) {
			statement.executeQuery("select * from ofh_user"); // Blind query with no limit
			fail("Expected SQLException from SafetyGuard");
		} catch (SQLException e) {
			assertTrue("Should throw Safety Guard Exception", e.getMessage().contains("Safety Guard: Refusing to execute a massive blind query"));
		}
	}

	@Test
	public void testSafetyGuardDisabled_AllowsBlindQuery() throws SQLException {
		
		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestResult.xml")));

		Properties props = new Properties();
		props.setProperty("user", "testUser");
		props.setProperty("password", "testPassword");
		props.setProperty("safetyGuard", "false");

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", props);
			 Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("select * from ofh_user"); // Blind query but safety is OFF!
			assertTrue("resultset should contain data", resultSet.next());
		}
	}

	@Test
	public void testSoapFaultMessage() {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestFault.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement()) {
			statement.executeQuery("select sysdate from dual");
			fail("Expected SQLException");
		} catch (SQLException e) {
			assertEquals("Parsing Error: SOAP Fault: Invalid column type XMLTYPE for report output", e.getMessage());
		}
	}

	@Test
	public void testXmlRowsetResult() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("select sysdate from dual")) {
			assertTrue("resultset should contain xml-derived row", resultSet.next());
			assertEquals("SYSDATE", resultSet.getMetaData().getColumnLabel(1));
			assertEquals("2026-04-12", resultSet.getString(1));
			assertEquals("2026-04-12", resultSet.getString("SYSDATE"));
		}
	}

	@Test
	public void testWrappedXmlRowsetResult() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestWrappedXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("select sysdate from dual")) {
			assertTrue("resultset should contain wrapped xml-derived row", resultSet.next());
			assertEquals("SYSDATE", resultSet.getMetaData().getColumnLabel(1));
			assertEquals("2026-04-12", resultSet.getString(1));
		}
	}

	@Test
	public void testWrappedXmlRowsetRespectsMaxRows() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestWrappedXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword")) {
			Statement fullStatement = connection.createStatement();
			fullStatement.setMaxRows(0);
			ResultSet fullResultSet = fullStatement.executeQuery("select sysdate from dual");
			assertTrue(fullResultSet.next());
			assertEquals(false, fullResultSet.next());
			fullResultSet.close();
			fullStatement.close();

			Statement limitedStatement = connection.createStatement();
			limitedStatement.setMaxRows(1);
			ResultSet limitedResultSet = limitedStatement.executeQuery("select sysdate from dual");
			assertTrue(limitedResultSet.next());
			assertEquals(false, limitedResultSet.next());
			limitedResultSet.close();
			limitedStatement.close();
		}
	}

	@Test
	public void testEmptyWrappedXmlResult() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestEmptyWrappedResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("select sysdate from dual")) {
			assertEquals(0, resultSet.getMetaData().getColumnCount());
			assertEquals(false, resultSet.next());
		}
	}

	@Test
	public void testFetchSizeStoresAndReturnsValue() throws SQLException {
		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement()) {
			statement.setFetchSize(200);
			assertEquals(200, statement.getFetchSize());
		}
	}

	@Test
	public void testFetchSizeRewritesBlindQueryAndPassesSafetyGuard() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestWrappedXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement()) {
			statement.setFetchSize(200);
			ResultSet resultSet = statement.executeQuery("select * from per_users");
			assertTrue("resultset should contain data", resultSet.next());

			mockXMLPServer.verify(WireMock.postRequestedFor(
					WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
					.withRequestBody(WireMock.containing("FETCH FIRST 200 ROWS ONLY")));
		}
	}

	@Test
	public void testFetchSizeDoesNotRewriteRownumQuery() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestWrappedXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement()) {
			statement.setFetchSize(200);
			statement.executeQuery("select * from per_users where rownum <= 50");

			mockXMLPServer.verify(WireMock.postRequestedFor(
					WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
					.withRequestBody(WireMock.notContaining("FETCH FIRST")));
		}
	}

	@Test
	public void testFetchSizeDoesNotRewriteDualQuery() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestWrappedXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement()) {
			statement.setFetchSize(200);
			statement.executeQuery("select sysdate from dual");

			mockXMLPServer.verify(WireMock.postRequestedFor(
					WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
					.withRequestBody(WireMock.notContaining("FETCH FIRST")));
		}
	}

	@Test
	public void testFetchSizeUsesMinOfFetchSizeAndMaxRows() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestWrappedXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement()) {
			statement.setFetchSize(200);
			statement.setMaxRows(50);
			statement.executeQuery("select * from per_users where 1=1");

			mockXMLPServer.verify(WireMock.postRequestedFor(
					WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
					.withRequestBody(WireMock.containing("FETCH FIRST 50 ROWS ONLY")));
		}
	}

	@Test
	public void testNoFetchSizeDoesNotRewrite() throws SQLException {

		mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
				.willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("E2ETestWrappedXmlResult.xml")));

		try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
				"testPassword");
				Statement statement = connection.createStatement()) {
			statement.executeQuery("select * from per_users where 1=1");

			mockXMLPServer.verify(WireMock.postRequestedFor(
					WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
					.withRequestBody(WireMock.notContaining("FETCH FIRST")));
		}
	}

    @Test
    public void testBareAmpersandInDataDoesNotBreakParsing() throws SQLException {

        mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBodyFile("E2ETestBareAmpersandResult.xml")));

        try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
                "testPassword");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select department_name, sysdate from my_table where 1=1")) {
            assertTrue("resultset should contain row", resultSet.next());
            assertEquals("R&D", resultSet.getString("DEPARTMENT_NAME"));
            assertEquals("2026-04-12", resultSet.getString("SYSDATE"));
        }
    }

    @Test
    public void testOffsetPaginationAppliesOffsetOnReExecution() throws SQLException {

        mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBodyFile("E2ETestWrappedXmlResult.xml")));

        try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
                "testPassword")) {

            Statement stmt1 = connection.createStatement();
            stmt1.setFetchSize(100);
            ResultSet firstPage = stmt1.executeQuery("select * from per_users where 1=1");
            assertTrue(firstPage.next());
            assertFalse(firstPage.next());
            firstPage.close();
            stmt1.close();

            Statement stmt2 = connection.createStatement();
            stmt2.setFetchSize(100);
            stmt2.executeQuery("select * from per_users where 1=1");

            mockXMLPServer.verify(WireMock.postRequestedFor(
                    WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                    .withRequestBody(WireMock.containing("OFFSET 1 ROWS FETCH NEXT 100 ROWS ONLY")));
        }
    }

    @Test
    public void testOffsetPaginationAccumulatesOffsetAcrossPages() throws SQLException {

        mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBodyFile("E2ETestWrappedXmlResult.xml")));

        try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
                "testPassword")) {

            Statement stmt1 = connection.createStatement();
            stmt1.setFetchSize(100);
            ResultSet page1 = stmt1.executeQuery("select * from per_users where 1=1");
            assertTrue(page1.next());
            assertFalse(page1.next());
            page1.close();
            stmt1.close();

            Statement stmt2 = connection.createStatement();
            stmt2.setFetchSize(100);
            stmt2.executeQuery("select * from per_users where 1=1");
            mockXMLPServer.verify(WireMock.postRequestedFor(
                    WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                    .withRequestBody(WireMock.containing("OFFSET 1 ROWS FETCH NEXT 100 ROWS ONLY")));

            Statement stmt3 = connection.createStatement();
            stmt3.setFetchSize(100);
            ResultSet page3 = stmt3.executeQuery("select * from per_users where 1=1");
            assertTrue(page3.next());
            assertFalse(page3.next());
            page3.close();
            stmt3.close();

            Statement stmt4 = connection.createStatement();
            stmt4.setFetchSize(100);
            stmt4.executeQuery("select * from per_users where 1=1");
            mockXMLPServer.verify(WireMock.postRequestedFor(
                    WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                    .withRequestBody(WireMock.containing("OFFSET 1 ROWS FETCH NEXT 100 ROWS ONLY")));
        }
    }

    @Test
    public void testOffsetPaginationResetsOnDifferentQuery() throws SQLException {

        mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBodyFile("E2ETestWrappedXmlResult.xml")));

        try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
                "testPassword")) {

            Statement stmt1 = connection.createStatement();
            stmt1.setFetchSize(100);
            ResultSet page1 = stmt1.executeQuery("select * from per_users where 1=1");
            assertTrue(page1.next());
            page1.close();
            stmt1.close();

            Statement stmt2 = connection.createStatement();
            stmt2.setFetchSize(100);
            stmt2.executeQuery("select * from per_users where 1=1");
            mockXMLPServer.verify(WireMock.postRequestedFor(
                    WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                    .withRequestBody(WireMock.containing("OFFSET 1 ROWS FETCH NEXT 100 ROWS ONLY")));

            Statement stmt3 = connection.createStatement();
            stmt3.setFetchSize(100);
            stmt3.executeQuery("select * from per_all_people where 1=1");
            mockXMLPServer.verify(WireMock.postRequestedFor(
                    WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                    .withRequestBody(WireMock.containing("FETCH FIRST 100 ROWS ONLY")));
        }
    }

    @Test
    public void testOffsetPaginationNoOffsetOnFirstExecution() throws SQLException {

        mockXMLPServer.stubFor(WireMock.post(WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
                        .withBodyFile("E2ETestWrappedXmlResult.xml")));

        try (Connection connection = DriverManager.getConnection("jdbc:ofh://http://localhost:8089", "testUser",
                "testPassword");
                Statement statement = connection.createStatement()) {
            statement.setFetchSize(100);

            statement.executeQuery("select * from per_users where 1=1");

            mockXMLPServer.verify(WireMock.postRequestedFor(
                    WireMock.urlEqualTo("/xmlpserver/services/ExternalReportWSSService"))
                    .withRequestBody(WireMock.containing("FETCH FIRST 100 ROWS ONLY")));
        }
    }

}
