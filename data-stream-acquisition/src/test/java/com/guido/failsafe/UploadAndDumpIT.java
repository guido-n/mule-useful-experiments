package com.guido.failsafe;

import com.guido.actualtests.BigXMLFileGenerator;
import com.guido.actualtests.TestProperties;
import com.guido.actualtests.Tests;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.guido.actualtests.Constants.DUMP_HTTP_PATH_PROPERTY;

public class UploadAndDumpIT {

    @BeforeClass
    public static void fixtureSetup() throws IOException {

        new BigXMLFileGenerator().generateBigXmlFile();

    }

    @Test
    public void testUpload() throws IOException, InterruptedException {

        Tests.uploadFile(TestProperties.getProperty(DUMP_HTTP_PATH_PROPERTY));

    }

    @Test
    public void testUploadParallel() throws IOException, InterruptedException {

        Tests.uploadFileParallel(TestProperties.getProperty(DUMP_HTTP_PATH_PROPERTY));

    }

}
