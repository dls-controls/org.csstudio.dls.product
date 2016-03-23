package org.csstudio.openfile.beamlines;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.csstudio.openfile.newwindow.WindowSpec;
import org.csstudio.openfile.newwindow.WindowSpecParser;
import org.csstudio.openfile.newwindow.WindowManagementException;
import org.junit.Before;
import org.junit.Test;

public class WindowSpecParserTest {

    private WindowSpecParser simpleParser;
    private WindowSpecParser linksParser;
    private String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private String emptyXmlString = xmlHeader + "<windowSpec></windowSpec>";
    private String perspectiveId = "hello";
    private String perspectiveFile = "/tmp/perspective_hello.xmi";

    @Before
    public void setUp() throws WindowManagementException {
        Map<String, String> linksMap = new HashMap<>();
        String simpleXml = createXml("hello", "/tmp/perspective_hello.xmi", linksMap);
        InputStream simpleStream = new ByteArrayInputStream(simpleXml.getBytes());
        simpleParser = new WindowSpecParser(simpleStream);
        linksMap.put("/a/b", "/c/d");
        String linksXml = createXml(perspectiveId, perspectiveFile, linksMap);
        InputStream linksStream = new ByteArrayInputStream(linksXml.getBytes());
        linksParser = new WindowSpecParser(linksStream);
    }

    private String createXml(String id, String file, Map<String, String> links) {
        StringBuilder builder = new StringBuilder(xmlHeader);
        builder.append("<windowSpec>");
        builder.append("<perspectiveId>" + id + "</perspectiveId>");
        builder.append("<perspectiveFile>" + file + "</perspectiveFile>");
        builder.append("<links>");
        for (String key : links.keySet()) {
            builder.append("<link>");
            builder.append("<eclipsePath>" + key + "</eclipsePath>");
            builder.append("<filePath>" + links.get(key) + "</filePath>");
            builder.append("</link>");
        }
        builder.append("</links>");
        builder.append("</windowSpec>");
        return builder.toString();
    }

    @Test(expected=WindowManagementException.class)
    public void emptyXmlThrowsException() throws WindowManagementException {
        InputStream is = new ByteArrayInputStream(emptyXmlString.getBytes());
        new WindowSpecParser(is);
    }

    @Test
    public void simpleXmlFetchesPerspectiveIdAndFile() throws WindowManagementException {
        WindowSpec spec = simpleParser.get();
        assertEquals(perspectiveId, spec.getPerspectiveId());
        assertEquals(perspectiveFile, spec.getPerspectiveFile());
    }

    @Test
    public void linksXmlFetchesPerspectiveId() throws WindowManagementException {
        WindowSpec spec = linksParser.get();
        assertEquals("hello", spec.getPerspectiveId());
    }

    @Test
    public void linksXmlFetchesLinkData() throws WindowManagementException {
        WindowSpec spec = linksParser.get();
        Map<String, String> result = new HashMap<>();
        result.put("/a/b", "/c/d");
        assertEquals(result, spec.getLinks());
    }

}
