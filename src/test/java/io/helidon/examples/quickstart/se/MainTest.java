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

import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.net.URL;
import java.net.HttpURLConnection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import io.helidon.webserver.WebServer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest {

    private static WebServer webServer;
    private static final JsonReaderFactory JSON = Json.createReaderFactory(Collections.emptyMap());

    @BeforeAll
    public static void startTheServer() throws Exception {
        webServer = Main.startServer();

        long timeout = 2000; // 2 seconds should be enough to start the server
        long now = System.currentTimeMillis();

        while (!webServer.isRunning()) {
            Thread.sleep(100);
            if ((System.currentTimeMillis() - now) > timeout) {
                Assertions.fail("Failed to start webserver");
            }
        }
    }

    @AfterAll
    public static void stopServer() throws Exception {
        if (webServer != null) {
            webServer.shutdown()
                     .toCompletableFuture()
                     .get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testHelloWorld() throws Exception {
        HttpURLConnection conn;
        conn = getURLConnection("POST", "/helidon/payment");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        //os.write("{\"paymentid\" : \"FerInt051\" , \"paymentTime\" : \"18-JAN-2019 11:50 AM\" , \"orderId\" : \"000101\" , \"paymentMethod\" : \"VISA\" , \"serviceSurvey\" : \"4\" , \"totalPayed\" : \"22\" , \"customerId\" : \"c002\"}".getBytes());
        String objectPyament = "{\"paymentid\" : \"FerInt051\" , " +
                                "\"paymentTime\" : \"2019-05-22T08:08:43.270Z\" , " +
                                "\"orderId\" : \"20190604114154\" , " +
                                "\"paymentMethod\" : \"VISA\" , " +
                                "\"serviceSurvey\" : \"4\" , " +
                                "\"originalPrice\" : \"22\" , " +
                                "\"totalPaid\" : \"22\" , " +
                                "\"customerId\" : \"c002\"}";
        System.out.println("object : " + objectPyament);
        os.write(objectPyament.getBytes());
        os.flush();
        os.close();
        Assertions.assertEquals(200, conn.getResponseCode(), "HTTP response3");
        
        conn = getURLConnection("POST", "/helidon/selectpayment");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        OutputStream os2 = conn.getOutputStream();
        os2.write("{\"paymentid\" : \"FerInt051\"}".getBytes());
        os2.close();
        Assertions.assertEquals(200, conn.getResponseCode(), "HTTP response3");
        
        conn = getURLConnection("POST", "/helidon/selectpayment");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        OutputStream os3 = conn.getOutputStream();
        os3.write("{\"paymentid\" : \"\"}".getBytes());
        os3.close();
        Assertions.assertEquals(200, conn.getResponseCode(), "HTTP response3");
              
        conn = getURLConnection("GET", "/health");
        Assertions.assertEquals(200, conn.getResponseCode(), "HTTP response2");

        conn = getURLConnection("GET", "/metrics");
        Assertions.assertEquals(200, conn.getResponseCode(), "HTTP response2");
        
    }

    private HttpURLConnection getURLConnection(String method, String path) throws Exception {
        URL url = new URL("http://localhost:" + webServer.port() + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        System.out.println("Connecting: " + method + " " + url);
        return conn;
    }
}
