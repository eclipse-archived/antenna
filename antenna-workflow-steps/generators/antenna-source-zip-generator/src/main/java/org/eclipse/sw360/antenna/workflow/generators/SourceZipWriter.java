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

package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.analysis.filter.AllowAllArtifactsFilter;
import org.eclipse.sw360.antenna.analysis.filter.BlacklistFilter;
import org.eclipse.sw360.antenna.analysis.filter.MatchStateArtifactFilter;
import org.eclipse.sw360.antenna.api.IArtifactFilter;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.repository.Attachable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.*;

/**
 * The SourceZipWriter creates a zip file containing the given Sources.
 */
public class SourceZipWriter extends AbstractGenerator {
    private static final String SOURCE_ZIP_PATH_KEY = "source.zip.path";
    private static final String IDENTIFIER = "sources-zip";
    private static final String CLASSIFIER = "antenna-sources-zip";
    private static final String TYPE = "zip";

    private IArtifactFilter notAllowed;
    private IArtifactFilter unknownMatchStateFilter;
    private IArtifactFilter p2ConfigFilter;
    private Path sourceZipPath;
    private IProcessingReporter reporter;
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceZipWriter.class);

    /**
     * Creates a zip file which contains the source jars of the artifacts, which
     * have passed all filters. Name of zip file :
     * buildNamejobNr_3rdPartySources.zip";
     *
     * @param artifacts
     *            Artifacts with the source files that will be added to the zip.
     */
    public File createZip(List<Artifact> artifacts) {
        if (!artifacts.isEmpty()) {
            createSourceZipPath();

            try (FileOutputStream output = new FileOutputStream(sourceZipPath.toFile());
                    ZipOutputStream zipOutput = new ZipOutputStream(output)) {
                zipOutput.setLevel(Deflater.BEST_COMPRESSION);
                for (Artifact artifact : artifacts) {
                    if (notAllowed.passed(artifact)) {
                        if (unknownMatchStateFilter.passed(artifact)) {
                            if (artifact.getMvnSourceJar() != null && p2ConfigFilter.passed(artifact)) {
                                this.addContentToZip(artifact, zipOutput, "mvn");
                            } else {
                                this.addContentToZip(artifact, zipOutput, "p2");
                            }
                        } else {
                            if (!artifact.isProprietary()) {
                                this.reporter.addProcessingMessage(artifact.getArtifactIdentifier(),
                                        MessageType.MATCHSTATE_UNKNOWN,
                                        "Artifact is not added to sources.zip as MatchState of artifact is declared as unknown.");
                            }
                        }
                    }
                }
                zipOutput.flush();
                output.flush();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage() + "  The Sources-Zip File could not be created");
            }
        }
        return sourceZipPath.toFile();
    }

    /**
     * Adds the content of the source zip with the specified sourceTypes. If an
     * mvn source exists it will be added, otherwise, p2 source will be added
     *
     * @param artifact
     *            Artifact of which the content shall be added.
     * @param zipOut
     *            ZipOutputStream for the ZipFile.
     * @param sourceType
     *            Can be "mvn" or "p2"
     */
    private void addContentToZip(Artifact artifact, ZipOutputStream zipOut, String sourceType) {
        if (this.notAllowed.passed(artifact)) {
            String filename = getFileNameOrNull(artifact);
            if (filename == null) {
                return;
            }
            try {
                File sourceJar = null;
                String entryName = filename.replaceAll(".jar", "");
                LOGGER.debug("Writing File: " + entryName);
                if ("mvn".equals(sourceType)) {
                    sourceJar = artifact.getMvnSourceJar();
                } else {
                    sourceJar = artifact.getP2SourceJar();
                }
                if (null != sourceJar) {
                    writeContentToZipEntry(zipOut, sourceJar, entryName);
                }
            } catch (ZipException e) {
                exceptionHandling(artifact, e);
            } catch (IOException e) {
                String message = "An Exception occured during the creation of the zip file: " + sourceType
                        + " source of " + filename + " could not be resolved: " + e.getMessage();
                this.reporter.addProcessingMessage(artifact.getArtifactIdentifier(), MessageType.PROCESSING_FAILURE,
                        message);
                LOGGER.warn(e.getMessage());
            }
        }
    }

    String getFileNameOrNull(Artifact artifact) {
        ArtifactIdentifier artifactIdentifier = artifact.getArtifactIdentifier();
        String filename = null;
        if (artifactIdentifier != null && artifactIdentifier.getFilename() != null) {
            filename = artifactIdentifier.getFilename();
        } else if (artifact.getMvnSourceJar() != null) {
            filename = artifact.getMvnSourceJar().getName();
        } else if (artifact.getP2SourceJar() != null) {
            filename = artifact.getP2SourceJar().getName();
        }
        if (filename == null) {
            LOGGER.debug("No filename for artifact '" + artifact + "'. ArtifactIdentifier was null.");
        }
        return filename;
    }

    private void exceptionHandling(Artifact artifact, ZipException e) {
        if (! e.getMessage().equals("zip file is empty")) {
            reporter.addProcessingMessage(artifact.getArtifactIdentifier(), MessageType.PROCESSING_FAILURE,
                    e.getMessage() + ": caused by File: " + getFileNameOrNull(artifact));
            LOGGER.warn(e.getMessage() + ": caused by File: " + getFileNameOrNull(artifact));
        }
    }

    /**
     * Writes the content of the specified source jar to the ZipOutputStream.
     *
     * @param zipOut
     * @param sourceJar
     * @param entryName
     * @throws IOException
     */
    private void writeContentToZipEntry(ZipOutputStream zipOut, File sourceJar, String entryName)
            throws IOException {
        ZipFile zipFile = new ZipFile(sourceJar);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            zipOut.putNextEntry(new ZipEntry(entryName + "/" + zipEntry.getName()));
            byte[] buffer = new byte[1024 * 4];
            for (int read = inputStream.read(buffer); -1 != read; read = inputStream.read(buffer)) {
                zipOut.write(buffer, 0, read);
            }
            zipOut.flush();
            inputStream.close();
        }
        zipFile.close();
    }

    /**
     * Creates the target path for the source zip.
     *
     * @return Returns the target path for the source zip.
     */
    private void createSourceZipPath() {
        if (sourceZipPath.toFile().isDirectory()) {
            throw new RuntimeException("Zip path is a directory but needs to be a file");
        }
        Path parent = sourceZipPath.getParent();
        if (parent == null) {
            throw new RuntimeException("The parent directory is null");
        }
        if (!parent.toFile().exists()) {
            boolean isCreated = parent.toFile().mkdirs();
            if (!isCreated) {
                throw new RuntimeException("We can not create a directory for the zip file");
            }
        }
    }

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> artifacts) {
        File sourceArchive = createZip(new ArrayList<>(artifacts));
        return Collections.singletonMap(IDENTIFIER, new Attachable(TYPE, CLASSIFIER, sourceArchive));
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        this.reporter = context.getProcessingReporter();
        this.notAllowed = new AllowAllArtifactsFilter();
        Set<MatchState> blacklistUnknown = new HashSet<>();
        blacklistUnknown.add(MatchState.UNKNOWN);
        this.unknownMatchStateFilter = new MatchStateArtifactFilter(blacklistUnknown);
        this.p2ConfigFilter = new BlacklistFilter(context.getConfiguration().getPrefereP2());

        if (configMap.containsKey(SOURCE_ZIP_PATH_KEY)) {
            this.sourceZipPath = Paths.get(getConfigValue(SOURCE_ZIP_PATH_KEY, configMap));
        } else {
            this.sourceZipPath = context.getToolConfiguration().getAntennaTargetDirectory().resolve("sources.zip");
        }
    }
}
