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

package org.eclipse.sw360.antenna.knowledgebase.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseClassification;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseThreatGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This CSVBasedLicenseKnowledgeBase delivers maps for the mapping of: alias
 * to id, id to license and id to text etc.
 */
public class CSVBasedLicenseKnowledgeBase implements ILicenseManagementKnowledgeBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVBasedLicenseKnowledgeBase.class);

    private static final String LICENSES_CSV = "Licenses.csv";

    private static final String KEY_IDENT = "Identifier";
    private static final String KEY_ALIAS = "Aliases";
    private static final String KEY_NAME = "Name";
    private static final String KEY_URL = "LicenseURL";
    private static final String KEY_OSS = "OpenSource";
    private static final String KEY_DELIVER_SRC = "DeliverSources";
    private static final String KEY_DELIVER_LICENSE = "DeliverLicense";
    private static final String KEY_CLASSIFICATION = "CoveredByINSTStandardProcess";
    private static final String KEY_THREAT_GROUP = "ThreatGroup";
    private static final Collection<String> KEY_LIST = Arrays.asList(KEY_IDENT, KEY_ALIAS, KEY_NAME, KEY_URL, KEY_OSS, KEY_DELIVER_SRC, KEY_DELIVER_LICENSE, KEY_CLASSIFICATION, KEY_THREAT_GROUP);

    private final Map<String, String> aliasIdMap = new HashMap<>();
    private final Map<String, String> idLicenseMap = new HashMap<>();
    private final Map<String, String> idTextMap = new HashMap<>();
    private final Map<String, LicenseThreatGroup> idThreatGroupMap = new HashMap<>();
    private final Map<String, LicenseClassification> idClassificationMap = new HashMap<>();

    private final List<Consumer<CSVRecord>> mapperFunctions = new ArrayList<>();

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

        checkThatCSVIsOnClasspath();
        initMapperFunctions();
        initMaps();
    }

    private void checkThatCSVIsOnClasspath() {
        final URL resource = CSVBasedLicenseKnowledgeBase.class.getClassLoader().getResource(LICENSES_CSV);
        if(resource == null) {
            throw new ExecutionException("The required file " + LICENSES_CSV + " was not found on the classpath");
        }
    }

    private void initMapperFunctions() {
        // alias mapper function
        mapperFunctions.add(row -> {
            final String licenseId = row.get(KEY_IDENT);
            final String licenseName = row.get(KEY_NAME);
            final String licenseAliases = row.get(KEY_ALIAS);
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
        mapperFunctions.add(row -> this.idLicenseMap.put(row.get(KEY_IDENT), row.get(KEY_NAME)));

        // threat group mapper
        mapperFunctions.add(row -> {
            LicenseThreatGroup threatGroup = LicenseThreatGroup.UNKNOWN;

            String threadGroupString = row.get(KEY_THREAT_GROUP);
            if(threadGroupString != null && threadGroupString.length() > 0) {
                try {
                    threatGroup = LicenseThreatGroup.fromValue(threadGroupString);
                } catch (IllegalArgumentException e) {
                    String errMsg = String.format(
                            "Illegal threat group [%s] for license [%s]. Falling back to UNKNOWN",
                            threadGroupString, row.get(KEY_IDENT));
                    LOGGER.warn(errMsg);
                    reporter.add(row.get(KEY_IDENT), MessageType.PROCESSING_FAILURE, errMsg);
                }
            }

            this.idThreatGroupMap.put(row.get(KEY_IDENT), threatGroup);
        });

        // classification mapper
        mapperFunctions.add(row -> {
            LicenseClassification lc;
            try {
                lc = LicenseClassification.fromValue(row.get(KEY_CLASSIFICATION));
            } catch (IllegalArgumentException e) {
                String errMsg = String.format(
                        "Illegal classifier %s for licenses %s. Falling back to NOT_CLASSIFIED",
                        row.get(KEY_CLASSIFICATION), row.get(KEY_IDENT));
                LOGGER.warn(errMsg);
                reporter.add(row.get(KEY_IDENT), MessageType.PROCESSING_FAILURE, errMsg);
                lc = LicenseClassification.NOT_CLASSIFIED;
            }
            this.idClassificationMap.put(row.get(KEY_IDENT), lc);
        });
    }

    /**
     * Prepare Map idClassificationMap with license identifier as key and
     * classification as value
     */
    private void initMaps() {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader().withDelimiter(';').withQuote('"');


        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream iStream = Optional.ofNullable(classLoader.getResourceAsStream(LICENSES_CSV)).orElseThrow(() -> new ExecutionException("Knowledgebase not found"));
             Reader iReader = new InputStreamReader(iStream, encoding);
             CSVParser csvParser = new CSVParser(iReader, csvFormat)) {
            validateHeader(csvParser);
            for (CSVRecord row : csvParser) {
                mapperFunctions.stream()
                        .parallel()
                        .forEach(f -> f.accept(row));
            }
        } catch (IOException e) {
            throw new ExecutionException("Could not initialize knowledgebase maps", e);
        }
    }

    private void validateHeader(CSVParser csvParser) {
        Collection<String> headers = csvParser.getHeaderMap().keySet();
        List<String> missingHeaders = KEY_LIST.stream().filter(key -> !headers.contains(key)).collect(Collectors.toList());
        if (!missingHeaders.isEmpty()) {
            String errMsg = String.format("License knowledgebase malformed. Missing headers: %s", String.join(";", missingHeaders));
            LOGGER.error(errMsg);
            throw new ExecutionException(errMsg);
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
            throw new ExecutionException("An UnsupportedEncodingException was thrown while reading the license texts.", e);
        } catch (IOException e) {
            throw new ExecutionException("Unable to read licensetext in recources", e);
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
