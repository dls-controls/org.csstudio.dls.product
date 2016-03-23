package org.csstudio.openfile.newwindow;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WindowSpecParser {

    private static final String perspectiveId = "perspectiveId";
    private static final String perspectiveFile = "perspectiveFile";
    private static final String linksTag = "links";
    private static final String linkTag = "link";
    private static final String eclipsePathTag = "eclipsePath";
    private static final String filePathTag = "filePath";

    private WindowSpec windowSpec = null;

    public WindowSpecParser(InputStream inputStream) throws WindowManagementException {
        WindowSpec spec = new WindowSpec();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(inputStream);
            Element doc = dom.getDocumentElement();
            String pid = getTextValue(doc, perspectiveId);
            if (pid == null) {
                throw new WindowManagementException("PerspectiveId is required.");
            }
            spec.setPerspectiveId(pid);
            spec.setPerspectiveFile(getTextValue(doc, perspectiveFile));
            NodeList linksElements = dom.getElementsByTagName(linksTag);
            Node linksElement = linksElements.item(0);
            if (linksElement != null) {
                spec.setPerspectiveId(pid);
                NodeList linkList = linksElement.getChildNodes();
                System.out.println("There are " + linkList.getLength() + " element in the linklist");
                for (int i = 0; i < linkList.getLength(); i++) {
                    if (linkList.item(i).getNodeName().equals(linkTag)) {
                        System.out.println(linkList.item(i).getNodeType());
                        String eclipsePath = getTextValue(((Element) linkList.item(i)), eclipsePathTag);
                        String filePath = getTextValue(((Element) linkList.item(i)), filePathTag);
                        spec.addLink(filePath, eclipsePath);
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new WindowManagementException(e);
        }
        windowSpec = spec;
    }

    public WindowSpec get() {
        return windowSpec;
    }

    private String getTextValue(Element doc, String tag) {
        String value = null;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

}
