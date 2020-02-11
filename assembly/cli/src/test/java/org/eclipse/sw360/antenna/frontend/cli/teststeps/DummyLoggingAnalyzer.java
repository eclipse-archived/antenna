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
package org.eclipse.sw360.antenna.frontend.cli.teststeps;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractAnalyzer;
import org.eclipse.sw360.antenna.api.workflow.WorkflowStepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * A dummy analyzer implementation that is invoked during test Antenna runs.
 * <p>
 * This analyzer does nothing meaningful, but generates log messages on INFO
 * and DEBUG level. This can be used to check whether the log level of the
 * Antenna CLI can be configured dynamically.
 */
public class DummyLoggingAnalyzer extends AbstractAnalyzer {
    private static final String ANALYZER_NAME = "DummyLoggingAnalyzer";

    /**
     * The content of the message written with log level INFO. This string can
     * be searched for in the log output to check whether INFO logging is
     * enabled.
     */
    public static final String INFO_LOG_MESSAGE = generateLogMessage("Info");

    /**
     * The content of the message written with log level DEBUG. This string can
     * be searched for in the log output to check whether DEBUG logging is
     * enabled.
     */
    public static final String DEBUG_LOG_MESSAGE = generateLogMessage("Debug");

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyLoggingAnalyzer.class);

    @Override
    public String getName() {
        return ANALYZER_NAME;
    }

    @Override
    public WorkflowStepResult yield() throws ExecutionException {
        LOGGER.info(INFO_LOG_MESSAGE);
        LOGGER.debug(DEBUG_LOG_MESSAGE);
        return new WorkflowStepResult(Collections.emptyList());
    }

    /**
     * Generates a log message that includes the target log level.
     *
     * @param level the log level
     * @return the resulting message
     */
    private static String generateLogMessage(String level) {
        return String.format("%s log from %s.", level, ANALYZER_NAME);
    }
}
