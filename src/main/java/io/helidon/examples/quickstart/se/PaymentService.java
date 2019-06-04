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

//import java.util.List;
//import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;

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
 * curl -X POST -H "Content-Type: application/json" -d '{"paymentid": "019374432883", "paymentTime": "18-JAN-2019 11:50 AM","orderId": "000101", "paymentMethod": "VISA", "serviceSurvey": "5","originalPrice":"33","totalPaid": "33", "customerId": "c002"}' http://localhost:9002/helidon/payment
 *
 * The message is returned as a JSON object
 */

public class PaymentService implements Service {
	/**
	 * The config file is used only to provide info regarding the server
	 */

	private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

	PaymentService(Config config) {
		System.out.println(config.get("app.greeting").asString().orElse("Wedo DevOps Microservice PAYMENT"));
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
	private void updateDatabaseHandler(ServerRequest request, ServerResponse response) {
		request.content().as(JsonObject.class).thenAccept(jo -> {
			try {
				updateJsonDBResponse(jo, response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).exceptionally(t -> {
			System.err.println("fail no content provided: " + t.getMessage());
			t.printStackTrace(System.err);
			JsonObject jsonErrorObject = JSON.createObjectBuilder()
					.add("error", "No data provided! to create a payment information needed... ")
					.build();
			response.status(Http.Status.BAD_REQUEST_400)
					.send(jsonErrorObject);
			return null;
		});
	}

	/**
	 * Set the preparation to call json object param check, database call and response to process select results.
	 * @param request the server request
	 * @param response the server response
	 */
	private void selectDatabaseHandler(ServerRequest request, ServerResponse response) {
		request.content().as(JsonObject.class).thenAccept(jo -> {
			try {
				selectJsonDBResponse(jo, response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).exceptionally(t -> {
				System.err.println("fail no content provided: " + t.getMessage());
				t.printStackTrace(System.err);
				JsonObject jsonErrorObject = JSON.createObjectBuilder()
						.add("error", "No data provided! If you want a select all, then you need to at least send paymentid ='' ")
						.build();
				response.status(Http.Status.BAD_REQUEST_400)
						.send(jsonErrorObject);
				return null;
		});
	}

	/**
	 * Set the validaton of json object param check, database call and response.
	 * @param response the server response
	 */
	private void updateJsonDBResponse(JsonObject jo, ServerResponse response) throws IOException {
		String dbresult;
		String errorParam = "";

		if (!jo.containsKey("paymentid")) {
			errorParam = "No paymentid provided";
		} else if (!jo.containsKey("paymentTime")) {
			errorParam = "No paymentTime provided";
		} else if (!jo.containsKey("orderId")) {
			errorParam = "No orderId provided";
		} else if (!jo.containsKey("paymentMethod")) {
			errorParam = "No paymentMethod provided";
		} else if (!jo.containsKey("serviceSurvey")) {
			errorParam = "No serviceSurvey provided";
		} else if (!jo.containsKey("originalPrice")) {
			errorParam = "No originalPrice provided";
		} else if (!jo.containsKey("totalPaid")) {
			errorParam = "No totalPaid provided";
		} else if (!jo.containsKey("customerId")) {
			errorParam = "No customerId provided";
		}

		if (!errorParam.isEmpty()){
			JsonObject jsonErrorObject = JSON.createObjectBuilder()
					.add("error", errorParam)
					.build();
			response.status(Http.Status.BAD_REQUEST_400)
					.send(jsonErrorObject);
		}
		else {
			// Get parameters from json object
			String paymentid = jo.getString("paymentid");
			String tempPaymentTime = jo.getString("paymentTime");
			String paymentTime = "TO_TIMESTAMP('" + tempPaymentTime + "', 'YYYY-MM-DD\"T\"HH24:MI:SS.ff3\"Z\"')";
			String orderId = jo.getString("orderId");
			String paymentMethod = jo.getString("paymentMethod");
			String serviceSurvey = jo.getString("serviceSurvey");
			String originalPrice = jo.getString("originalPrice");
			String totalPaid = jo.getString("totalPaid");
			String customerId = jo.getString("customerId");

			// Call database
			DatabaseClient dbclient = new DatabaseClient();
			dbresult = dbclient.insertPayment(paymentid, orderId, paymentTime, paymentMethod, serviceSurvey, originalPrice, totalPaid, customerId);
			//if dbresult is null or empty return a string with "SQL ERROR - Check logs"

			JsonObject returnObject = JSON.createObjectBuilder()
					.add("message", "payment creation requested")
					.add("dbresult", dbresult)
					.build();
			response.status(Http.Status.OK_200).send(returnObject);
		}
	}


	/**
	 * Set the validaton of json object param check, database call and response.
	 * @param response the server response
	 */
	private void selectJsonDBResponse(JsonObject jo, ServerResponse response) throws IOException {
		if (!jo.containsKey("paymentid")) {
			JsonObject jsonErrorObject = JSON.createObjectBuilder()
					.add("error", "No paymentid provided! If you want a select all, then you need to at least send paymentid ='' ")
					.build();
			response.status(Http.Status.BAD_REQUEST_400)
					.send(jsonErrorObject);
		}
		else {
			// Get parameters from json object
			String paymentid = jo.getString("paymentid");

			// Call database service
			DatabaseClient dbclient = new DatabaseClient();
			DatabaseResult dbResult = new DatabaseResult();

			dbResult = dbclient.selectPayments(paymentid);
			String[][] dbresult = dbResult.getSelectLine();
			//ArrayList<String> lines = dbResult.getLines();
			System.out.print("\n dbResult.getLines() " + dbResult.getLines());
			System.out.print("\n dbResult.getLinesLength() " + dbResult.getLinesLength());
			System.out.print("\n dbResult.getNumColumnsListArray() " + dbResult.getNumColumnsListArray());

			//dbresult = dbclient.selectPayments(paymentid);

			//TODO to solve this bug print here object dbResult and check values
			System.out.println("\ndbResult.getNumColumnsSelectLine() :" + dbResult.getNumColumnsSelectLine());
			System.out.println("\ndbResult.getSelectLineLength() :" + dbResult.getSelectLineLength());

			int line = 0;
			while (line <= dbResult.getSelectLineLength()) {
				for (int column = 0; column <= (dbResult.getNumColumnsSelectLine() - 1); column++) {
					System.out.print("\n dbresult[" + line + "][" + (column) + "]" + dbresult[line][column]);
				}
				line++;
			}

			// Group keys
			String JSON_KEY_MEMBERS_LIST = "rows";
			JsonObjectBuilder[] groupArray = new JsonObjectBuilder[dbResult.getSelectLineLength()];

			System.out.println("groupArray length :" + groupArray.length); //10
			System.out.println("dbresult length :" + dbresult.length); //100

			//TODO use dynamic arrays with lists and ArrayList

			for (int i = 0; i < groupArray.length; i++) {
				//this code is to initialize all available space within array
				groupArray[i] = Json.createObjectBuilder();
			}

			for (int j = 0; j < dbResult.getSelectLineLength()/*dbresult.length*/; j++) {
				//5 below is the number of columns or attributes
				//System.out.println("Dentro do dbresult valor de J :"+j);
				for (int i = 0; i < dbResult.getNumColumnsSelectLine(); i++) {
					//System.out.println("Dentro do dbresult valor de I :"+i);
					if ((j + 1) < dbresult.length) {
						if ((!(dbresult[j + 1][i] == null || dbresult[j + 1][i].length() == 0))) {
							//		System.out.println("dbresult nao é null :" + dbresult[j + 1][i]);
							groupArray[j].add(dbresult[0][i], dbresult[j + 1][i]);
						}
					}


				}
			}
			System.out.println("\ngroupArray length 2nd:" + groupArray.length);
			JsonArrayBuilder membersArray = Json.createArrayBuilder();

			for (int j = 0; j < groupArray.length; j++) {
				if (!(groupArray[j] == null)) {
					System.out.println("groupArrayFinal[" + j + "]: " + groupArray[j]);
					membersArray.add(groupArray[j]);
				}

			}

			JsonObjectBuilder groupFinal = Json.createObjectBuilder();
			groupFinal.add(JSON_KEY_MEMBERS_LIST, membersArray.build());
			System.out.print("\n");
			response.status(Http.Status.OK_200).send(groupFinal.build().toString());
		}
	}
}
