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

import org.eclipse.sw360.antenna.frontend.compliancetool.sw360.SW360Configuration;
import org.eclipse.sw360.antenna.sw360.client.adapter.SW360Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of a status reporter for the compliance tool.
 * It takes parameters given to the reporter mode and produces
 * a csv file with the information requested in its information
 * parameter
 */
public class SW360StatusReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360StatusReporter.class);

    private final SW360Configuration configuration;
    private final String infoParameter;
    private InfoRequest infoRequest;
    private final String outputFormat;
    private ReporterOutput reporterOutput;

    public SW360StatusReporter(SW360Configuration configuration, Set<String> parameters) {
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");

        Objects.requireNonNull(parameters, "Parameters must not be null");

        this.outputFormat = ReporterParameterParser.getOutputFormat(parameters);
        this.reporterOutput = ReporterOutputFactory.getReporterOutput(outputFormat);

        this.infoParameter = ReporterParameterParser.getInfoParameterFromParameters(parameters);
        this.infoRequest = InfoRequestFactory.getInfoRequestFromString(infoParameter);
        parseAdditionalParameters(parameters);
    }


    /**
     * Creates a parsed {@code InfoRequest} from a set of parameter
     * @param parameters set of parameters to be parsed
     */
    private void parseAdditionalParameters(Set<String> parameters) {
        if (Objects.equals(infoRequest, InfoRequest.emptyInfoRequest())) {
            throw new IllegalArgumentException(infoParameter + ": " + infoRequest.helpMessage());
        }

        if (infoRequest.hasAdditionalParameters()) {
            infoRequest.parseAdditionalParameter(parameters);
        } else if (parameters.size() < 1) {
            LOGGER.warn("You have provided additional parameters that are not necessary for the information parameter {}.", infoRequest.getInfoParameter());
        }

        if (!infoRequest.isValid()) {
            throw new IllegalStateException(
                    "The information parameter " + infoRequest.getInfoParameter() + " you requested does not have all parameters it needs." +
                            System.lineSeparator() + infoRequest.helpMessage());
        }
    }

    void setInfoRequest(InfoRequest infoRequest) {
        this.infoRequest = infoRequest;
    }

    /**
     * Executes the execute function of the infoRequest and prints it
     * to a csv file.
     */
    public void execute() {
        LOGGER.debug("{} has started.", SW360StatusReporter.class.getName());
        final SW360Connection connection = configuration.getConnection();

        final Collection result = infoRequest.execute(connection);

        reporterOutput.printFile(result, configuration);
    }
}
