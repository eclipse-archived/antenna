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

package org.eclipse.sw360.antenna.configuration;

import org.apache.commons.lang.Validate;
import org.eclipse.sw360.antenna.api.IConfigReader;
import org.eclipse.sw360.antenna.api.IXMLValidator;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.xml.generated.AntennaConfig;
import org.eclipse.sw360.antenna.xml.XMLResolverJaxB;
import org.eclipse.sw360.antenna.xml.XMLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Reads and validates the configuration.xml and creates a Configuration with
 * the values of the xml file. ConfigurationReader
 */
public class ConfigurationReader implements IConfigReader {

    private static final String CONFIG_XML = "config.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReader.class);
    private URL configXsdURL;
    private XMLResolverJaxB xmlResolver;
    private IXMLValidator xmlValidator;

    /**
     * A configuration reader gets a config.xml as URI or as file object. The
     * xml file is validated against the config.xsd of Antenna.
     *
     * @param xsdURL
     *            Schema definition file used to validate configuration file.
     * @param encoding
     *            Encoding of the content referenced by the URL
     */
    public ConfigurationReader(URL xsdURL, Charset encoding) {
        this.configXsdURL = xsdURL;
        xmlResolver = new XMLResolverJaxB(encoding);
        xmlValidator = new XMLValidator();
    }

    public ConfigurationReader(Charset encoding) {
        this(AntennaConfig.getConfigXsd(), encoding);
    }

    @Override
    public Configuration readConfigFromUri(URI configFileUri, Path antennaTargetDirectory) {
        Validate.notNull(configFileUri, "Configured config uri is null!");

        LOGGER.info("Validate config file against xsd.");
        File configFile = resolveConfigUriAndGetFile(configFileUri, antennaTargetDirectory);
        return checkAndParseConfigXML(configFile, configXsdURL);

    }

    @Override
    public Configuration readConfigFromFile(File configFile, Path antennaTargetDirectory) {
        Validate.isTrue(configFile.exists(), "Configured config file '" + configFile + "' does not exist!");
        Validate.isTrue(configFile.isFile(), "Configured config file '" + configFile + "' must be a file!");

        LOGGER.info("Validate config file against xsd.");
        return checkAndParseConfigXML(configFile, configXsdURL);
    }

    private File resolveConfigUriAndGetFile(URI configFileUri, Path antennaTargetDirectory) {
        File configFromUri;
        if (configFileUri.getScheme().contains("file")) {
            configFromUri = new File(configFileUri);
        } else {
            Path destinationPath = antennaTargetDirectory.resolve(CONFIG_XML);
            configFromUri = destinationPath.toFile();
            try {
                boolean alreadyExisted = configFromUri.createNewFile();
                if(alreadyExisted){
                    LOGGER.info("Destination file already existed, continuing by overwriting the file.");
                }
                LOGGER.debug("Copy configuration file to target folder of antenna.");
                org.apache.commons.io.FileUtils.copyURLToFile(configFileUri.toURL(), configFromUri);
            } catch (IOException e) {
                throw new ConfigurationException("Failed to fetch file to target folder of antenna.", e);
            }
        }
        return configFromUri;
    }

    private Configuration checkAndParseConfigXML(File xmlFile, URL configXsdURL) {
        xmlValidator.validateXML(xmlFile, configXsdURL);

        AntennaConfig config = xmlResolver.resolveXML(xmlFile);
        return new Configuration(config);
    }

}
