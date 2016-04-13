package org.csstudio.archive.reader.fastarchiver.archive_requests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import org.csstudio.archive.reader.ValueIterator;
import org.csstudio.archive.reader.fastarchiver.exceptions.FADataNotAvailableException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FAArchivedDataRequestPerformanceTest {
    private Duration[] timeIntervals = new Duration[] {
            Duration.ofSeconds(10), Duration.ofSeconds(20),
            Duration.ofSeconds(30), Duration.ofSeconds(40),
            Duration.ofSeconds(50), Duration.ofSeconds(60),
            Duration.ofSeconds(90), Duration.ofSeconds(120),
            Duration.ofSeconds(300), Duration.ofSeconds(900),
            Duration.ofSeconds(1800), Duration.ofSeconds(3600),
            Duration.ofSeconds(10800), Duration.ofSeconds(21600),
            Duration.ofSeconds(43200), Duration.ofSeconds(86400),
            Duration.ofSeconds(259200), Duration.ofSeconds(518400),
            Duration.ofSeconds(1036800), Duration.ofSeconds(1382400) };

    private Duration[] timeIntervalsRaw = new Duration[] {
            Duration.ofSeconds(10), Duration.ofSeconds(20),
            Duration.ofSeconds(30), Duration.ofSeconds(40),
            Duration.ofSeconds(50), Duration.ofSeconds(60),
            Duration.ofSeconds(90), Duration.ofSeconds(120),
            Duration.ofSeconds(300), Duration.ofSeconds(900),
            Duration.ofSeconds(1800)};

    private static final String URL = "fads://fa-archiver:8888"; // specific to DLS
    private static String name;
    private static HashMap<String, int[]> mapping;
    private FAArchivedDataRequest faadr;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        mapping = new FAInfoRequest(URL).fetchMapping();
        name = (String)mapping.keySet().toArray()[0];
    }

    @Before
    public void setUp() throws Exception {
        faadr = new FAArchivedDataRequest(URL, mapping);
    }

    @After
    public void tearDown() throws Exception {
        faadr = null;
    }

    @Test
    public void testGetOptimisedValues() {
        Instant end;
        Instant start;
        int count = 8000;

        for (Duration timeInterval: timeIntervals){
            end = Instant.now();
            start = end.minus(timeInterval);

            ValueIterator result = null;
            long before = 0;
            long after = 0;

            try {
                before = System.nanoTime();
                result = faadr.getOptimisedValues(name, start, end, count);
                after = System.nanoTime();
            } catch (IOException | FADataNotAvailableException e) {
                fail("URL, name, and time should be valid");
                return;
            }

            System.out
                    .printf("'getOptimisedValues' with count = %d, time interval %d, takes %d nanoseconds\n",
                            count, timeInterval.getSeconds(), after - before);
            assertNotNull("Did not return data", result);
        }

    }

    @Test
    public void testGetRawValues() {
        Instant start;
        Instant end;

        for (Duration timeInterval : timeIntervalsRaw) {
            end = Instant.now();
            start = end.minus(timeInterval);

            ValueIterator result = null;
            long before = 0;
            long after = 0;

            try {
                before = System.nanoTime();
                result = faadr.getRawValues(name, start, end);
                after = System.nanoTime();
            } catch (IOException | FADataNotAvailableException e) {
                fail("URL, name, and time should be valid");
                return;
            }

            System.out.printf("for time interval %d 'getRawValues' takes %d nanoseconds\n", timeInterval.getSeconds(), after
                    - before);
            assertNotNull("Did not return data", result);
        }
    }

}
