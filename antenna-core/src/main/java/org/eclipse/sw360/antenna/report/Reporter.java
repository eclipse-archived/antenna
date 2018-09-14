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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.reporting.IncompleteSourcesFailure;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.reporting.Report;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.repository.Attachable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reporter for processing messages.
 */
public class Reporter implements IProcessingReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Reporter.class);
    private static final String REPORT_FILENAME = "Antenna_3rdPartyAnalysisReport.txt";

    private static final String CLASSIFIER = "antenna-processing-report";
    private static final String TYPE = "txt";
    private static final String UNKNOWN = "<unknown>";

    private Report report;
    private Path targetDirectory;
    private final Charset encoding;
    private final Path reportPath;

    public Reporter(Path targetDirectory) {
        this(targetDirectory, Charset.forName("UTF-8"));
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

    private void writeFormattedLine(PrintStream stream, MessageType messageType, String filename, String hash, String groupId, ProcessingMessage message) {
        writeFormattedLine(stream, "[" + messageType + "]",
                filename, hash,
                groupId, artifactIdExtractor(message),
                versionExtractor(message), message.getMessage());

    }

    private void writeFormattedLine(PrintStream stream, String messageType, String filename, String hash, String groupId, String runnerId, String runnerVersion, String message) {
        stream.format("%-30s\t%-85s\t%-22s\t%-30s\t%-55s\t%-20s\t%s%n", messageType,
                filename, hash, groupId, runnerId, runnerVersion, message);
    }

    private void writeFormattedLine(PrintStream stream, ProcessingMessage message){
        if (message.getIdentifier().isPresent()){
            ArtifactIdentifier artifactIdentifier = message.getIdentifier().get();
            writeFormattedLine(stream, message.getMessageType(), artifactIdentifier.getFilename(),
                    artifactIdentifier.getHash(),
                    artifactIdentifier.getMavenCoordinates().getGroupId(), message);
        }else{
            stream.format("%-30s\t%-30s\t%s%n", message.getMessageType(), message.getLicenseName(),
                    message.getMessage());
        }
    }

    private void writeMissingSourcesToStream(PrintStream stream, IncompleteSourcesFailure iFailure) {
        List<String> missingSources = iFailure.getMissingSources();
        for (String missingSource : missingSources) {
            stream.println("\t" + missingSource);
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
        PrintStream stream;
        try {
            stream = new PrintStream(out, false, encoding.toString());
        } catch (UnsupportedEncodingException e) {
            throw new AntennaExecutionException("Could not start printstream.", e);
        }

        writeFormattedLine(stream, "message type", "filename", "hash", "group Id", "artifact Id", "version", "message");

        List<ProcessingMessage> msgList = report.getMessageList();
        synchronized (msgList) {
            for (ProcessingMessage message : msgList) {
                writeFormattedLine(stream, message);

                if (message instanceof IncompleteSourcesFailure) {
                    writeMissingSourcesToStream(stream, (IncompleteSourcesFailure) message);
                }
            }
        }
    }

    @Override
    public void addProcessingMessage(MessageType type, String message) {
        ProcessingMessage msg = new ProcessingMessage(type);
        msg.setMessage(message);
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
    public void addProcessingMessage(ArtifactIdentifier id, MessageType type, String message) {
        ProcessingMessage msg = new ProcessingMessage(type);
        msg.setIdentifier(id);
        msg.setMessage(message);
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
     * Creates a new ProcessingMessage and adds it to the report.
     *
     * @param licenseName
     *            Name of the license to which the message refers to.
     * @param type
     *            Describes the content of the message.
     * @param message
     *            Message that describes the cause of the ProcessingMessage.
     */

    @Override
    public void add(String licenseName, MessageType type, String message) {
        ProcessingMessage msg = new ProcessingMessage(type);
        msg.setLicenseName(licenseName);
        msg.setMessage(message);
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

    private String genericExtractor(ProcessingMessage message, Function<ArtifactIdentifier, String> extractor) {
        return message.getIdentifier()
                .map(extractor)
                .orElseThrow(() -> new AntennaExecutionException("Not able to extract part from message."));
    }

    /**
     * Checks if the message has maven or javaScript coordinates and returns the
     * right value
     */
    private String versionExtractor(ProcessingMessage message) {
        return genericExtractor(message, i -> {
            if (i.getMavenCoordinates() != null && i.getMavenCoordinates().getVersion() != null){
                return i.getMavenCoordinates().getVersion();
            } else if (i.getJavaScriptCoordinates() != null && i.getJavaScriptCoordinates().getVersion() != null) {
                return i.getJavaScriptCoordinates().getVersion();
            } else {
                LOGGER.debug("Not able to extract version from message.");
                return UNKNOWN;
            }
        });
    }

    /**
     * Checks if the message has maven or javaScript coordinates and returns the
     * right value
     */
    private String artifactIdExtractor(ProcessingMessage message) {
        return genericExtractor(message, i -> {
            if (i.getMavenCoordinates() != null && i.getMavenCoordinates().getArtifactId() != null){
                return i.getMavenCoordinates().getArtifactId();
            } else if (i.getJavaScriptCoordinates() != null && i.getJavaScriptCoordinates().getArtifactId() != null) {
                return i.getJavaScriptCoordinates().getArtifactId();
            } else {
                LOGGER.debug("Not able to extract artifactId from message.");
                return UNKNOWN;
            }
        });
    }

    /**
     * Add processing message to the report.
     */
    @Override
    public void add(String message, MessageType type) {
        ProcessingMessage msg = new ProcessingMessage(type);
        msg.setMessage(message);
        this.report.add(msg);
    }
}
