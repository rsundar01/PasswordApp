package com.security;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppServerProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServerProvider.class);

    private static Map<String, String> propertiesMap = null;
    private static HttpServer httpServer = null;

    // Defaults
    private static final String INETADDRESS = "localhost";
    private static final String PORT = "8080";
    private static final String NTHREADS = "1";
    private static final String TCP_BACKLOG = "0"; // Use system default


    // Properties
    private static final String INETADDRESS_KEY = "inetAddress";
    private static final String PORT_KEY = "port";
    private static final String NUM_THREADS_KEY = "nThreads";
    private static final String BACKLOG_KEY = "tcpbacklog";


    // Registered Paths
    private static final String SLASH_HASH = "/hash";
    private static final String SLASH_STATS = "/stats";

    private AppServerProvider(){}

    private static HttpServer configureHttpServer() {
        HttpServer httpServer = null;
        try {
            Properties properties = PropertiesProvider.getProperties();
            if(properties == null) properties = new Properties();
            propertiesMap = new HashMap<>((Map) properties);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(propertiesMap.getOrDefault(INETADDRESS_KEY, INETADDRESS),
                            Integer.parseInt(propertiesMap.getOrDefault(PORT_KEY, PORT)));
            LOGGER.info("Bound socket {} {}", inetSocketAddress.getHostString(), inetSocketAddress.getPort());

            httpServer = HttpServer.create(inetSocketAddress, Integer.parseInt(propertiesMap.getOrDefault(BACKLOG_KEY, TCP_BACKLOG)));
            LOGGER.info("Tcp back log {}", propertiesMap.getOrDefault(BACKLOG_KEY, TCP_BACKLOG));

            httpServer.setExecutor(Executors.newFixedThreadPool(Integer.parseInt(propertiesMap.getOrDefault(NUM_THREADS_KEY, NTHREADS))));
            LOGGER.info("num of server threads {}", propertiesMap.getOrDefault(NUM_THREADS_KEY, NTHREADS));

            httpServer.createContext(SLASH_HASH, HashRequestHandler.getInstance());
            httpServer.createContext(SLASH_STATS, HashRequestHandler.getInstance());

        } catch (IOException ioe) {
            LOGGER.error(ioe.getMessage());
            throw new RuntimeException(ioe.getMessage());
        }
        return httpServer;
    }

    public static synchronized HttpServer getHttpServer() {
        if(httpServer == null){
            LOGGER.info("Configuring server...");
            httpServer = configureHttpServer();
            LOGGER.info("Server configuration successful");

        }
        return httpServer;
    }

}
