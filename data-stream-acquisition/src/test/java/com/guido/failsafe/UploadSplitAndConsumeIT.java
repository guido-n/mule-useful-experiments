package com.guido.failsafe;

import com.guido.actualtests.BigXMLFileGenerator;
import com.guido.actualtests.TestProperties;
import com.guido.actualtests.Tests;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.guido.actualtests.Constants.HTTP_PATH_PROPERTY;

public class UploadSplitAndConsumeIT {

    @BeforeClass
    public static void fixtureSetup() throws IOException {

        new BigXMLFileGenerator().generateBigXmlFile();

    }

    @Test
    public void testUpload() throws IOException, InterruptedException {

        Tests.uploadFile(TestProperties.getProperty(HTTP_PATH_PROPERTY));

    }

    @Test
    public void testUploadParallel() throws IOException, InterruptedException {

        Tests.uploadFileParallel(TestProperties.getProperty(HTTP_PATH_PROPERTY));

    }

}
