package com.security;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashRequestHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashRequestHandler.class);


    private SHA512Hash sha512Hash = new SHA512Hash();
    private Base64Encoder base64Encoder = new Base64Encoder();
    private static HashRequestHandler hashRequestHandler= null;

    // Defaults
    private static final String DELAY = "5"; // 5 seconds
    private static final String MAX_INPUT_SIZE = "1024"; //1024 bytes

    // Properties key
    private static final String DELAY_KEY = "responseDelaySec";
    private static final String MAX_INPUT_KEY = "maxPasswordLenBytes";

    // static data
    private static String ENCODING = "UTF-8";
    private static String PASSWD_KEY = "password";
    private static String HEADER_CONTENT_KEY = "Content-Type";
    private static String HEADER_CONTENT_VALUE_JSON = "application/json;charset=" + ENCODING;
    private static String HEADER_CONTENT_VALUE_HTML = "text/html; charset=" + ENCODING;


    // stats
    private static long nRequests = 0;
    private static long sumDuration = 0;

    // request tracking


    private HashRequestHandler(){}

    public static HttpHandler getInstance(){
        if(hashRequestHandler == null) {
            synchronized (HashRequestHandler.class) {
                if(hashRequestHandler == null) hashRequestHandler = new HashRequestHandler();
            }
        }
        return hashRequestHandler;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // Start measuring execution time
        RequestTracking.requestTracker.incrementAndGet();

        try {
            Properties properties = PropertiesProvider.getProperties();
            if (properties == null)
                properties = new Properties();
            Map<String, String> propertiesMap = new HashMap<>((Map) properties);

            switch (httpExchange.getRequestMethod()) {
                case "GET":
                    getHandler(httpExchange);
                    break;
                case "POST":
                    Instant begin = Instant.now();
                    try {
                        Thread.sleep(Integer.parseInt(propertiesMap.getOrDefault(DELAY_KEY, DELAY)) * 1000);
                    } catch (InterruptedException inte) {
                        LOGGER.error(inte.getMessage());
                    }
                    postHandler(httpExchange, propertiesMap);
                    long elapsed = Duration.between(begin, Instant.now()).toMillis();
                    synchronized (this) {
                        nRequests++;
                        sumDuration += elapsed;
                    }
                    break;
                default:
                    handleError(httpExchange, "Method Not Allowed");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            handleError(httpExchange, "Internal Server Error");
        }


        RequestTracking.requestTracker.decrementAndGet();
    }

    private void postHandler(HttpExchange httpExchange, Map<String, String> propertiesMap) throws IOException{

        switch(httpExchange.getHttpContext().getPath()){
            case "/hash":
                hashEndpoint(httpExchange, propertiesMap);
                break;
            default:
                handleError(httpExchange, "Not Found");
        }
    }

    private void getHandler(HttpExchange httpExchange) throws IOException{

        switch(httpExchange.getHttpContext().getPath()){
            case "/stats":
                statsEndPoint(httpExchange);
                break;
            default:
                handleError(httpExchange, "Not Found");
        }
    }


    private void hashEndpoint(HttpExchange httpExchange, Map<String, String> propertiesMap) throws IOException {
        InputStream iDataStream = httpExchange.getRequestBody();
        OutputStream oDataStream = httpExchange.getResponseBody();
        int size = iDataStream.available();
        if(!validateContentLength(size, propertiesMap)){
            handleError(httpExchange, "Bad Request");
            return;
        }

        byte[] iDataBytes = null;
        byte[] oDataBytes = null;
        if(size > 0){
            iDataBytes = new byte[size];
        }
        if(iDataBytes != null) {
            iDataStream.read(iDataBytes);
            byte[] parsedParamBytes = parseUrlEncoded(iDataBytes);
            if(parsedParamBytes != null) {
                oDataBytes = base64Encoder.encodeBase64(sha512Hash.computeHash(parsedParamBytes));
            }
        }
        //Prepare for output
        addResponseHeader(httpExchange, "html");
        httpExchange.sendResponseHeaders(200, oDataBytes.length);
        oDataStream.write(oDataBytes);
    }


    private void statsEndPoint(HttpExchange httpExchange) throws IOException {
        long nReqeustsLocal = 0;
        long sumDurationLocal = 0;
        long average = 0;
        JSONObject jsonObject = new JSONObject();
        OutputStream oDataStream = httpExchange.getResponseBody();
        addResponseHeader(httpExchange, "json");
        byte[] oDataBytes = null;

        synchronized (this){
            nReqeustsLocal = nRequests;
            sumDurationLocal = sumDuration;
        }

        average = 0;
        if(nReqeustsLocal > 0){
            average = sumDurationLocal/nReqeustsLocal;
        }
        jsonObject.put("total", nReqeustsLocal);
        jsonObject.put("average", average);
        oDataBytes = jsonObject.toString().getBytes();
        httpExchange.sendResponseHeaders(200, oDataBytes.length);
        oDataStream.write(oDataBytes);

    }

    private byte[] parseUrlEncoded(byte[] iDataBytes){
        JSONObject jsonObject = new JSONObject();
        byte[] parsedBytes = null;
        String iDataString = new String(iDataBytes, StandardCharsets.UTF_8);
        String[] parsedStrings = iDataString.split("&");
        for(String s : parsedStrings){
            String parameter[] = s.split("=");
            if((parameter != null) && (parameter.length == 2) && parameter[0].equals(PASSWD_KEY)){
                parsedBytes = parameter[1].getBytes();
                break;
            }
        }

        return parsedBytes;
    }

    private void addResponseHeader(HttpExchange httpExchange, String type){

        switch(type) {
            case "json":
                httpExchange.getResponseHeaders().set(HEADER_CONTENT_KEY, HEADER_CONTENT_VALUE_JSON);
                break;
            default:
                httpExchange.getResponseHeaders().set(HEADER_CONTENT_KEY, HEADER_CONTENT_VALUE_HTML);

        }

    }

    private void handleError(HttpExchange httpExchange, String error) throws IOException{
        String errorString = "";
        int httpError = 0;
        byte[] oDataBytes = null;
        switch(error){
            case "Method Not Allowed":
                errorString = "<h1>405 Method Not Allowed</h1>";
                httpError = 405;
                break;
            case "Not Found":
                errorString = "<h1>404 Not Found</h1>";
                httpError = 404;
                break;
            case "Bad Request":
                errorString = "<h1>400 Bad Request</h1>";
                httpError = 400;
                break;
            default:
                errorString = "<h1>500 Internal Server Error</h1>";
                httpError = 500;
        }

        oDataBytes = errorString.getBytes();
        addResponseHeader(httpExchange, "html");
        httpExchange.sendResponseHeaders(httpError, oDataBytes.length);
        OutputStream oDataStream = httpExchange.getResponseBody();
        oDataStream.write(oDataBytes);

    }

    private boolean validateContentLength(int size, Map<String, String> propertiesMap){

        int maxLenBytes = Integer.parseInt(propertiesMap.getOrDefault(MAX_INPUT_KEY, MAX_INPUT_SIZE));
        if(size > (maxLenBytes + 9)){
            return false;
        }
        return true;
    }
}
