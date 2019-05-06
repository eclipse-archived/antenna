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

package org.eclipse.sw360.antenna.api;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Validates a xml file against a xsd file.
 */
public abstract class IXMLValidator {
    /**
     * Returns true if the xml can be validated against the xsd.
     * 
     * @param xmlFile
     *            XML to be validated.
     * @param xsdUrl
     *            XSD against which the xml will be validated.
     *
     * @throws AntennaConfigurationException
     */
    public abstract void validateXML(File xmlFile, URL xsdUrl) throws AntennaConfigurationException;

    public void validateXML(URL xmlUrl, URL xsdUrl) throws AntennaConfigurationException {
        try {
            validateXML(new File(xmlUrl.toURI()), xsdUrl);
        } catch (URISyntaxException e) {
            throw new AntennaConfigurationException("Failed to convert URL=["+xmlUrl+"] to File", e);
        }
    }
}
