package com.guido.tck;

import com.guido.actualtests.BigXMLFileGenerator;
import com.guido.actualtests.TestProperties;
import com.guido.actualtests.Tests;

import org.junit.BeforeClass;
import org.junit.Test;

import org.mule.tck.junit4.FunctionalTestCase;

import java.io.IOException;

import static com.guido.actualtests.Constants.*;

public class UploadAndDumpTest extends FunctionalTestCase {

    @BeforeClass
    public static void fixtureSetup() throws IOException {

        new BigXMLFileGenerator().generateBigXmlFile();

        System.setProperty("mule.test.timeoutSecs", String.valueOf("12000"));
        System.setProperty(PROPERTIES_FILENAME_PROPERTY, PROPERTIES_FILE_PATH);
    }

    @Test
    public void testUpload() throws IOException, InterruptedException {

        Tests.uploadFile(TestProperties.getProperty(DUMP_HTTP_PATH_PROPERTY));

    }

    @Test
    public void testUploadParallel() throws IOException, InterruptedException {

        Tests.uploadFileParallel(TestProperties.getProperty(DUMP_HTTP_PATH_PROPERTY));

    }

    @Override
    protected String[] getConfigFiles() {
        return new String[]{APP_SPRING_XML, APP_HTTP_XML, APP_ACQU_DUMP_XML};
    }
}
