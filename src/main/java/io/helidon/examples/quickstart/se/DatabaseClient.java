package io.helidon.examples.quickstart.se;

import java.nio.file.*;
import java.sql.*;
import java.io.*;
import java.util.*;

import io.helidon.webserver.ServerResponse;


/**
* Executes SQL statements calls against a jdbc DB.
* @version 1.00 2019-03-26
* @author Fernando Harris
* 
*/
public class DatabaseClient
{
 public String insertPayment(String paymentCode, String orderId, String paymentTime, String paymentMehtod, String serviceSurvey, String totalPayed, String customerId ) throws IOException
 {
	 String dbresult = "";
	 
    try
    {
    	dbresult = executeInsertPayment(paymentCode, orderId, paymentTime, paymentMehtod, serviceSurvey, totalPayed, customerId );
    }
    catch (SQLException ex)
    {
       for (Throwable t : ex)
          t.printStackTrace();
    }
    
    return dbresult;
 }
 
 public String[][] selectPayments(String paymentCode) throws IOException
 {
	 String[][] dbresult = {};
	 
    try
    {
    	dbresult = executeSelectPayments(paymentCode);
    }
    catch (SQLException ex)
    {
       for (Throwable t : ex)
          t.printStackTrace();
    }
    
    return dbresult;
 }

 /**
  * Executes the insert SQL operation against the database 
  * 
  */
 public static String executeInsertPayment(String paymentCd, String order, String payTime, String payMehtod, String servSurvey, String totPayed, String custId ) throws SQLException, IOException
 {  
	 String dbresult = "";
	 
    try (Connection conn = getConnectionNoFile();
       Statement stat = conn.createStatement())
    {
    	
    	//logging values passed:
    	 System.out.println("parameter paymentCd: " +paymentCd);
    	 System.out.println("parameter order: "		+order);
    	 System.out.println("parameter payTime: "	+payTime);
    	 System.out.println("parameter payMehtod: "	+payMehtod);
    	 System.out.println("parameter servSurvey: "+servSurvey);
    	 System.out.println("parameter totPayed: "	+totPayed);
    	 System.out.println("parameter custId: "	+custId);
    	
 
    	stat.executeUpdate("INSERT INTO PAYMENTS (PAYMENTCODE, ORDERID, PAYMENTTIME, PAYMENTMETHOD, SERVICESURVEY, TOTALPAYED, CUSTOMERID) "
    			+ "VALUES ('"+paymentCd+"','"+order+"', "+payTime+", '"+payMehtod+"', '"+servSurvey+"', '"+totPayed+"', '"+custId+"')");
    	

       try (ResultSet result = stat.executeQuery("SELECT * FROM PAYMENTS where PAYMENTCODE = '"+paymentCd+"'"))
       {
          if (result.next()){
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
  * Executes the Select SQL operation against the database to obtain all the payments
  * 
  */
 public static String[][] executeSelectPayments(String paymentCd) throws SQLException, IOException
 {  
	String[][] selectLine = new String[100][7];
	 
    try (Connection conn = getConnectionNoFile();
       Statement stat = conn.createStatement())
    {
    	
    	if (paymentCd.isEmpty())
    	{
    		System.out.println("parameter paymentCd has not been sent");

	    	try (ResultSet result2 = stat.executeQuery("SELECT * FROM PAYMENTS ORDER BY PAYMENTTIME DESC FETCH FIRST 10 ROW ONLY"))
	       	 {
	       	   ResultSetMetaData metaData = result2.getMetaData();
	       	   int columnCount = metaData.getColumnCount();
	       	   
	       	   for (int i = 1; i <= columnCount; i++)
	       	   {
	       		 selectLine[0][i-1] = metaData.getColumnLabel(i);
	       	   }
	       	   System.out.println();
	       	   
	       	   int op = 0;
	       	   while (result2.next())
	       	   {       		   
	       		   op++;
	       		   for (int i = 1; i <= columnCount; i++)
	           	   {
	       			selectLine[op][i-1] = result2.getString(i);
	           		System.out.print("\n selectLine["+op+"]["+(i-1)+"]" + selectLine[op][i-1]);
	           	   }
	       		   System.out.println();
	       	   }
          
	       	 }	
    	}else
    	{
    	 //logging values passed:    	
    	 System.out.println("parameter paymentCd: " +paymentCd);
  
       
    	 try (ResultSet result2 = stat.executeQuery("SELECT * FROM PAYMENTS where PAYMENTCODE = '"+paymentCd+"'"))
    	 {
    	   ResultSetMetaData metaData = result2.getMetaData();
    	   int columnCount = metaData.getColumnCount();
    	   
    	   for (int i = 1; i <= columnCount; i++)
    	   {
    		 selectLine[0][i-1] = metaData.getColumnLabel(i);
    	   }
    	   System.out.println();
    	   
    	   while (result2.next())
    	   {
    		   for (int i = 1; i <= columnCount; i++)
        	   {
    			 selectLine[1][i-1] = result2.getString(i);
        	   }
    		   System.out.println();
    	   }
       
       
       }
    }
   
    }
    return selectLine;
 }
 
 
 /**
  * Gets a connection from the properties specified in the code.
  * @return the database connection
  */
 public static Connection getConnectionNoFile() throws SQLException, IOException
 { 
 
 
    String url = "jdbc:oracle:thin:@//130.61.124.136:1521/dodbhp_pdb1.sub03010825490.devopsvcn.oraclevcn.com";
    String username = "microservice";
    String password = "AAZZ__welcomedevops123";
    
    

    return DriverManager.getConnection(url, username, password);
 }

 /**
  * Gets a connection from the properties specified in the file database.properties.
  * @return the database connection
  */
 /*
 public static Connection getConnection() throws SQLException, IOException
 { 
    Properties props = new Properties();
    try (InputStream in = Files.newInputStream(Paths.get("database.properties")))
    {
       props.load(in);
    }
    String drivers = props.getProperty("jdbc.drivers");
    if (drivers != null) System.setProperty("jdbc.drivers", drivers);
    String url = props.getProperty("jdbc.url");
    String username = props.getProperty("jdbc.username");
    String password = props.getProperty("jdbc.password");
    
    

    return DriverManager.getConnection(url, username, password);
 }
*/

 
}
