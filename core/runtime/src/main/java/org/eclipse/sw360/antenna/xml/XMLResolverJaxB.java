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

package org.eclipse.sw360.antenna.xml;

import org.eclipse.sw360.antenna.api.IXMLResolver;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Creates an AntennaConfig with the values of the given configuration xml.
 */
public class XMLResolverJaxB implements IXMLResolver {
    private final Charset encoding;

    public XMLResolverJaxB(Charset encoding){
        this.encoding = encoding;
    }
    /**
     * @return Returns an AntennaConfig with the values of the given configuration
     *         xml. If the File is not found a ProcessingMessage is added to the
     *         Report.
     * @throws ConfigurationException
     *             If the file can not be resolved.
     * @param file
     *            File to be resolved.
     */
    @Override
    public AntennaConfig resolveXML(File file){
        try {
            JAXBContext context = JAXBContext.newInstance(AntennaConfig.class);
            Unmarshaller um = context.createUnmarshaller();
            return (AntennaConfig) um.unmarshal(new InputStreamReader(new FileInputStream(file), encoding));
        } catch (JAXBException e) {
            throw new ConfigurationException("The config.xml could not be resolved.", e);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Failed to read antenna config from file.", e);
        }

    }

}
