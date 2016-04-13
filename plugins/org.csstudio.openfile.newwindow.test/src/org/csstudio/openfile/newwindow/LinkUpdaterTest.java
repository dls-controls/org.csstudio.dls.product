package org.csstudio.openfile.newwindow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.openfile.newwindow.LinkUpdater;
import org.junit.Before;
import org.junit.Test;

public class LinkUpdaterTest {

    private Map<String, String> empty;
    private Map<String, String> oneLink;
    private Map<String, String> twoLinks;

    @Before
    public void setUp() {
        empty = new HashMap<>();
        oneLink = new HashMap<>();
        oneLink.put("/a/b", "/c/d");
        twoLinks = new HashMap<>(oneLink);
        twoLinks.put("x", "y");
    }

    @Test
    public void testGetLinksStringReturnsAnEmptyStringIfNoLinks() {
        LinkUpdater updater = new LinkUpdater(empty);
        assertEquals("", updater.getLinksString());
    }

    @Test
    public void testGetLinksStringWithOneLink() {
        LinkUpdater updater = new LinkUpdater(oneLink);
        assertEquals("/a/b=/c/d", updater.getLinksString());
    }

    @Test
    public void testGetLinksStringWithTwoLinks() {
        LinkUpdater updater = new LinkUpdater(twoLinks);
        // The order of link output is unspecified.
        assertTrue(updater.getLinksString().contains("/a/b=/c/d"));
        assertTrue(updater.getLinksString().contains("x=y"));
        assertTrue(updater.getLinksString().contains(","));
    }

}
