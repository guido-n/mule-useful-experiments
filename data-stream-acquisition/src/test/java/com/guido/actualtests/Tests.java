package com.guido.actualtests;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.guido.actualtests.Constants.*;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Actual test implementations reused both by xIT (failsafe) and xTest (surefire) tests
 */
public class Tests {

    private static final Logger log = LoggerFactory.getLogger(Tests.class);

    private static void doUpload(CloseableHttpClient client, String path) throws IOException {

        String url = String.format(
                "http://%s:%s/%s",
                TestProperties.getProperty("com.guido.host"),
                TestProperties.getProperty("com.guido.port"),
                path
        );

        InputStreamEntity requestEntity = new InputStreamEntity(
                new FileInputStream(BIG_XML_FILE_PATH), -1,
                ContentType.APPLICATION_OCTET_STREAM);

        // set chunked transfer encoding ie. no Content-length
        requestEntity.setChunked(true);

        HttpPost httpPost = new HttpPost((url));
        httpPost.setEntity(requestEntity);
        HttpResponse response = client.execute(httpPost);

        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

    }

    /**
     * Upload a BIG_XML_FILE_PATH file via HTTP
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void uploadFile(String path) throws IOException, InterruptedException {

        log.info("FILE UPLOAD: {}", BIG_XML_FILE_PATH);

        doUpload(HttpClients.createDefault(), path);

        log.warn("RECEIVED RESPONSE");

        Thread.sleep(Long.parseLong(TestProperties.getProperty("com.guido.test.upload.waitafter")));
    }

    /**
     * Upload several BIG_XML_FILE_PATH files via HTTP in parallel
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void uploadFileParallel(String path) throws IOException, InterruptedException {

        int parallelism = Integer.parseInt(TestProperties.getProperty("com.guido.test.upload.parallelism"));
        int clientPoolSize = parallelism / 2;

        ExecutorService pool = Executors.newFixedThreadPool(parallelism);
        CountDownLatch doneSignal = new CountDownLatch(parallelism);

        ArrayList<CloseableHttpClient> clientsPool = new ArrayList<>();
        IntStream.rangeClosed(1, clientPoolSize).forEach(i -> clientsPool.add(HttpClients.createDefault()));

        IntStream.rangeClosed(1, parallelism)
                .forEach(i -> {
                    pool.submit(() -> {
                        try {
                            doUpload(clientsPool.get(i % clientPoolSize), path);
                            doneSignal.countDown();
                        } catch (Exception e) {
                            log.error("EXCEPTION", e);
                        }
                    });
                });

        log.info("ALL FILE UPLOAD TASKS SUBMITTED");

        doneSignal.await();

        log.warn("ALL UPLOADS DONE, RESPONSES RECEIVED");

        clientsPool
                .stream()
                .forEach(
                        client -> {
                            try {
                                client.close();
                            } catch (IOException e) {
                                log.error("EXCEPTION on client.close()", e);
                            }
                        });

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);

        Thread.sleep(Long.parseLong(TestProperties.getProperty("com.guido.test.upload.waitafter")));
    }
}
