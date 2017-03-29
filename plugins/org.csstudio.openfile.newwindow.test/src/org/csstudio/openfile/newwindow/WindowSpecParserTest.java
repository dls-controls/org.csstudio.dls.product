package org.csstudio.openfile.newwindow;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class WindowSpecParserTest {

    private String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private String helloId = "hello";
    private String helloFile = "/tmp/perspective_hello.xmi";
    private String byeId = "bye";
    private String byeFile = "/tmp/perspective_bye.xmi";

    private final List<String> noIds = new ArrayList<>();
    private final List<String> noFiles = new ArrayList<>();
    private final List<Map<String, String>> noLinks = new ArrayList<>();

    private String createXml(List<String> ids, List<String> files, List<Map<String, String>> linkMaps) {
        StringBuilder builder = new StringBuilder(xmlHeader);
        builder.append("<windowSpec>");
        for (String id : ids) {
            builder.append("<perspectiveId>" + id + "</perspectiveId>");
        }
        for (String file : files) {
            builder.append("<perspectiveFile>" + file + "</perspectiveFile>");
        }
        for (Map<String, String> linkMap : linkMaps) {
            builder.append("<links>");
            for (String key : linkMap.keySet()) {
                builder.append("<link>");
                builder.append("<filePath>" + key + "</filePath>");
                builder.append("<eclipsePath>" + linkMap.get(key) + "</eclipsePath>");
                builder.append("</link>");
            }
            builder.append("</links>");
        }
        builder.append("</windowSpec>");
        return builder.toString();
    }

    private List<String> createStringList(String... strings) {
        List<String> list = new ArrayList<>();
        for (String string : strings) {
            list.add(string);
        }
        return list;
    }

    private WindowSpecParser createParser(String xmlString) {
        InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
        return new WindowSpecParser(inputStream);
    }

    private List<Map<String, String>> createEmptyLinksMap() {
        List<Map<String, String>> linksMaps = new ArrayList<>();
        linksMaps.add(new HashMap<String, String>());
        return linksMaps;
    }

    private List<Map<String, String>> createLinksMap(int numLinks) {

        List<Map<String, String>> linksMaps = new ArrayList<>();
        for (int i = 0; i < numLinks; i++) {
            Map<String, String> links = new HashMap<>();
            links.put("/a/b" + i, "/c/d" + i);
            linksMaps.add(links);
        }
        return linksMaps;
    }

    @Test(expected=WindowManagementException.class)
    public void emptyXmlThrowsException() throws WindowManagementException {
        String emptyXmlString = createXml(noIds, noFiles, noLinks);
        InputStream is = new ByteArrayInputStream(emptyXmlString.getBytes());
        WindowSpecParser parser = new WindowSpecParser(is);

        parser.parse();
    }

    @Test(expected=WindowManagementException.class)
    public void xmlWithNoIdThrowsException() throws WindowManagementException {
        List<String> oneFile = createStringList(helloFile);
        String noIdXml = createXml(noIds, oneFile, noLinks);
        WindowSpecParser parser = createParser(noIdXml);

        parser.parse();
    }

    @Test(expected=WindowManagementException.class)
    public void xmlWithTwoPerspectiveIdsThrowsException() throws WindowManagementException {
        List<String> twoIds = createStringList(helloId, byeId);
        List<String> oneFile = createStringList(helloFile);
        String twoIdXml = createXml(twoIds, oneFile, noLinks);
        WindowSpecParser parser = createParser(twoIdXml);

        parser.parse();
    }

    @Test(expected=WindowManagementException.class)
    public void xmlWithTwoPerspectiveFilesThrowsException() throws WindowManagementException {
        List<String> twoIds = createStringList(helloId);
        List<String> oneFile = createStringList(helloFile, byeFile);
        String twoIdXml = createXml(twoIds, oneFile, noLinks);
        WindowSpecParser parser = createParser(twoIdXml);

        parser.parse();
    }

    public void xmlWithNoFileCreatesWindowSpec() throws WindowManagementException {
        List<String> oneId = createStringList(helloId);
        String noFileXml = createXml(oneId, noFiles, noLinks);
        WindowSpecParser parser = createParser(noFileXml);

        WindowSpec spec = parser.parse();
        assertEquals(helloId, spec.getPerspectiveId());
    }

    @Test
    public void simpleXmlFetchesPerspectiveIdAndFile() throws WindowManagementException {
        List<String> ids = createStringList(helloId);
        List<String> files = createStringList(helloFile);
        String simpleXmlString = createXml(ids, files, noLinks);
        WindowSpecParser simpleParser = createParser(simpleXmlString);

        WindowSpec spec = simpleParser.parse();


        assertEquals(helloFile, spec.getPerspectiveFile());
    }

    @Test
    public void linksXmlFetchesPerspectiveIdFileAndLinks() throws WindowManagementException {
        List<String> ids = createStringList(helloId);
        List<String> files = createStringList(helloFile);
        List<Map<String, String>> linksMap = createLinksMap(1);
        String linksXml = createXml(ids, files, linksMap);

        WindowSpecParser parser = createParser(linksXml);
        WindowSpec spec = parser.parse();

        assertEquals(helloId, spec.getPerspectiveId());
        assertEquals(helloFile, spec.getPerspectiveFile());
        assertEquals(linksMap.get(0), spec.getLinks());
    }

    @Test
    public void emptyLinksXmlFetchesPerspectiveIdFileAndLinks() throws WindowManagementException {
        List<String> ids = createStringList(helloId);
        List<String> files = createStringList(helloFile);
        List<Map<String, String>> linksMap = createEmptyLinksMap();
        String linksXml = createXml(ids, files, linksMap);

        WindowSpecParser parser = createParser(linksXml);
        WindowSpec spec = parser.parse();

        assertEquals(helloId, spec.getPerspectiveId());
        assertEquals(helloFile, spec.getPerspectiveFile());
        assertTrue(spec.getLinks().isEmpty());
    }

    @Test(expected=WindowManagementException.class)
    public void twolinksXmlRaisesException() throws WindowManagementException {
        List<String> ids = createStringList(helloId);
        List<String> files = createStringList(helloFile);
        List<Map<String, String>> linksMap = createLinksMap(2);
        String linksXml = createXml(ids, files, linksMap);

        WindowSpecParser parser = createParser(linksXml);

        parser.parse();
    }


}
