package com.guido.tck;

import com.guido.actualtests.Tests;
import com.guido.actualtests.Constants;

import org.junit.BeforeClass;
import org.junit.Test;

import org.mule.tck.junit4.FunctionalTestCase;

import java.io.IOException;


/**
 * ReliabilityPatternMuleTest
 */
public class ReliabilityPatternMuleTest extends FunctionalTestCase {

    @BeforeClass
    public static void fixtureSetup() {
        System.setProperty("mule.test.timeoutSecs", String.valueOf("120"));
        System.setProperty(Constants.PROPERTIES_FILENAME_PROPERTY, Constants.PROPERTIES_FILE_PATH);
    }

    @Test
    public void testAppHttpInterfaceSync() throws IOException, InterruptedException {

        Tests.sendHttpRequestsToSync();

    }

    @Test
    public void testAppHttpInterfaceNonblocking() throws IOException, InterruptedException {

        Tests.sendHttpRequestsToNonblocking();

    }

    @Override
    protected String[] getConfigFiles() {
        return new String[] {
                Constants.APP_COSUMER_XML,
                Constants.APP_SPRING_XML,
                Constants.APP_VM_XML,
                Constants.APP_HTTP_XML,
                Constants.APP_SYNC_ACQUISITION_XML,
                Constants.APP_NONB_ACQUISITION_XML
        };
    }
}
