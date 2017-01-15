package com.guido.actualtests;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.mule.util.PropertiesUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Actual actualtests implementations reused both by xIT (failsafe) and xTest (surefire) tests
 */
public class Tests {

    private static final Logger log = LoggerFactory.getLogger(Tests.class);

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

    public static void testAppHttpInterface() throws IOException {

        log.info("STARTED!");

        String url = String.format(
                "http://%s:%s/%s/%s",
                props.getProperty("com.guido.host"),
                props.getProperty("com.guido.port"),
                props.getProperty("com.guido.basepath"),
                props.getProperty("com.guido.path")
        );

        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = HttpClients.createDefault().execute(httpGet);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        String responseBody = EntityUtils.toString(response.getEntity());
        assertThat(responseBody, equalTo(props.getProperty("com.guido.payload")));

        log.info("RECEIVED: {}", responseBody);
    }

}
