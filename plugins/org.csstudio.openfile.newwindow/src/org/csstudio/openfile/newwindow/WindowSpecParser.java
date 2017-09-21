package org.csstudio.openfile.newwindow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parse the information from the .nws XML file into a WindowSpec object.
 */
public class WindowSpecParser {

    private static final String PERSPECTIVE_ID_TAG = "perspectiveId";
    private static final String PERSPECTIVE_FILE_TAG = "perspectiveFile";
    private static final String LINKS_TAG = "links";
    private static final String LINK_TAG = "link";
    private static final String ECLIPSE_PATH_TAG = "eclipsePath";
    private static final String FILE_PATH_TAG = "filePath";

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

            String perspectiveId = getSingleNamedChildText(root, PERSPECTIVE_ID_TAG);
            spec.setPerspectiveId(perspectiveId);

            Optional<Element> perspectiveFileElement = getSingleOptionalNamedChild(root, PERSPECTIVE_FILE_TAG);
            if (perspectiveFileElement.isPresent()) {
                String perspectiveFile = perspectiveFileElement.get().getTextContent();
                spec.setPerspectiveFile(perspectiveFile);
            }

            Optional<Element> linksElement = getSingleOptionalNamedChild(root, LINKS_TAG);
            if (linksElement.isPresent()) {
                List<Element> links = getNamedChildren(linksElement.get(), LINK_TAG);
                for (Element link : links) {
                    String filePath = getSingleNamedChildText(link, FILE_PATH_TAG);
                    String eclipsePath = getSingleNamedChildText(link, ECLIPSE_PATH_TAG);
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
            throw new WindowManagementException(NLS.bind(Messages.WindowSpecParser_noChild, element.getTagName(), tagName));
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
            throw new WindowManagementException(NLS.bind(Messages.WindowSpecParser_tooManyElements, element.getTagName(), tagName));
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
