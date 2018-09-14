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

package org.eclipse.sw360.antenna.knowledgebase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;

import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseClassification;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jdk.nashorn.internal.runtime.Context;
import org.apache.commons.io.IOUtils;

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This CSVBasedLicenseKnowledgeBase delivers maps for the mapping of: alias
 * to id, id to license and id to text etc.
 */
public class CSVBasedLicenseKnowledgeBase implements ILicenseManagementKnowledgeBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVBasedLicenseKnowledgeBase.class);

    private static final String licensesCSV = "Licenses.csv";

    // number of columns in the license table
    private static final int NUMCOLS = 9;
    private static final int KEY_IDENT = 0;
    private static final int KEY_ALIAS = 1;
    private static final int KEY_NAME = 2;
    private static final int KEY_URL = 3;
    private static final int KEY_OSS = 4;
    private static final int KEY_DELIVER_SRC = 5;
    private static final int KEY_DELIVER_LICENSE = 6;
    private static final int KEY_CLASSIFICATION = 7;
    private static final int KEY_THREAT_GROUP = 8;

    private final Map<String, String> aliasIdMap = new HashMap<>();
    private final Map<String, String> idLicenseMap = new HashMap<>();
    private final Map<String, String> idTextMap = new HashMap<>();
    private final Map<String, LicenseThreatGroup> idThreatGroupMap = new HashMap<>();
    private final Map<String, LicenseClassification> idClassificationMap = new HashMap<>();

    private final List<Consumer<String[]>> mapperFunctions = new ArrayList<>();

    private IProcessingReporter reporter;
    private Charset encoding;

    /**
     * This CSVBasedLicenseKnowledgeBase delivers maps for the mapping of:
     * alias to id, id to license and id to text.
     */
    @Override
    public void init(IProcessingReporter reporter, Charset encoding) {
        this.reporter = reporter;
        this.encoding = encoding;

        checkThatCSVIsOnClasspath(licensesCSV);
        initMapperFunctions();
        initMaps(licensesCSV);
    }

    private void checkThatCSVIsOnClasspath(String licensesCSV) {
        final URL resource = CSVBasedLicenseKnowledgeBase.class.getClassLoader().getResource(licensesCSV);
        if(resource == null) {
            throw new RuntimeException("The required file " + licensesCSV + " was not found on the classpath");
        }
    }

    private void initMapperFunctions() {
        // alias mapper function
        mapperFunctions.add(row -> {
            final String licenseId = row[KEY_IDENT];
            final String licenseName = row[KEY_NAME];
            final String licenseAliases = row[KEY_ALIAS];
            if (!licenseAliases.equals("")) {
                String[] aliases = licenseAliases.split(",");
                Arrays.stream(aliases)
                        .filter(a -> !a.equals(""))
                        .forEach(a -> aliasIdMap.put(a.trim(), licenseId));
            }
            // alias the id by its license's name as well
            aliasIdMap.put(licenseName.trim(), licenseId);
        });

        // id mapper function
        mapperFunctions.add(row -> this.idLicenseMap.put(row[KEY_IDENT], row[KEY_NAME]));

        // threat group mapper
        mapperFunctions.add(row -> {
            LicenseThreatGroup threatGroup = LicenseThreatGroup.UNKNOWN;

            String threadGroupString = row[KEY_THREAT_GROUP];
            if(threadGroupString != null && threadGroupString.length() > 0) {
                try {
                    threatGroup = LicenseThreatGroup.fromValue(threadGroupString);
                } catch (IllegalArgumentException e) {
                    String errMsg = String.format(
                            "Illegal threat group [%s] for license [%s]. Falling back to UNKNOWN",
                            threadGroupString, row[KEY_IDENT]);
                    LOGGER.warn(errMsg);
                    reporter.add(row[KEY_IDENT], MessageType.PROCESSING_FAILURE, errMsg);
                }
            }

            this.idThreatGroupMap.put(row[KEY_IDENT], threatGroup);
        });

        // classification mapper
        mapperFunctions.add(row -> {
            LicenseClassification lc;
            try {
                lc = LicenseClassification.fromValue(row[KEY_CLASSIFICATION]);
            } catch (IllegalArgumentException e) {
                String errMsg = String.format(
                        "Illegal classifier %s for licenses %s. Falling back to NOT_CLASSIFIED",
                        row[KEY_CLASSIFICATION], row[KEY_IDENT]);
                LOGGER.warn(errMsg);
                reporter.add(row[KEY_IDENT], MessageType.PROCESSING_FAILURE, errMsg);
                lc = LicenseClassification.NOT_CLASSIFIED;
            }
            this.idClassificationMap.put(row[KEY_IDENT], lc);
        });
    }

    /**
     * Prepare Map idClassificationMap with license identifier as key and
     * classification as value
     * @param licensesCSV
     */
    private void initMaps(String licensesCSV) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream iStream = classLoader.getResourceAsStream(licensesCSV);
             Reader iReader = new InputStreamReader(iStream, encoding)) {
            CSVParserBuilder parserBuilder = new CSVParserBuilder()
                    .withQuoteChar('"')
                    .withSeparator(';');
            CSVReaderBuilder readerBuilder = new CSVReaderBuilder(iReader)
                    .withSkipLines(0)
                    .withCSVParser(parserBuilder.build());
            CSVReader reader = readerBuilder.build();

            String[] headers = reader.readNext();
            if (headers.length != NUMCOLS) {
                String errMsg = String.format("License knowledgebase malformed. %d rows expected but %d found.", NUMCOLS, headers.length);
                LOGGER.error(errMsg);
                LOGGER.error("Found headers {}", String.join("; ", headers));
                throw new AntennaExecutionException(errMsg);
            }

            String[] cols;
            while (null != (cols = reader.readNext())) {
                // init maps for current license
                final String[] row = cols;
                mapperFunctions.stream()
                        .parallel()
                        .forEach(f -> f.accept(row));
            }

        } catch (IOException e) {
            throw new AntennaExecutionException("Could not initialize knowledgebase maps", e);
        }
    }

    /**
     * Loads the license text belonging to the id from the database, if it
     * exists.
     *
     * @param id Id of the license for which the text will be loaded
     * @return null if the text does not exist, the text otherwise.
     */
    private String loadLicenseText(String id) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try(InputStream iStream = classLoader.getResourceAsStream("licenses/" + id + ".txt")) {
            if(iStream == null) {
                reporter.add(id, MessageType.MISSING_LICENSE_TEXT, "No license text found ");
                return null;
            }
            return IOUtils.toString(iStream, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("An UnsupportedEncodingException was thrown while reading the license texts.", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read licensetext in recources", e);
        }
    }

    /**
     * @param licenseAlias for which the Id will be returned
     * @return LicenseId which belongs to the licenseAlias.
     */
    @Override
    public String getLicenseIdForAlias(String licenseAlias) {
        return this.aliasIdMap.get(licenseAlias);
    }

    /**
     * @param licenseId Id of the license which will be mapped to the corresponding
     *                  name
     * @return LicenseName which belongs to the licenseId.
     */
    @Override
    public String getLicenseNameForId(String licenseId) {
        return this.idLicenseMap.get(licenseId);
    }

    @Override
    public String getTextForId(String licenseId) {
        String text = idTextMap.get(licenseId);
        if (null == text) {
            text = loadLicenseText(licenseId);
            if (null != text) {
                this.idTextMap.put(licenseId, text);
            }
        }
        return text;
    }

    @Override
    public LicenseThreatGroup getThreatGroupForId(String id) {
        return this.idThreatGroupMap.get(id);
    }

    @Override
    public LicenseClassification getClassificationById(String licenseId) {
        Optional<LicenseClassification> classification = Optional.ofNullable(this.idClassificationMap.get(licenseId));
        return classification.orElse(LicenseClassification.NOT_CLASSIFIED);
    }
}
