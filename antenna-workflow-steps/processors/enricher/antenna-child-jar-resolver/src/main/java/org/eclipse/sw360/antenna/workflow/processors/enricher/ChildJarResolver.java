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
package org.eclipse.sw360.antenna.workflow.processors.enricher;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.util.AntennaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;

/**
 * Resolves the Manifest file of Jars packaged in an other zip/war/jar.
 */
public class ChildJarResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChildJarResolver.class);
    private Path targetDirectory;

    /**
     * Resolves the given list of artifacts. Checks if a pathname contains more
     * than one jar/zip/war, if yes the inner jar/zip/war will be resolved.
     *
     */
    private void resolveArtifacts(Collection<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            boolean hasPathnames = artifact.getPathnames().length > 0;
            if (!artifact.hasSources() && hasPathnames) {
                Path path = Paths.get(artifact.getPathnames()[0]);
                if (isPathContainingInnerJars(path)) {
                    resolveSources(artifacts, artifact, path);
                }
            } else if (hasPathnames && !artifact.hasSources()) {
                this.reporter.addProcessingMessage(artifact.getArtifactIdentifier(), MessageType.MISSING_PATHNAME,
                        "As Artifact has no Pathnames, the Manifest file could not be resolved and no bundle coordinates were found.");
            }
        }
    }

    /**
     * @param artifacts the complete list of all artifacts passed to the Resolver
     * @param artifactWithinSomeOtherJar the artifact currently working on
     */
    private void resolveSources(Collection<Artifact> artifacts, Artifact artifactWithinSomeOtherJar, Path pathToArtifact) {
        String parentJarName = getParentJarBaseName(pathToArtifact);
        for (Artifact parentArtifact : artifacts) {
            Optional<String> parentArtifactFileName = getArtifactFileName(parentArtifact);

            if (parentArtifactFileName.isPresent() &&
                    parentJarName.equals(parentArtifactFileName.get())) {
                File parentSource;
                String fileName = parentArtifactFileName.get();

                try {
                    if (parentArtifact.getMvnSourceJar() != null) {
                        parentSource = parentArtifact.getMvnSourceJar();
                        artifactWithinSomeOtherJar.setMavenSourceJar(getChildSourceJar(parentSource, fileName));
                        return;
                    } else if (parentArtifact.getP2SourceJar() != null) {
                        parentSource = parentArtifact.getP2SourceJar();
                        artifactWithinSomeOtherJar.setP2SourceJar(getChildSourceJar(parentSource, fileName));
                        return;
                    }
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                    reporter.addProcessingMessage(artifactWithinSomeOtherJar.getArtifactIdentifier(),
                            MessageType.PROCESSING_FAILURE,
                            "An exeption occured while Child Source resolving:" + e.getMessage());
                }
            }
        }
    }

    private Optional<String> getArtifactFileName(Artifact artifact) {
        ArtifactIdentifier artifactIdentifier = artifact.getArtifactIdentifier();
        if (artifactIdentifier == null) {
            ProcessingMessage msg = new ProcessingMessage(MessageType.PROCESSING_FAILURE);
            msg.setMessage("No filename available as ArtifactIdentifier of '" + artifact + "' is null.");
            reporter.add(msg);
            return Optional.empty();
        }
        return Optional.ofNullable(artifactIdentifier.getFilename());
    }

    /**
     * This takes a inputSourceJar, and writes it to {@code targetDirectory + "/" + outputSourceJarFileName }
     * While writing each zip entry it changes its internal path from
     *      {@code "path/within/zip" }
     * to
     *      {@code + "artifactName_jar/path/within/zip" }
     * where {@code "artifactName.jar" } is the {@code outputSourceJarFileName }
     */
    private File getChildSourceJar(File inputSourceJar, String outputSourceJarFileName) throws IOException {
        String entryName = AntennaUtils.replaceDotInJarExtension(outputSourceJarFileName);
        File outputSourceJar = targetDirectory.resolve(outputSourceJarFileName).toFile();

        if (outputSourceJar.exists()) {
            LOGGER.warn("The file " + outputSourceJar + " already exists, ChildJarResolver will not overwrite it.");
            return outputSourceJar;
        }

        try (JarFile parentSourceJar = new JarFile(inputSourceJar);
             FileOutputStream output = new FileOutputStream(outputSourceJar);
             JarOutputStream jarOutput = new JarOutputStream(output)) {
            Enumeration<JarEntry> entries = parentSourceJar.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                jarOutput.putNextEntry(new ZipEntry(entryName + "/" + zipEntry.getName()));
                try (InputStream inputStream = parentSourceJar.getInputStream(zipEntry)) {
                    IOUtils.copy(inputStream, jarOutput);
                }
            }
            jarOutput.flush();

        }
        return outputSourceJar;
    }

    /**
     *
     * @param path
     *            Pathname of the parentJar.
     * @return Pathname of the parentJar.
     */
    private String getParentJarBaseName(Path path) {
        return AntennaUtils.getJarPath(path).getFileName().toString();
    }

    private boolean isPathContainingInnerJars(Path jarPath) {
        final Iterator<Path> jarPathIteratorFromPath = AntennaUtils.getJarPathIteratorFromPath(jarPath);
        if (jarPathIteratorFromPath.hasNext()) {
            jarPathIteratorFromPath.next();
            return jarPathIteratorFromPath.hasNext();
        }
        return false;
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.info("Resolving child jars...");
        resolveArtifacts(artifacts);
        LOGGER.info("Resolving child jars... done");
        return artifacts;
    }

    @Override
    public void configure(Map<String,String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);
        this.targetDirectory = context.getToolConfiguration().getAntennaTargetDirectory();
    }
}
