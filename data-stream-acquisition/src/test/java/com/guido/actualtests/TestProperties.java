package com.guido.actualtests;

import org.mule.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * TestProperties
 */
public class TestProperties {

    private static final Logger log = LoggerFactory.getLogger(TestProperties.class);

    private static Properties props = loadTestingProperties();

    private static Properties loadTestingProperties() {

        try {
            return PropertiesUtils.loadProperties(
                    Tests.class.getResource(Constants.PROPERTIES_FILE_PATH).toURI().toURL()
            );
        } catch (Exception e) {
            log.error("could not load properties file: {}", e.getMessage(), e);
        }

        return new Properties();
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }
}
