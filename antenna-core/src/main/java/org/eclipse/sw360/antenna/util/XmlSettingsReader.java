/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * Convenience class for getting typed values from an XML file.
 */
public class XmlSettingsReader {

    private Document doc;
    private XPath xpath;

    /**
     * Creates a new reader.
     *
     * @param xmlString
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public XmlSettingsReader(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder dBuilder = initDocumentBuilder();
        this.doc = dBuilder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
        doc.getDocumentElement().normalize();
        xpath = initXPath();
    }

    private DocumentBuilder initDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        return dbFactory.newDocumentBuilder();
    }
    
    private XPath initXPath() {
        return XPathFactory.newInstance().newXPath();
    }

    /**
     * Gets the first integer with the matching tag.
     *
     * @param tagName
     *            Element name of the int.
     * @param defaultVal
     *            What to return if element not found.
     * @return First integer with the matching tag or default value.
     */
    public int getIntProperty(String xPathExp, String tagName, int defaultVal) {
        return getElementByXPath(xPathExp, tagName, node -> {
            try {
                return Integer.parseInt(node.getTextContent());
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }, defaultVal);
    }

    public String getStringPropertyByXPath(String xPathExp, String tagName) {
        return getStringPropertyByXPath(xPathExp, tagName, null);

    }

    public String getStringPropertyByXPath(String xPathExp, String tagName, String defaultVal) {
        return getElementByXPath(xPathExp, tagName, n -> n.getTextContent().trim(), defaultVal);
    }

    /**
     * Gets the first boolean with the matching tag.
     *
     * @param tagName
     *            Element name of the boolean.
     * @param defaultVal
     *            What to return if element not found.
     * @return First boolean with the matching tag (returns true if the element
     *         value is "true", otherwise false) or default value.
     */
    public boolean getBooleanProperty(String xPathExp, String tagName, boolean defaultVal) {
        return getElementByXPath(xPathExp, tagName, node -> {
            try {
                return Boolean.parseBoolean(node.getTextContent());
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }, defaultVal);
    }

    /**
     * Gets the first string with the matching tag.
     *
     * @param tagName
     *            Element name of the string.
     * @return First string with the matching tag, or null if not found.
     */
    public String getStringProperty(String tagName) {
        return this.getStringProperty(tagName, null);
    }

    public File getFileProperty(String tagName) {
        return Optional.ofNullable(getStringProperty(tagName, null))
                .map(File::new)
                .orElse(null);
    }

    /**
     * Gets the first string with the matching tag.
     *
     * @param tagName
     *            Element name of the string.
     * @return First string with the matching tag, or default value if not
     *         found.
     */
    public String getStringProperty(String tagName, String defaultValue) {
        Node property = getElement(doc, tagName);

        if (property == null) {
            return defaultValue;
        }

        if (property.getNodeType() == Node.ELEMENT_NODE) {
            return property.getTextContent().trim();
        }

        return null;
    }
    
    /**
     * Gets a list of strings from the children of the first matching tag.
     *
     * @param tagName
     *            Element name of the parent.
     * @return First matching list of strings from the children, or an empty
     *         list if not found.
     */
    public List<String> getStringListProperty(String tagName) {
        List<String> propertyStrings = new ArrayList<>();
        NodeList propertyList = doc.getElementsByTagName(tagName);

        if (propertyList.getLength() == 0) {
            return propertyStrings;
        }

        if (propertyList.item(0).getNodeType() == Node.ELEMENT_NODE) {
            Element parentElement = (Element) propertyList.item(0);

            NodeList childProperties = parentElement.getElementsByTagName("param");

            for (int i = 0; i < childProperties.getLength(); i++) {
                propertyStrings.add(childProperties.item(i).getTextContent().trim());
            }
        }

        return propertyStrings;
    }

    public Map<String, Boolean> getStringKeyedBooleanMapProperty(String tagName) {
        return getStringKeyedMapProperty(tagName, Boolean::parseBoolean);
    }

    public <T> Map<String, T> getStringKeyedMapProperty(String tagName, Function<String, T> mapFunction) {
        NodeList propertyList = doc.getElementsByTagName(tagName);

        if (propertyList.getLength() == 0 || propertyList.item(0).getNodeType() != Node.ELEMENT_NODE) {
            return Collections.emptyMap();
        }

        Element parentElement = (Element) propertyList.item(0);
        NodeList childProperties = parentElement.getChildNodes();

        Map<String,T> resultMap = new HashMap<>();
        for (int i = 0; i < childProperties.getLength(); i++) {
            resultMap.put(childProperties.item(i).getNodeName(),
                    mapFunction.apply(childProperties.item(i).getTextContent().trim()));
        }
        return resultMap;
    }

    /**
     * Creates an object that is based on an XSD complex type.
     *
     * @param tagName
     *            The root element of the complex type.
     * @param targetClass
     *            The JAXB-generated class that represents this complex type.
     * @return An object extracted from the XML settings file.
     */
    public <T> T getComplexType(String tagName, Class<T> targetClass) {
        Node property = getElement(doc, tagName);

        if (property == null) {
            return null;
        }

        try {
            JAXBContext jc = JAXBContext.newInstance(targetClass);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<T> jb = unmarshaller.unmarshal(property, targetClass);
            return jb.getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException(
                    "Problem extracting " + targetClass + " from settings: " + e.getMessage());
        }
    }

    /**
     * Gets the first element matching the tagName, or null if no element
     * matches.
     */
    private Node getElement(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);

        if (nodes.getLength() == 0) {
            return null;
        }

        return nodes.item(0);
    }

    private <T> T getElementByXPath(String xPathExp, String tagName, Function<Node,T> callback, T defaultValue) {
        Optional<Node> property = getElementByXPath(xPathExp, tagName);

        if (property.isPresent()) {
            Node node = property.get();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return callback.apply(node);
            }
        }

        return defaultValue;
    }

    private Optional<Node> getElementByXPath(String xPathExp, String tagName) {
        String xPathExpWithTag = xPathExp + "/" + tagName;
        try {
            XPathExpression exp = xpath.compile(xPathExp + "/" + tagName);
            return Optional.ofNullable((Node) exp.evaluate(doc, XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Could not evaluate XPath expression: " + xPathExpWithTag, e);
        }
    }
}
