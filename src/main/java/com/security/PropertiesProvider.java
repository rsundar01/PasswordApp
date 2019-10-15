package com.security;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServerProvider.class);

    private static Properties properties = null;
    public static final String APP_PROPERTIES = "application.properties";

    private PropertiesProvider(){}

    public static Properties getProperties(){
        if(properties == null) {
            synchronized (PropertiesProvider.class) {
                if(properties == null) {
                    properties = new Properties();
                    try {
                        properties.load(AppServerProvider.class.getClassLoader()
                                .getResource(APP_PROPERTIES)
                                .openStream());
                    }catch(IOException ioe){
                        LOGGER.error(ioe.getMessage());
                        properties = null;
                    }
                }
            }
        }
        return properties;
    }

}
