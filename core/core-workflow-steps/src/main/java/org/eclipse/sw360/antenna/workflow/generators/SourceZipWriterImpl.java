/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.eclipse.sw360.antenna.api.Attachable;
import org.eclipse.sw360.antenna.api.IArtifactFilter;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class SourceZipWriterImpl {
    private static final String IDENTIFIER = "sources-zip";
    private static final String CLASSIFIER = "antenna-sources-zip";
    private static final String TYPE = "zip";

    private IArtifactFilter notAllowed;
    private IArtifactFilter unknownMatchStateFilter;
    private Path sourceZipPath;
    private IProcessingReporter reporter;

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceZipWriterImpl.class);

    public SourceZipWriterImpl(Path sourceZipPath, IArtifactFilter notAllowed, IArtifactFilter unknownMatchStateFilter, IProcessingReporter reporter) {
        this.sourceZipPath = sourceZipPath;
        this.notAllowed = notAllowed;
        this.unknownMatchStateFilter = unknownMatchStateFilter;
        this.reporter = reporter;
    }

    public Map<String, IAttachable> produce(Collection<Artifact> artifacts) {
        return Collections.singletonMap(IDENTIFIER, new Attachable(TYPE, CLASSIFIER, createZip(new ArrayList<>(artifacts))));
    }

    /**
     * Creates a zip file which contains the source jars of the artifacts, which
     * have passed all filters. Name of zip file :
     * buildNamejobNr_3rdPartySources.zip";
     *
     * @param artifacts Artifacts with the source files that will be added to the zip.
     */
    public File createZip(List<Artifact> artifacts) {
        if (!artifacts.isEmpty()) {
            createSourceZipPath();

            try (FileOutputStream output = new FileOutputStream(sourceZipPath.toFile());
                 ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(output)) {

                zipOutput.setLevel(Deflater.BEST_COMPRESSION);

                artifacts.stream()
                        .filter(notAllowed::passed)
                        .forEach(artifact -> {
                            if (unknownMatchStateFilter.passed(artifact)) {
                                addContentToZip(artifact, zipOutput);
                            } else {
                                if (!artifact.isProprietary()) {
                                    this.reporter.add(artifact,
                                            MessageType.MATCHSTATE_UNKNOWN,
                                            "Artifact is not added to sources.zip as MatchState of artifact is declared as unknown.");
                                }
                            }
                        });

                zipOutput.flush();
                output.flush();
            } catch (IOException e) {
                throw new ExecutionException("The sources.zip file could not be created", e);
            }
        }
        return sourceZipPath.toFile();
    }

    /**
     * Creates the target path for the source zip.
     */
    private void createSourceZipPath() {
        if (sourceZipPath.toFile().isDirectory()) {
            throw new ExecutionException("Zip path is a directory but needs to be a file");
        }
        Path parent = sourceZipPath.getParent();
        if (parent == null) {
            throw new ExecutionException("The parent directory is null");
        }
        if (!parent.toFile().exists()) {
            boolean isCreated = parent.toFile().mkdirs();
            if (!isCreated) {
                throw new ExecutionException("We can not create a directory for the zip file");
            }
        }
    }

    /**
     * Adds the content of the source zip with the specified sourceTypes.
     *
     * @param artifact Artifact of which the content shall be added.
     * @param zipOut   ZipOutputStream for the ZipFile.
     */
    private void addContentToZip(Artifact artifact, ZipArchiveOutputStream zipOut) {
        if (notAllowed.passed(artifact)) {
            final Optional<Path> sourceFile = artifact.askForGet(ArtifactSourceFile.class);
            if (!sourceFile.isPresent()) {
                return;
            }
            String entryName = artifact.askFor(ArtifactFilename.class)
                    .flatMap(ArtifactFilename::getBestFilenameEntryGuess)
                    .map(ArtifactFilename.ArtifactFilenameEntry::getFilename)
                    .orElse(sourceFile.get().toFile().getName())
                    .replaceAll(".jar", "");
            try {
                LOGGER.debug("Writing File: {}", entryName);
                writeContentToZipEntry(zipOut, sourceFile.get().toFile(), entryName);
            } catch (ZipException e) {
                if (!e.getMessage().equals("zip file is empty")) {
                    reporter.add(artifact, MessageType.PROCESSING_FAILURE,
                            e.getMessage() + ": caused by File: " + sourceFile);
                    LOGGER.warn("{}: caused by File: {}", e.getMessage(), sourceFile);
                }
            } catch (IOException e) {
                String message = "An Exception occurred during the creation of the zip file: source of " + artifact + " could not be resolved: " + e.getMessage();
                this.reporter.add(artifact, MessageType.PROCESSING_FAILURE,
                        message);
                LOGGER.warn(e.getMessage());
            }
        }
    }

    private void writeContentToZipEntry(ZipArchiveOutputStream zipOut, File sourceJar, String entryName)
            throws IOException {
        try (ZipFile zipFile = new ZipFile(sourceJar)) {
            Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry zipEntry = entries.nextElement();
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                zipOut.putArchiveEntry(new ZipArchiveEntry(entryName + "/" + zipEntry.getName()));
                byte[] buffer = new byte[1024 * 4];
                for (int read = inputStream.read(buffer); -1 != read; read = inputStream.read(buffer)) {
                    zipOut.write(buffer, 0, read);
                }
                zipOut.closeArchiveEntry();
                zipOut.flush();
                inputStream.close();
            }
        }
    }
}
