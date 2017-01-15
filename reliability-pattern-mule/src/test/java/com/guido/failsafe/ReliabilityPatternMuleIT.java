package com.guido.failsafe;

import com.guido.actualtests.Tests;

import org.junit.Test;

import java.io.IOException;

/**
 * ReliabilityPatternMuleIT
 */
public class ReliabilityPatternMuleIT {

    @Test
    public void testAppHttpInterfaceSync() throws IOException, InterruptedException {

        Tests.sendHttpRequestsToSync();

    }

    @Test
    public void testAppHttpInterfaceNonblocking() throws IOException, InterruptedException {

        Tests.sendHttpRequestsToNonblocking();

    }
}
