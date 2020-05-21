package io.helidon.examples.quickstart.se;

//import java.nio.file.*;
import java.sql.*;
import java.io.*;
//import java.util.*;

import java.util.ArrayList;

//import io.helidon.webserver.ServerResponse;

/**
 * Executes SQL statements calls against a jdbc DB.
 *
 * @version 1.00 2019-03-26
 * @author Fernando Harris
 *
 */
public class DatabaseClient {

	public String getPaymentCodeFromSequence () throws SQLException,IOException {
		String query = "SELECT PAYMENT_SEQ.nextval FROM DUAL";
		long paymentCode;
		String paymentCd = "";
		try (Connection conn = getConnectionFromEnvVars(); Statement stat = conn.createStatement()) {
			try (ResultSet result = stat.executeQuery(query)) {
				while (result.next()){
					if ((paymentCode = result.getLong(1)) > 0 ){
						paymentCd += paymentCode;
					}
					else
						throw new Exception ("Payment Code Error: " + paymentCode);
				}
			}
			catch (Exception exRs){
				System.out.println("ERROR DatabaseClient getPaymentCode ResultSet-> " + exRs.getMessage());
				System.out.println("StackTrace: " + exRs.getStackTrace().toString());
			}
		}
		catch(Error error){
			System.out.println("ERROR DatabaseClient getPaymentCode -> " + error.getMessage());
			System.out.println("StackTrace: " + error.getStackTrace().toString());
		}
		return paymentCd;
	}

	public String insertPayment(String paymentCode, 
								String orderId, 
								String paymentTime, 
								String paymentMehtod,
								String serviceSurvey, 
								String originalPrice, 
								String totalPied, 
								String customerId) throws IOException {

		String dbresult = "";

		try {
			dbresult = executeInsertPayment(paymentCode, 
											orderId, 
											paymentTime, 
											paymentMehtod, 
											serviceSurvey, 
											originalPrice, 
											totalPied, 
											customerId);
		} catch (SQLException ex) {
			for (Throwable t : ex)
				t.printStackTrace();
		}

		return dbresult;
	}

	// public String[][] selectPayments(String paymentCode) throws IOException
	public DatabaseResult selectPayments(String paymentCode) throws IOException {
		//String[][] dbresult = {};
		DatabaseResult dBresult = new DatabaseResult();

		try {
			dBresult = executeSelectPayments(paymentCode);
			// dbresult = dBresult.getSelectLine();
		} catch (SQLException ex) {
			for (Throwable t : ex)
				t.printStackTrace();
		}

		// return dbresult;
		return dBresult;
	}

	/**
	 * Executes the insert SQL operation against the database
	 *
	 */
	private String executeInsertPayment(String paymentCd, 
										String order, 
										String payTime, 
										String payMehtod,
										String servSurvey, 
										String oriPrice, 
										String totPaid, 
										String custId) throws SQLException, IOException {

		String dbresult = "";
		// try (Connection conn = getConnectionNoFile();
		// try (Connection conn = getConnection();
		try (Connection conn = getConnectionFromEnvVars(); Statement stat = conn.createStatement()) {
			// logging values passed:
			System.out.println("parameter paymentCd: "  + paymentCd);
			System.out.println("parameter order: "      + order);
			System.out.println("parameter payTime: "    + payTime);
			System.out.println("parameter payMehtod: "  + payMehtod);
			System.out.println("parameter servSurvey: " + servSurvey);
			System.out.println("parameter oriPrice: "   + oriPrice);
			System.out.println("parameter totPaid: "    + totPaid);
			System.out.println("parameter custId: "     + custId);

			stat.executeUpdate("INSERT INTO PAYMENTS (PAYMENTCODE,ORDERID,PAYMENTTIME,PAYMENTMETHOD,SERVICESURVEY,ORIGINALPRICE,TOTALPAID,CUSTOMERID)"
					+ "VALUES ('" + paymentCd + "','"
					+ order + "', "
					+ payTime + ", '"
					+ payMehtod + "', '"
					+ servSurvey + "', '"
					+ oriPrice + "', '"
					+ totPaid + "', '"
					+ custId +
					"')");

			try (ResultSet result = stat.executeQuery("SELECT * FROM PAYMENTS where PAYMENTCODE = '" + paymentCd + "'")) {
				if (result.next()) {
					System.out.println(result.getString(1));
					dbresult = result.getString(1);
				} else {
					System.out.println("ERROR IN DB");
					dbresult = "ERROR IN DB";
				}
			}

		}
		return dbresult;
	}

	/**
	 * Executes the Select SQL operation against the database to obtain all the
	 * payments
	 *
	 */
	private DatabaseResult executeSelectPayments(String paymentCd) throws SQLException, IOException {
		/**
		 TODO use ArrayList lines instead of String[][] selectLine to optimize storage
		 only used to initialize with some values. 100 columns and 500 lines should
		 be enough
		 */
		int maxNumberOfColumnsTable = 100;
		int maxNumberOfLinesTable   = 500;

		String[][] selectLine     = new String[maxNumberOfLinesTable][maxNumberOfColumnsTable];
		ArrayList<String> lines   = new ArrayList<String>();
		int selectLineColumnCount;
		int selectLinesCount;
		int numColumnsListArray;
		String query;

		try (Connection conn = getConnectionFromEnvVars(); Statement stat = conn.createStatement()) {
			if (paymentCd.isEmpty()) {
				query = "SELECT * FROM PAYMENTS WHERE rownum <= 50 ORDER BY PAYMENTTIME DESC";
				System.out.println("parameter paymentCd has not been sent");
			} else {
				query = "SELECT * FROM PAYMENTS where PAYMENTCODE = '" + paymentCd + "'";
				System.out.println("parameter paymentCd: " + paymentCd);
			}

			try (ResultSet result2 = stat.executeQuery(query)) {
				ResultSetMetaData metaData = result2.getMetaData();
				selectLineColumnCount      = metaData.getColumnCount();
				numColumnsListArray        = selectLineColumnCount;

				for (int i = 1; i <= selectLineColumnCount; i++) {
					selectLine[0][i - 1] = metaData.getColumnLabel(i);
					lines.add(metaData.getColumnLabel(i));
				}
				System.out.println();

				selectLinesCount = 0;
				while (result2.next()) {
					selectLinesCount++; // selectLinesCount will count the real number of lines to send, to avoid
					// sending the MaxLimit right now defined as 50K
					for (int i = 1; i <= selectLineColumnCount; i++) {
						selectLine[selectLinesCount][i - 1] = result2.getString(i);
						lines.add(result2.getString(i));
						System.out.print("\n selectLine[" + selectLinesCount + "][" + (i - 1) + "]"
								+ selectLine[selectLinesCount][i - 1]);
					}

					System.out.println();
				}
				System.out.print("lines :" + lines);
				System.out.print("\nlines length :" + lines.size());
			}
		}
		
		return new DatabaseResult(lines, 
								  selectLine, 
								  selectLineColumnCount, 
								  numColumnsListArray, 
								  lines.size(),
								  selectLinesCount);
	}

	/**
	 * Gets a connection from the properties specified in the file
	 * database.properties.
	 *
	 * @return the database connection
	 *
	 *
	  public static Connection getConnection() throws SQLException, IOException {
		  Properties props = new Properties(); try (InputStream in =
		  Files.newInputStream(Paths.get("database.properties"))) { props.load(in); }
		  String drivers = props.getProperty("jdbc.drivers"); if (drivers != null)
		  System.setProperty("jdbc.drivers", drivers); String url =
		  props.getProperty("jdbc.url"); String username =
		  props.getProperty("jdbc.username"); String password =
		  props.getProperty("jdbc.password");
		  return DriverManager.getConnection(url, username, password);
	  }
	 */

	/**
	 * Get environment variables for database connectivity
	 *
	 * @return the database connection
	 */

	public static Connection getConnectionFromEnvVars() throws SQLException, IOException {
		/*
		 * Properties props = new Properties();
		 *
		 * try (InputStream in = Files.newInputStream(Paths.get("database.properties")))
		 * { props.load(in); }
		 *
		 * String drivers = props.getProperty("jdbc.drivers"); if (drivers != null)
		 * System.setProperty("jdbc.drivers", drivers); String url =
		 * props.getProperty("jdbc.url"); String username =
		 * props.getProperty("jdbc.username"); String password =
		 * props.getProperty("jdbc.password");
		 *
		 */
		String javaHome      = System.getenv("JAVA_HOME");;
		String sqldbUrl      = System.getenv("SQLDB_URL");;
		String sqldbUsername = System.getenv("SQLDB_USERNAME");;
		String sqldbPassword = System.getenv("SQLDB_PASSWORD");;

		System.out.println("JAVA_HOME:      " + javaHome);
		System.out.println("SQLDB_URL:      " + sqldbUrl);
		System.out.println("SQLDB_USERNAME: " + sqldbUsername);
		System.out.println("SQLDB_PASSWORD: " + "********");
		
		return DriverManager.getConnection(sqldbUrl, sqldbUsername, sqldbPassword);
	}
}
