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

package org.eclipse.sw360.antenna.report;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessageWithPayload;
import org.eclipse.sw360.antenna.model.reporting.Report;
import org.eclipse.sw360.antenna.api.Attachable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reporter for processing messages.
 */
public class Reporter implements IProcessingReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reporter.class);
    private static final String REPORT_FILENAME = "Antenna_3rdPartyAnalysisReport.txt";

    private static final String CLASSIFIER = "antenna-processing-report";
    private static final String TYPE = "txt";

    private Report report;
    private Path targetDirectory;
    private final Charset encoding;
    private final Path reportPath;

    public Reporter(Path targetDirectory) {
        this(targetDirectory, StandardCharsets.UTF_8);
    }

    /**
     * @param targetDirectory
     *            Directory to which the report will be written.
     */
    public Reporter(Path targetDirectory, Charset encoding) {
        this.targetDirectory = targetDirectory;
        this.report = new Report();
        this.encoding = encoding;
        this.reportPath = targetDirectory.resolve(REPORT_FILENAME);


        File fTargetDirectory  = targetDirectory.toFile();
        if (!fTargetDirectory.exists() && !fTargetDirectory.mkdirs()) {
            throw new AntennaExecutionException("Target directory: " + fTargetDirectory + " cannot be created.");
        }
    }

    @Override
    public Report getProcessingReport() {
        return this.report;
    }

    private void writeFormattedMessage(PrintStream stream, ProcessingMessage message){
        final String spacer = "    ";
        if(message.getIdentifier() != null) {
            stream.format("%s:\n%s%s: %s\n",
                    message.getMessageType(),
                    spacer,
                    message.getIdentifier(),
                    message.getMessage().replaceAll("\n", "\n" + spacer + spacer + spacer));
        } else {
            stream.format("%s:\n%s%s\n",
                    message.getMessageType(),
                    spacer,
                    message.getMessage().replaceAll("\n","\n" + spacer));

        }

        if (message instanceof ProcessingMessageWithPayload) {
            ((ProcessingMessageWithPayload) message).getPayload()
                    .forEach(payloadMsg -> stream.format("%s%s- %s",
                            spacer,
                            spacer,
                            payloadMsg.replaceAll("\n","\n" + spacer + spacer + "  ")));
        }
    }

    /**
     * Writes the report to the given OutputStream.
     *
     * @param out
     *            OutputStream to which the report will be written.
     */
    @Override
    public void writeReport(OutputStream out) {
        try (PrintStream stream = new PrintStream(out, false, encoding.toString())) {
            List<ProcessingMessage> msgList = report.getMessageList();
            synchronized (msgList) {
                msgList.forEach(msg -> writeFormattedMessage(stream, msg));
            }
        } catch (UnsupportedEncodingException e) {
            throw new AntennaExecutionException("Could not start printstream.", e);
        }
    }

    @Override
    public void add(MessageType type, String message) {
        ProcessingMessage msg = new ProcessingMessage(type, message);
        this.report.add(msg);
    }

    @Override
    public void add(String identifier, MessageType type, String message) {
        ProcessingMessage msg = new ProcessingMessage(type, identifier, message);
        this.report.add(msg);
    }

    @Override
    public void add(Artifact artifact, MessageType type, String message) {
        ProcessingMessage msg = new ProcessingMessage(type, artifact.toString(), message);
        this.report.add(msg);
    }

    /**
     * Creates a new ProcessingMessage and adds it to the report.
     *
     * @param id
     *            ArtifactIdentifier of the artifact to which the message refers
     *            to.
     * @param type
     *            MessageType that describes the content of the message.
     * @param message
     *            Message that describes the
     */
    @Override
    public void add(ArtifactIdentifier id, MessageType type, String message) {
        ProcessingMessage msg = new ProcessingMessage(type, id.toString(), message);
        this.report.add(msg);
    }

    /**
     * Adds the given ProcessingMessage to the report.
     *
     * @param msg
     *            ProcessingMessage, which is added to the report.
     */
    @Override
    public void add(ProcessingMessage msg) {
        this.report.add(msg);
    }

    /**
     * Creates the path to which the report will be written to.
     */
    private void createReportPath() throws IOException {
        Files.createDirectories(this.targetDirectory);
    }

    public IAttachable writeReportToReportPath() throws AntennaExecutionException {
        try {
            createReportPath();
            LOGGER.info("Writing report to {}", reportPath.toString());
            try (OutputStream out = new FileOutputStream(reportPath.toFile())) {
                writeReport(out);
            }
            return new Attachable(TYPE, CLASSIFIER, reportPath.toFile());
        } catch (IOException e) {
            throw new AntennaExecutionException("The processing report could not be created.", e);
        }
    }
}
