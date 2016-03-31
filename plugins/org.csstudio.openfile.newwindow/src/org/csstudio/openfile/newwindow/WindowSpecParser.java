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

/**
 * Parse the information from the .nws XML file into a WindowSpec object.
 */
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

    /**
     * Parse the XML and interpret it to create a WindowSpec object.
     * @return WindowSpec object
     * @throws WindowManagementException if parsing fails
     */
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

    /**
     * Retrieve the text content from a single tag found as a descendant of element.
     * Unless exactly one such tag is found, throw an exception.
     * @param element to search from
     * @param tagName name of tag to find
     * @return text content of found tag
     * @throws WindowManagementException unless precisely one element is found
     */
    private String getSingleNamedChildText(Element element, String tagName) throws WindowManagementException {
        Element namedChild = getSingleMandatoryNamedChild(element, tagName);
        return namedChild.getFirstChild().getTextContent();
    }

    /**
     * Return the single sub-element with the specified name.
     * @param element to search from
     * @param tagName name of tag to find
     * @return found element
     * @throws WindowManagementException unless precisely one matching element is found
     */
    private Element getSingleMandatoryNamedChild(Element element, String tagName) throws WindowManagementException {
        Optional<Element> maybeElement = getSingleOptionalNamedChild(element, tagName);
        if (!maybeElement.isPresent()) {
            throw new WindowManagementException("aa");
        }
        return maybeElement.get();
    }

    /**
     * Return the single sub-element with the specified name if present.
     * @param element to search from
     * @param tagName name of tag to find
     * @return optional containing element
     * @throws WindowManagementException if more than one matching element is found
     */
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

    /**
     * Return a list of all sub-elements matching the tag name.  As
     * element.getElementsByTagName(), but only returns Element objects.
     * @param element to search from
     * @param tagName name of tag to find
     * @return list of matching elements
     */
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
