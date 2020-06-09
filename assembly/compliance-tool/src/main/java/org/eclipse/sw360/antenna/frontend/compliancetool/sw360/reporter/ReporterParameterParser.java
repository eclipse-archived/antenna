/*
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.reporter;

import org.eclipse.sw360.antenna.frontend.stub.cli.AbstractAntennaCLIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class responsible for parsing status reporter parameters
 * and creating the {@code InfoRequest} the status reporter
 * uses.
 */
class ReporterParameterParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReporterParameterParser.class);

    /**
     * The long prefix to identify a parameter indication the desired info parameter of the reporter.
     */
    static final String REPORTER_PARAMETER_PREFIX = AbstractAntennaCLIOptions.SWITCH_PREFIX + "-info";

    /**
     * The long parameter prefix to determine the output format of the reporter
     */
    static final String OUTPUT_FORMAT_PREFIX_LONG = AbstractAntennaCLIOptions.SWITCH_PREFIX + "-output";

    /**
     * The short parameter prefix to determine the output format of the reporter
     */
    static final String OUTPUT_FORMAT_PREFIX_SHORT = AbstractAntennaCLIOptions.SWITCH_PREFIX + "o";

    /**
     * The default output format for the reporter output
     */
    static final String DEFAULT_OUTPUT_FORMAT = "CSV";

    /**
     * Creates a string representing the info parameter of an{@link InfoRequest}
     *
     * @param parameters set of parameters to be parsed
     * @return parsed {@code InfoRequest} with parsed additional parameters
     * @throws IllegalArgumentException when invalid or misconfigured parameters are given or are missing.
     */
    static String getInfoParameterFromParameters(Map<String, String> parameters) {
        return parameters.get(REPORTER_PARAMETER_PREFIX);
    }

    /**
     * Parses the parameter value of a parameter key from a set of parameters.
     *
     * @param parameters   Set of parameters searched for parameter key
     * @param parameterKey parameter key of the parameter key value pair
     * @return parameter value of the parameter key if contained in parameter set, otherwise null
     */
    static String parseParameterValueFromMapOfParameters(Map<String, String> parameters, String parameterKey) {
        return parameters.get(parameterKey);
    }

    /**
     * Returns the parameter value of the output format key
     *
     * @param parameters Set of parameters that are searched for the output format parameter
     * @return String of the output format parameter value
     */
    static String getOutputFormat(Map<String, String> parameters) {
        final String outputLong = parameters.get(OUTPUT_FORMAT_PREFIX_LONG);
        if (outputLong != null) {
            return outputLong;
        } else {
            return Optional.ofNullable(parameters.get(OUTPUT_FORMAT_PREFIX_SHORT))
                    .orElseGet(() -> {
                        LOGGER.warn("No output format is given.");
                        return DEFAULT_OUTPUT_FORMAT;
                    });
        }
    }

    /**
     * Maps a set of parameters with the key being the parameter key
     * and the value the parameter value. The Strings are split by the
     * {@code AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER}
     *
     * @param parameters set of strings representing parameters
     * @return Map of parameter key value pairs
     * @throws {@code IllegalStateException} by default if set collected
     *                to map contains a duplicate key
     */
    static Map<String, String> mapParameters(Set<String> parameters) {
        return parameters.stream()
                .map(p -> p.split(AbstractAntennaCLIOptions.PARAMETER_IDENTIFIER))
                .filter(sa -> {
                    if (sa.length != 2) {
                        throw new IllegalArgumentException(Arrays.toString(sa) + " does not have the proper format.");
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toMap(sa -> sa[0], sa -> sa[1]));
    }

    /**
     * Checks if the given set is either null or empty
     *
     * @param parameters set of strings representing parameters
     * @throws {@code IllegalArgumentException} if set is empty
     */
    static void checkParameters(Set<String> parameters) {
        Objects.requireNonNull(parameters, "Parameters must not be null");
        if (parameters.isEmpty()) {
            LOGGER.error("No parameters provided for the reporter reporter.");
            LOGGER.info(InfoRequestFactory.helpMessage());
            throw new IllegalArgumentException("No parameters provided for the reporter reporter.");
        }
    }
}
