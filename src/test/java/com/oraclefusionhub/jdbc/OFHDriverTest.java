package com.oraclefusionhub.jdbc;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

		ResultSet resultSet = statement.executeQuery("select * from ofh_user");

		assertTrue("resultset should contain data", resultSet.next());

		connection.close();

		assertTrue("connection should be closed", connection.isClosed());
	}

}