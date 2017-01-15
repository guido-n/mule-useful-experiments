package com.guido.actualtests;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

/**
 * BigXMLFileGenerator
 */
public class BigXMLFileGenerator {

    /**
     * Generate a BIG xml file: temporary/bigfile.xml
     */
    @Test
    @Ignore
    public void generateBigXmlFile() throws IOException {

        // make temporary/ dir if it doesn't exist
        Path temporaryPath = Paths.get(Constants.TMP_PATH);
        if (!Files.exists(temporaryPath)) {
            Files.createDirectory(temporaryPath);
        }

        // dump loads of xml in temporary/bigfile.xml
        BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(Constants.BIG_XML_FILE_PATH));
        PrintWriter printWriter = new PrintWriter(bufferedWriter);
        printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        printWriter.println("<rootelement>");

        IntStream
                .rangeClosed(1, Integer.parseInt(TestProperties.getProperty("com.guido.test.upload.bigfile")))
                .forEach(i -> printWriter.println( String.format("<childelement id=\"%d\">HELLO%10d</childelement>", i, i) ));

        printWriter.println("</rootelement>");
        printWriter.flush();
        printWriter.close();

    }
}
