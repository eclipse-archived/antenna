/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class XmlSettingsReaderTest {
        
    private static final String XML_ROOT_TAG = "rootTag";
    private static final String XML_CHILD_TAG = "childTag";
    private static final String XML_GRAND_CHILD_TAG = "grandChildTag";
    private static final String PARAM_TAG = "param";
    
    private static final String STRING_TAG = "stringTag";
    private static final String INT_TAG = "intTag"; 
    private static final String BOOL_TAG = "boolTag";
    private static final String COMPLEX_TAG = "boolean";
    private static final String BOOL_MAP_TAG_1 = "boolMapKeyTag1";
    private static final String BOOL_MAP_TAG_2 = "boolMapKeyTag2";    
    
    private static final String DEFAULT_STRING = "defaultStringValue";
    private static final int DEFAULT_INT = 0;
    private static final boolean DEFAULT_BOOL = false;
    
    
    private String stringValue = "testStringValue";
    private int intValue = 2000;
    private boolean booleanValue = true;
    
    private XmlSettingsReader xsr;
    
    static String prepareElement(String tag, String value) {
        return "<" + tag + ">" + value + "</" + tag + ">";
    }
    
    
    @Test 
    public void readSimpleStringProperty() throws Exception {
        String simpleStringXml = prepareElement(STRING_TAG, stringValue);
        xsr = new XmlSettingsReader(simpleStringXml);
        
        String readStringProperty = xsr.getStringProperty(STRING_TAG);
        
        assertEquals(stringValue, readStringProperty);
    }
    
    @Test
    public void readSimpleIntProperty() throws Exception {
        String simpleIntXml = prepareElement(INT_TAG, String.valueOf(intValue));
        xsr = new XmlSettingsReader(simpleIntXml);
        
        int readIntProperty = xsr.getIntProperty("/", INT_TAG, DEFAULT_INT);
        
        assertEquals(intValue, readIntProperty);
    }
    
    @Test
    public void readSimpleBooleanProperty() throws Exception {
        String simpleBooleanXml = prepareElement(BOOL_TAG, String.valueOf(booleanValue));
        xsr = new XmlSettingsReader(simpleBooleanXml);
        
        boolean readBooleanProperty = xsr.getBooleanProperty("/", BOOL_TAG, DEFAULT_BOOL);
        
        assertEquals(booleanValue, readBooleanProperty);
    }
    
    @Test
    public void readStringListProperty() throws Exception {
        String stringItem1 = prepareElement(PARAM_TAG, stringValue);
        String stringItem2 = prepareElement(PARAM_TAG, DEFAULT_STRING);
        String stringListXml = prepareElement(STRING_TAG, stringItem1 + stringItem2);
        xsr = new XmlSettingsReader(stringListXml);
        
        List<String> readStringListProperty = xsr.getStringListProperty(STRING_TAG);
        
        assertEquals(2, readStringListProperty.size());
        assertTrue(readStringListProperty
                .stream()
                .anyMatch(e -> e.equals(stringValue)));
        assertTrue(readStringListProperty
                .stream()
                .anyMatch(e -> e.equals(DEFAULT_STRING)));
    }
    
    @Test
    public void readStringKeyedBooleanMapProperty() throws Exception {
        String booleanItem1 = prepareElement(BOOL_MAP_TAG_1, String.valueOf(booleanValue));
        String booleanItem2 = prepareElement(BOOL_MAP_TAG_2, String.valueOf(DEFAULT_BOOL));
        String booleanMapXml =  prepareElement(BOOL_TAG, booleanItem1 + booleanItem2); 
        xsr = new XmlSettingsReader(booleanMapXml);
        
        Map<String, Boolean> readBooleanMapProperty = xsr.getStringKeyedBooleanMapProperty(BOOL_TAG);
        
        assertEquals(2, readBooleanMapProperty.keySet().size());
        assertEquals(booleanValue, readBooleanMapProperty
                .values()
                .stream()
                .anyMatch(e -> e == booleanValue));
        assertEquals(booleanValue, readBooleanMapProperty
                .values()
                .stream()
                .anyMatch(e -> e == DEFAULT_BOOL));
    }
    
    @Test
    public void readComplexTypeProperty() throws Exception {
        String complexXml = prepareElement(COMPLEX_TAG, String.valueOf(booleanValue));
        xsr = new XmlSettingsReader(complexXml);
        
        Boolean readComplexProperty = xsr.getComplexType(COMPLEX_TAG, Boolean.class);
        
        assertEquals(booleanValue, readComplexProperty);
    }
    
    @Test
    public void readValueByXPathExp() throws Exception {
        String simpleDefaultStringElement = prepareElement(STRING_TAG, DEFAULT_STRING);
        String simpleStringElement = prepareElement(STRING_TAG, stringValue);
        String grandChildXml = prepareElement(XML_GRAND_CHILD_TAG, simpleStringElement);
        String childXml = prepareElement(XML_CHILD_TAG, grandChildXml);
        String rootXml = prepareElement(XML_ROOT_TAG, simpleDefaultStringElement + childXml);
        xsr = new XmlSettingsReader(rootXml);

        
        String xPath = "descendant::" + XML_GRAND_CHILD_TAG;
        String readStringPropertyByXPath = xsr.getStringPropertyByXPath(xPath, STRING_TAG, DEFAULT_STRING);
        
        assertNotEquals(stringValue, DEFAULT_INT);
        assertEquals(stringValue, readStringPropertyByXPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void readValueByXPathExpWithException() throws Exception {
        String simpleDefaultStringElement = prepareElement(STRING_TAG, DEFAULT_STRING);
        String simpleStringElement = prepareElement(STRING_TAG, stringValue);
        String grandChildXml = prepareElement(XML_GRAND_CHILD_TAG, simpleStringElement);
        String childXml = prepareElement(XML_CHILD_TAG, grandChildXml);
        String rootXml = prepareElement(XML_ROOT_TAG, simpleDefaultStringElement + childXml);
        xsr = new XmlSettingsReader(rootXml);

        String incorrectXPathExp = "::anyWrongExp";
        String readStringPropertyByXPath = xsr.getStringPropertyByXPath(incorrectXPathExp, STRING_TAG, DEFAULT_STRING);
    }
}
