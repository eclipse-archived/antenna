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
package org.eclipse.sw360.antenna.frontend.compliancetool.sw360.status;

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
    private InfoParameter infoParameter;
    private final String outputFormat;
    private ReporterOutput reporterOutput;

    public SW360StatusReporter(SW360Configuration configuration, Set<String> parameters) {
        this.configuration = Objects.requireNonNull(configuration, "Configuration must not be null");
        this.infoParameter =  SW360StatusReporterParameters
                .getInfoParameterFromParameters(Objects.requireNonNull(parameters, "Parameters must not be null"));
        this.outputFormat = SW360StatusReporterParameters.getOutputFormat(parameters);
        init();
    }

    private void init() {
        this.reporterOutput = ReporterOutputFactory.getReporterOutput(outputFormat);
    }

    void setInfoParameter(InfoParameter infoParameter) {
        this.infoParameter = infoParameter;
    }

    /**
     * Executes the execute function of the infoParameter and prints it
     * to a csv file.
     */
    public void execute() {
        LOGGER.debug("{} has started.", SW360StatusReporter.class.getName());
        final SW360Connection connection = configuration.getConnection();

        final Collection result = infoParameter.execute(connection);

        reporterOutput.printFile(result, configuration);
    }
}
