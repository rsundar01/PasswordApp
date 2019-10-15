package com.security;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestAppServerProvider {
    private HttpServer httpServer;
    private CloseableHttpClient httpClient;
    private final String TEST_DATA_KEY = "password";

    /** test data **/
    int nTestData = 3;
    String[] data = new String[nTestData];
    String[] hashdata = new String[nTestData];



    @Before
    public void setUp(){
        httpServer = AppServerProvider.getHttpServer();
        if(httpServer != null) httpServer.start();
        httpClient = HttpClients.createDefault();

        data[0] = "procore";
        hashdata[0] = "VWx+vwK0xCGmazm68Bs7grHIXJv7Nl0W3vwR2DZ79dLGgoG0L+/9O3zc1xRmM28ltCujLRUb1/nEqJU3fQJMRw==";

        data[1] = "angryMonkey";
        hashdata[1] = "ZEHhWB65gUlzdVwtDQArEyx+KVLzp/aTaRaPlBzYRIFj6vjFdqEb0Q5B8zVKCZ0vKbZPZklJz0Fd7su2A+gf7Q==";

        Random random = new Random();
        byte[] randomBytes = new byte[1024];
        random.nextBytes(randomBytes);
        data[2] = new String(randomBytes);
        hashdata[2] = "";

    }

    @After
    public void tearDown(){
        if(httpServer != null) httpServer.stop(1);
    }

    @Test
    public void checkServerInstance(){
        CloseableHttpResponse httpResponse = null;
        try {

            /* Check with normal Input */
            httpResponse = makePostRequest("/hash", data[0]);
            Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
            Assert.assertEquals( (long) hashdata[0].length(), httpResponse.getEntity().getContentLength());

            // Compare contents
            byte[] resBodyBytes = new byte[(int)httpResponse.getEntity().getContentLength()];
            httpResponse.getEntity().getContent().read(resBodyBytes);
            Assert.assertEquals(hashdata[0], new String(resBodyBytes, StandardCharsets.UTF_8));

            /* Check with long input */
            httpResponse = makePostRequest("/hash", data[2]);
            Assert.assertEquals(400, httpResponse.getStatusLine().getStatusCode());

        }catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            try{
                if(httpResponse != null) httpResponse.close();
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


    private CloseableHttpResponse makePostRequest(String endpoint, String data) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:8080" + endpoint);
        List<NameValuePair> parameterList = new ArrayList<>();
        parameterList.add(new BasicNameValuePair(TEST_DATA_KEY, data));
        httpPost.setEntity(new UrlEncodedFormEntity(parameterList));
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        return httpResponse;
    }

    private CloseableHttpResponse makeGetRequest(String endpoint) throws IOException {
        HttpGet httpPost = new HttpGet("http://localhost:8080" + endpoint);
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        return httpResponse;
    }
}
