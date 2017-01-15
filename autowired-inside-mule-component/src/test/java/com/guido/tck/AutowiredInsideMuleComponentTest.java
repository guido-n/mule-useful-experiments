package com.guido.tck;

import com.guido.actualtests.Tests;
import com.guido.actualtests.Constants;

import org.junit.BeforeClass;
import org.junit.Test;

import org.mule.tck.junit4.FunctionalTestCase;

import java.io.IOException;


/**
 * AutowiredInsideMuleComponentTest
 */
public class AutowiredInsideMuleComponentTest extends FunctionalTestCase {

    @BeforeClass
    public static void fixtureSetup() {
        System.setProperty(Constants.PROPERTIES_FILENAME_PROPERTY, Constants.PROPERTIES_FILE_PATH);
    }

    @Test
    public void testAppHttpInterface() throws IOException {

        Tests.testAppHttpInterface();

    }

    @Override
    protected String[] getConfigFiles() {
        return new String[] {Constants.APP_XML, Constants.APP_SPRING_XML};
    }
}
