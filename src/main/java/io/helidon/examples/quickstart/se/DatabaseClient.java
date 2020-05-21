package io.helidon.examples.quickstart.se;

//import java.nio.file.*;
import java.sql.*;
import java.io.*;
//import java.util.*;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.Json;

//import io.helidon.webserver.ServerResponse;

/**
 * Executes SQL statements calls against a jdbc DB.
 *
 * @version 1.00 2019-03-26
 * @author Fernando Harris and modified by Iván Sampedro
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
	public JsonObject selectPayments(String paymentCode, int maxNumRows) throws IOException {
		//String[][] dbresult = {};
		JsonObject dBresult = null;

		try {
			dBresult = executeSelectPayments(paymentCode,maxNumRows);
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
		try (Connection conn = getConnectionFromEnvVars();Statement stat = conn.createStatement()) {
			StringBuffer insertSQL = new StringBuffer("INSERT INTO PAYMENTS (")
											  .append("PAYMENTCODE").append(",")
											  .append("ORDERID").append(",")
											  .append("PAYMENTTIME").append(",")
											  .append("PAYMENTMETHOD").append(",")
											  .append("SERVICESURVEY").append(",")
											  .append("ORIGINALPRICE").append(",")
											  .append("TOTALPAID").append(",")
											  .append("CUSTOMERID").append(")")
											  .append(" VALUES (?,?,TO_TIMESTAMP(?,'YYYY-MM-DD\"T\"HH24:MI:SS.ff3\"Z\"'),?,?,?,?,?)");

			// logging values passed:
			System.out.println(insertSQL.toString());
			System.out.println("parameter 1 paymentCd: "  + paymentCd);
			System.out.println("parameter 2 order: "      + order);
			System.out.println("parameter 3 payTime: "    + payTime);
			System.out.println("parameter 4 payMehtod: "  + payMehtod);
			System.out.println("parameter 5 servSurvey: " + servSurvey);
			System.out.println("parameter 6 oriPrice: "   + oriPrice);
			System.out.println("parameter 7 totPaid: "    + totPaid);
			System.out.println("parameter 8 custId: "     + custId);

			PreparedStatement pstat = conn.prepareStatement(insertSQL.toString());

			pstat.setString(1,paymentCd);
			pstat.setString(2,order);
			pstat.setString(3,payTime);
			pstat.setString(4,payMehtod);
			pstat.setInt   (5,Integer.parseInt(servSurvey));
			pstat.setFloat (6,Float.parseFloat(oriPrice));
			pstat.setFloat (7,Float.parseFloat(totPaid));
			pstat.setString(8,custId);

			if (pstat.executeUpdate() > 0){
				try (ResultSet result = stat.executeQuery("SELECT * FROM PAYMENTS where PAYMENTCODE = '" + paymentCd + "'")) {
					if (result.next()) {
						System.out.println(result.getString(1));
						dbresult = result.getString(1);
					} else {
						System.out.println("ERROR IN DB getting new inserted value");
						dbresult = "ERROR IN DB getting new inserted value";
					}
				}
			}
			else {
				System.out.println("ERROR IN DB INSERT result <= 0");
				dbresult = "ERROR IN DB INSERT result <= 0";
			}
		}
		return dbresult;
	}

	/**
	 * Executes the Select SQL operation against the database to obtain maxnumrows payments
	 * the payment code passed as parameter
	 *
	 */
	private JsonObject executeSelectPayments(String paymentCd, int maxNumRows) throws SQLException, IOException {
		//Return a JsonArray with db maxnumrows data from db or the selected payment code.
		String query;
		int numColumns;
		JsonArrayBuilder jRowsBuilder = Json.createArrayBuilder();

		try (Connection conn = getConnectionFromEnvVars(); Statement stat = conn.createStatement()) {
			if (paymentCd.isEmpty()) {
				query = "SELECT * FROM (SELECT * FROM PAYMENTS ORDER BY PAYMENTTIME DESC) WHERE rownum <= " + maxNumRows;
				System.out.println("parameter paymentCd has not been sent : " + query);
			} else {
				query = "SELECT * FROM PAYMENTS where PAYMENTCODE = '" + paymentCd + "'";
				System.out.println("parameter paymentCd: " + paymentCd + " : " + query);
			}

			try (ResultSet rset = stat.executeQuery(query)) {
				ResultSetMetaData metaData = rset.getMetaData();
				numColumns 				   = metaData.getColumnCount();
			

				while(rset.next()){
					JsonObjectBuilder jColumnBuilder = Json.createObjectBuilder();
					for (int i=1;i <= numColumns;i++){
						jColumnBuilder.add(metaData.getColumnLabel(i), rset.getString(i));
					}
					jRowsBuilder.add(jColumnBuilder);
				}
			}
		}
		return Json.createObjectBuilder().add("rows",jRowsBuilder).build();
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
