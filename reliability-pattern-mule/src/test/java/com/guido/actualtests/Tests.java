package com.guido.actualtests;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import org.mule.util.PropertiesUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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

    public static void sendHttpRequestsToSync() throws IOException, InterruptedException {
        sendHttpRequests(props.getProperty("com.guido.acquisition.synchronous.path"));
    }

    public static void sendHttpRequestsToNonblocking() throws IOException, InterruptedException {
        sendHttpRequests(props.getProperty("com.guido.acquisition.nonblocking.path"));
    }

    private static void sendHttpRequests(String path) throws IOException, InterruptedException {

        String url = String.format(
                "http://%s:%s/%s",
                props.getProperty("com.guido.host"),
                props.getProperty("com.guido.port"),
                path
        );

        int parallelism = 32;
        int clientPoolSize = parallelism / 2;
        int requests = 2048;

        ArrayList<CloseableHttpClient> clientsPool = new ArrayList<>();
        IntStream.rangeClosed(1, clientPoolSize).forEach(i -> clientsPool.add(HttpClients.createDefault()));

        ExecutorService pool = Executors.newFixedThreadPool(parallelism);
        CountDownLatch doneSignal = new CountDownLatch(requests);

        IntStream.rangeClosed(1, requests)
                .forEach( i -> {
                    pool.submit( () -> {
                        try {
                            String body = String.format("MESSAGE%05d", i);

                            HttpPost httpPost = new HttpPost((url));
                            httpPost.setEntity(new StringEntity(body));

                            HttpResponse response = clientsPool.get(i % clientPoolSize).execute(httpPost);
                            assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

                            String responseBody = EntityUtils.toString(response.getEntity());
                            assertThat(responseBody, equalTo(body));

                            doneSignal.countDown();

                        } catch (Exception e) {
                            log.error("EXCEPTION", e);
                        }
                    });
                });

        log.warn("ALL TASKS SUBMITTED");

        doneSignal.await();

        log.warn("ALL REQUESTS SENT");

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

        Thread.sleep(16000);

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.SECONDS);
    }

}
