package com.security;


import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);

    private static final String DELAY_KEY = "responseDelaySec";
    private static final String DEFAULT_DELAY = "5";

    public static void main( String[] args )
    {

        Properties properties = PropertiesProvider.getProperties();
        int delay = Integer.parseInt(properties.getProperty(DELAY_KEY, DEFAULT_DELAY));

        LOGGER.info("Starting server...");
        AppServerProvider.getHttpServer().start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down server gracefully...");
            AppServerProvider.getHttpServer().stop(delay);
        }));
    }
}
