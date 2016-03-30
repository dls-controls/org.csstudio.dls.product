package org.csstudio.openfile.newwindow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WindowSpecParser {

    private static final String perspectiveIdTag = "perspectiveId";
    private static final String perspectiveFileTag = "perspectiveFile";
    private static final String linksTag = "links";
    private static final String linkTag = "link";
    private static final String eclipsePathTag = "eclipsePath";
    private static final String filePathTag = "filePath";

    private InputStream inputStream = null;

    public WindowSpecParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public WindowSpec parse() throws WindowManagementException {
        WindowSpec spec = new WindowSpec();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            Element root = doc.getDocumentElement();

            String perspectiveId = getSingleNamedChildText(root, perspectiveIdTag);
            spec.setPerspectiveId(perspectiveId);

            String perspectiveFile = getSingleNamedChildText(root, perspectiveFileTag);
            spec.setPerspectiveFile(perspectiveFile);

            Optional<Element> linksElement = getSingleOptionalNamedChild(root, linksTag);
            if (linksElement.isPresent()) {
                List<Element> links = getNamedChildren(linksElement.get(), linkTag);
                for (Element link : links) {
                    String filePath = getSingleNamedChildText(link, filePathTag);
                    String eclipsePath = getSingleNamedChildText(link, eclipsePathTag);
                    spec.addLink(filePath, eclipsePath);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new WindowManagementException(e);
        }
        return spec;
    }

    private String getSingleNamedChildText(Element element, String tagName) throws WindowManagementException {
        Element namedChild = getSingleMandatoryNamedChild(element, tagName);
        return namedChild.getFirstChild().getTextContent();
    }

    private Element getSingleMandatoryNamedChild(Element element, String tagName) throws WindowManagementException {
        Optional<Element> maybeElement = getSingleOptionalNamedChild(element, tagName);
        if (!maybeElement.isPresent()) {
            throw new WindowManagementException("aa");
        }
        return maybeElement.get();
    }

    private Optional<Element> getSingleOptionalNamedChild(Element element, String tagName) throws WindowManagementException {
        List<Element> namedElements = getNamedChildren(element, tagName);
        if (namedElements.size() > 1) {
            throw new WindowManagementException("Expected exactly one child of " + element.getTagName() + " with name " + tagName);
        }
        if (namedElements.size() == 0)
            return Optional.empty();
        else
            return Optional.of(namedElements.get(0));
    }

    private List<Element> getNamedChildren(Element element, String tagName) {
        List<Element> elements = new ArrayList<>();
        NodeList nodes = element.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                elements.add((Element) node);
            }
        }
        return elements;
    }

}
