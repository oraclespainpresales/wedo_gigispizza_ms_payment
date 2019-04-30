/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.quickstart.se;

import java.io.IOException;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

/**
 * A ervice to create payments in database. Examples:
 *
 * Get default greeting message:
 * curl -X GET http://localhost:8080/greet
 *
 * Get greeting message for Joe:
 * curl -X GET http://localhost:8080/greet/Joe
 *
 * Create payment in database
 * curl -X POST -H "Content-Type: application/json" -d '{"paymentid": "019374432883", "paymentTime": "18-JAN-2019 11:50 AM","orderId": "000101", "paymentMethod": "VISA", "serviceSurvey": "5","totalPayed": "33", "customerId": "c002"}' http://localhost:9002/helidon/payment
 *
 * The message is returned as a JSON object
 */

public class PaymentService implements Service {

    /**
     * The config file is used only to provide info regarding the server
     */
    
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    PaymentService(Config config) {
       // this.greeting = config.get("app.greeting").asString().orElse("Ciao");
    }

    /**
     * A service registers itself by updating the routine rules.
     * @param rules the routing rules.
     */
    @Override
    public void update(Routing.Rules rules) {
        rules
            .post("/selectpayment", this::selectDatabaseHandler)
            .post("/payment", this::updateDatabaseHandler);
    }

    
    
    /**
     * Set the preparation to call json object param check, database call and response.
     * @param request the server request
     * @param response the server response
     */
    private void updateDatabaseHandler(ServerRequest request,
                                       ServerResponse response) {
        request.content().as(JsonObject.class).thenAccept(jo -> {
			try {
				updateJsonDBResponse(jo, response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
    }
    
    /**
     * Set the preparation to call json object param check, database call and response to process select results.
     * @param request the server request
     * @param response the server response
     */
    private void selectDatabaseHandler(ServerRequest request,
                                       ServerResponse response) {
        request.content().as(JsonObject.class).thenAccept(jo -> {
			try {
				selectJsonDBResponse(jo, response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
    }

    /**
     * Set the validaton of json object param check, database call and response.
     * @param request the server request
     * @param response the server response
     */
    private void updateJsonDBResponse(JsonObject jo, ServerResponse response) throws IOException {

    	String errorParam;
    	String dbresult;
    	
    	errorParam = "Generic error with json object";
    	
     
   
        if (!jo.containsKey("paymentid")) {
        	
        	errorParam = "No paymentid provided";
        	
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
        
        if (!jo.containsKey("paymentTime")) {
        	
        	errorParam = "No paymentTime provided";
        	
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
        
        
        if (!jo.containsKey("orderId")) {
        	errorParam = "No orderId provided";
        	
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
        
        if (!jo.containsKey("paymentMethod")) {
        	errorParam = "No paymentMethod provided";
        	
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
        
        if (!jo.containsKey("serviceSurvey")) {
        	errorParam = "No serviceSurvey provided";
        	
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
        
        if (!jo.containsKey("totalPayed")) {
        	errorParam = "No totalPayed provided";
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
        
        if (!jo.containsKey("customerId")) {
        	errorParam = "No customerId provided";
        	
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
                
        
 
      // Get parameters from json object  
        String paymentid = jo.getString("paymentid");
        String tempPaymentTime = jo.getString("paymentTime");
        String paymentTime = "TO_DATE('"+tempPaymentTime+"', 'dd-mon-yyyy hh:mi PM')"; 
        String orderId = jo.getString("orderId");
    	String paymentMethod = jo.getString("paymentMethod");
    	String serviceSurvey = jo.getString("serviceSurvey");
    	String totalPayed = jo.getString("totalPayed");
    	String customerId = jo.getString("customerId");
        
      
    	// Call database
        DatabaseClient dbclient = new DatabaseClient();
    	dbresult = dbclient.insertPayment(paymentid, orderId, paymentTime, paymentMethod, serviceSurvey, totalPayed, customerId );
        //if dbresult is null or empty return a string with "SQL ERROR - Check logs"

    	JsonObject returnObject = JSON.createObjectBuilder()
                .add("message", "payment creation requested")
                .add("dbresult", dbresult)
                .build();
        
        response.status(Http.Status.OK_200).send(returnObject);
    }
    
    
    /**
     * Set the validaton of json object param check, database call and response.
     * @param request the server request
     * @param response the server response
     */
    private void selectJsonDBResponse(JsonObject jo, ServerResponse response) throws IOException {

    	String errorParam;
    	String[][] dbresult = {};
    	
    	errorParam = "Generic error with json object";
    	
     
   
        if (!jo.containsKey("paymentid")) {
        	
        	errorParam = "No paymentid provided! If you want a select all, then you need to at least send paymentid ='' ";
        	
        	  JsonObject jsonErrorObject = JSON.createObjectBuilder()
                      .add("error", errorParam)
                      .build();
              response.status(Http.Status.BAD_REQUEST_400)
                      .send(jsonErrorObject);
              return;
        }
        
 
      // Get parameters from json object  
        String paymentid = jo.getString("paymentid");
      
    	// Call database service
        DatabaseClient dbclient = new DatabaseClient();
        dbresult = dbclient.selectPayments(paymentid);
     
 	   int line = 0;
  	   while (line <= 10)
  	   {       		   
  		  
  		   for (int column = 0; column <= 6; column++)
      	   {
      		System.out.print("\n dbresult["+line+"]["+(column)+"]" + dbresult[line][column]);
      	   }
  		   line++;
  	   }
  	   
  	
  	//Checking how many lines dbresult has. If more than 1 then prepare to send multiple objects, if not send a single object associated with a single line    
  	boolean resSelectLineNoMoreThanOne = (dbresult[2][0] == null || dbresult[2][0].length() == 0);
  	
  	if (resSelectLineNoMoreThanOne){
		  
  		System.out.println("SECOND LINE IS EMPTY");  
        
        JsonObject returnObject1 = Json.createObjectBuilder()
        		      .add("payment", Json.createObjectBuilder()
        		          .add(dbresult[0][0], dbresult[1][0])
        		          .add(dbresult[0][1], dbresult[1][1])
        		          .add(dbresult[0][2], dbresult[1][2])
        		          .add(dbresult[0][3], dbresult[1][3])
        		          .add(dbresult[0][4], dbresult[1][4])
        		          .add(dbresult[0][5], dbresult[1][5])
        		          .add(dbresult[0][6], dbresult[1][6]))
        		      .build();
        
        JsonObject returnObject = JSON.createObjectBuilder()
                .add("message", "single line select requested")
                .add("singleline", returnObject1)
                .build();
        
        response.status(Http.Status.OK_200).send(returnObject);
        
  	  }else{
  		System.out.println("SECOND LINE IS NOT EMPTY");  
        
        JsonObject returnObject1 = Json.createObjectBuilder()
        		      .add("payment", Json.createObjectBuilder()
        		          .add(dbresult[0][0], dbresult[1][0])
        		          .add(dbresult[0][1], dbresult[1][1])
        		          .add(dbresult[0][2], dbresult[1][2])
        		          .add(dbresult[0][3], dbresult[1][3])
        		          .add(dbresult[0][4], dbresult[1][4])
        		          .add(dbresult[0][5], dbresult[1][5])
        		          .add(dbresult[0][6], dbresult[1][6]))
        		      .build();
        
        JsonObject returnObject2 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[2][0])
  		          .add(dbresult[0][1], dbresult[2][1])
  		          .add(dbresult[0][2], dbresult[2][2])
  		          .add(dbresult[0][3], dbresult[2][3])
  		          .add(dbresult[0][4], dbresult[2][4])
  		          .add(dbresult[0][5], dbresult[2][5])
  		          .add(dbresult[0][6], dbresult[2][6]))
  		      .build();
        
        JsonObject returnObject3 = Json.createObjectBuilder()
    		      .add("payment", Json.createObjectBuilder()
    		          .add(dbresult[0][0], dbresult[3][0])
    		          .add(dbresult[0][1], dbresult[3][1])
    		          .add(dbresult[0][2], dbresult[3][2])
    		          .add(dbresult[0][3], dbresult[3][3])
    		          .add(dbresult[0][4], dbresult[3][4])
    		          .add(dbresult[0][5], dbresult[3][5])
    		          .add(dbresult[0][6], dbresult[3][6]))
    		      .build();
        
        JsonObject returnObject4 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[4][0])
  		          .add(dbresult[0][1], dbresult[4][1])
  		          .add(dbresult[0][2], dbresult[4][2])
  		          .add(dbresult[0][3], dbresult[4][3])
  		          .add(dbresult[0][4], dbresult[4][4])
  		          .add(dbresult[0][5], dbresult[4][5])
  		          .add(dbresult[0][6], dbresult[4][6]))
  		      .build();
        
        JsonObject returnObject5 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[5][0])
  		          .add(dbresult[0][1], dbresult[5][1])
  		          .add(dbresult[0][2], dbresult[5][2])
  		          .add(dbresult[0][3], dbresult[5][3])
  		          .add(dbresult[0][4], dbresult[5][4])
  		          .add(dbresult[0][5], dbresult[5][5])
  		          .add(dbresult[0][6], dbresult[5][6]))
  		      .build();
        
        JsonObject returnObject6 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[6][0])
  		          .add(dbresult[0][1], dbresult[6][1])
  		          .add(dbresult[0][2], dbresult[6][2])
  		          .add(dbresult[0][3], dbresult[6][3])
  		          .add(dbresult[0][4], dbresult[6][4])
  		          .add(dbresult[0][5], dbresult[6][5])
  		          .add(dbresult[0][6], dbresult[6][6]))
  		      .build();
        
        JsonObject returnObject7 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[7][0])
  		          .add(dbresult[0][1], dbresult[7][1])
  		          .add(dbresult[0][2], dbresult[7][2])
  		          .add(dbresult[0][3], dbresult[7][3])
  		          .add(dbresult[0][4], dbresult[7][4])
  		          .add(dbresult[0][5], dbresult[7][5])
  		          .add(dbresult[0][6], dbresult[7][6]))
  		      .build();
        
        JsonObject returnObject8 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[8][0])
  		          .add(dbresult[0][1], dbresult[8][1])
  		          .add(dbresult[0][2], dbresult[8][2])
  		          .add(dbresult[0][3], dbresult[8][3])
  		          .add(dbresult[0][4], dbresult[8][4])
  		          .add(dbresult[0][5], dbresult[8][5])
  		          .add(dbresult[0][6], dbresult[8][6]))
  		      .build();
        
       JsonObject returnObject9 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[9][0])
  		          .add(dbresult[0][1], dbresult[9][1])
  		          .add(dbresult[0][2], dbresult[9][2])
  		          .add(dbresult[0][3], dbresult[9][3])
  		          .add(dbresult[0][4], dbresult[9][4])
  		          .add(dbresult[0][5], dbresult[9][5])
  		          .add(dbresult[0][6], dbresult[9][6]))
  		      .build();
        
       JsonObject returnObject10 = Json.createObjectBuilder()
  		      .add("payment", Json.createObjectBuilder()
  		          .add(dbresult[0][0], dbresult[10][0])
  		          .add(dbresult[0][1], dbresult[10][1])
  		          .add(dbresult[0][2], dbresult[10][2])
  		          .add(dbresult[0][3], dbresult[10][3])
  		          .add(dbresult[0][4], dbresult[10][4])
  		          .add(dbresult[0][5], dbresult[10][5])
  		          .add(dbresult[0][6], dbresult[10][6]))
  		      .build();
        
        
        //if dbresult is null or empty return a string with "SQL ERROR - Check logs"
        JsonObject returnObject = JSON.createObjectBuilder()
                .add("message", "multiline select requested")
                .add("line1", returnObject1)
                .add("line2", returnObject2)
                .add("line3", returnObject3)
                .add("line4", returnObject4)
                .add("line5", returnObject5)
                .add("line6", returnObject6)
                .add("line7", returnObject7)
                .add("line8", returnObject8)
                .add("line9", returnObject9)
                .add("line10", returnObject10)
                .build();
        
        response.status(Http.Status.OK_200).send(returnObject);
  	  }
      	   
    }


}
