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

/**
 * Factory class for creating a {@link ReporterOutput} implementation
 */
class ReporterOutputFactory {
    static final ReporterOutput DEFAULT_REPORTER_OUTPUT = new ReporterOutputCSV();

    /**
     * create function to create an object implementing the {@link ReporterOutput}
     *
     * @param outputFormat String representation of the type of the object
     * @return {@link ReporterOutput} of the output format
     */
    static ReporterOutput getReporterOutput(String outputFormat) {
        if (outputFormat.equalsIgnoreCase("csv")) {
            return new ReporterOutputCSV();
        }
        return DEFAULT_REPORTER_OUTPUT;
    }
}
