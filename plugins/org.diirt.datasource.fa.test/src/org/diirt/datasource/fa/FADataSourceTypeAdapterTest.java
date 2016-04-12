package org.diirt.datasource.fa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.csstudio.archive.reader.fastarchiver.archive_requests.FALiveDataRequest;
import org.csstudio.archive.vtype.ArchiveVDisplayType;
import org.csstudio.archive.vtype.ArchiveVNumber;
import org.diirt.datasource.ValueCache;
import org.diirt.datasource.ValueCacheImpl;
import org.diirt.vtype.AlarmSeverity;
import org.diirt.vtype.VType;
import org.junit.Before;
import org.junit.Test;

public class FADataSourceTypeAdapterTest {
    private FADataSourceTypeAdapter faDSAdapType;
    private static String URL = "fads://fa-archiver:8888";
    private FALiveDataRequest connection;

    @Before
    public void setUp() throws Exception {
        faDSAdapType = new FADataSourceTypeAdapter();
        connection = new FALiveDataRequest(URL,    4, 0);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testMatchValidClass() throws Exception{
        Class[] validClasses = new Class[]{Object.class, VType.class, ArchiveVDisplayType.class};
        for (Class clazz : validClasses) {
            ValueCache cache = new ValueCacheImpl(clazz);
            int result = faDSAdapType.match(cache, connection);
            assertEquals(1, result);
        }
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testMatchInvalidClass() throws Exception {
        Class[] validClasses = new Class[]{Exception.class, ArchiveVNumber.class};
        for (Class clazz : validClasses) {
            ValueCache cache = new ValueCacheImpl(clazz);
            int result = faDSAdapType.match(cache, new FALiveDataRequest(URL,
                    4, 0));
            assertEquals(0, result);
        }
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testGetSubscriptionParameter() {
        ValueCache<ArchiveVDisplayType> cache = new ValueCacheImpl<ArchiveVDisplayType>(ArchiveVDisplayType.class);
        faDSAdapType.getSubscriptionParameter(cache, connection);
    }

    @Test
    public void testUpdateCacheOfValidType() {
        ValueCache<ArchiveVDisplayType> cache = new ValueCacheImpl<ArchiveVDisplayType>(ArchiveVDisplayType.class);
        ArchiveVNumber newValue = new ArchiveVNumber(Instant.now(), AlarmSeverity.NONE, "status", null, 5);
        assertTrue(faDSAdapType.updateCache(cache, connection, newValue));
        assertEquals(cache.readValue(), newValue);
    }

    @Test
    public void testUpdateCacheOfInvalidType() {
        ValueCache<Exception> cache = new ValueCacheImpl<Exception>(Exception.class);
        ArchiveVNumber newValue = new ArchiveVNumber(Instant.now(), AlarmSeverity.NONE, "status", null, 5);
        assertFalse(faDSAdapType.updateCache(cache, connection, newValue));
        assertNull(cache.readValue());
    }

}
