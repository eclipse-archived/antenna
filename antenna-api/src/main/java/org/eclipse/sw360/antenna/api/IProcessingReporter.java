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

import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.reporting.Report;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;

import java.io.OutputStream;

/**
 * Reporter for processing messages.
 */
public interface IProcessingReporter {

    public static String getIdentifier() {
        return "antenna-report";
    }

    /**
     * 
     * @return Report of the reporter.
     */
    Report getProcessingReport();

    /**
     * Adds a new ProcessingMessage with the given parameters to the report.
     *
     * @param type
     *            MessageType that describes the content of the message.
     * @param message
     */
    void addProcessingMessage(MessageType type, String message);

    /**
     * Adds a new ProcessingMessage with the given parameters to the report.
     * 
     * @param id
     *            ArtifactIdentifier of the artifact to which the message refers
     *            to.
     * @param type
     *            MessageType that describes the content of the message.
     * @param message
     */
    void addProcessingMessage(ArtifactIdentifier id, MessageType type, String message);

    /**
     * Adds a new ProcessingMessage with the given parameters to the report.
     * 
     * @param licenseName
     * @param type
     * @param message
     */
    void add(String licenseName, MessageType type, String message);

    /**
     * Adds the given ProcessingMessage to the report.
     * 
     * @param msg
     */
    void add(ProcessingMessage msg);

    /**
     * Writes the report to the given OutputStream.
     * 
     * @param stream
     */
    void writeReport(OutputStream stream) throws AntennaException;

    IAttachable writeReportToReportPath() throws AntennaExecutionException;

    void add(String message, MessageType type);
}
