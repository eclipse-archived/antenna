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
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.reporting.Report;

import java.io.OutputStream;

/**
 * Reporter for processing messages.
 */
public interface IProcessingReporter {

    static String getIdentifier() {
        return "antenna-report";
    }

    Report getProcessingReport();

    void add(ProcessingMessage msg);
    void add(MessageType type, String message);
    void add(String identifier, MessageType type, String message);
    void add(Artifact artifact, MessageType type, String message);
    void add(ArtifactIdentifier id, MessageType type, String message);

    /**
     * Writes the report to the given OutputStream.
     * 
     * @param stream
     */
    void writeReport(OutputStream stream) throws AntennaException;

    IAttachable writeReportToReportPath() throws AntennaExecutionException;
}
