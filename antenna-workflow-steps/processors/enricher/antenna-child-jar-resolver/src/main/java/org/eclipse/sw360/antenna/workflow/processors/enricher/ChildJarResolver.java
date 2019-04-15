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

import org.apache.commons.io.IOUtils;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.util.AntennaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Resolves the Manifest file of Jars packaged in an other zip/war/jar.
 */
public class ChildJarResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChildJarResolver.class);
    private Path targetDirectory;

    public ChildJarResolver() {
        this.workflowStepOrder = 400;
    }

    /**
     * Resolves the given list of artifacts. Checks if a pathname contains more
     * than one jar/zip/war, if yes the inner jar/zip/war will be resolved.
     *
     */
    private void resolveArtifacts(Collection<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            final Optional<List<String>> pathnames = artifact.askForGet(ArtifactPathnames.class);
            boolean hasPathnames = pathnames.isPresent() && ! pathnames.get().isEmpty();
            boolean hasSources = artifact.getSourceFile().isPresent();
            if (!hasSources && hasPathnames) {
                String firstPath = pathnames.get().get(0);

                Path path = Paths.get(firstPath);
                if (isPathContainingInnerJars(path)) {
                    resolveSources(artifacts, artifact, path);
                }
            } else if (hasPathnames && !hasSources) {
                this.reporter.add(artifact, MessageType.MISSING_PATHNAME,
                        "An Artifact has no Pathnames, the Manifest file could not be resolved and no bundle coordinates were found.");
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
                String fileName = parentArtifactFileName.get();

                try {
                    final Optional<Path> sourceFile = parentArtifact.askForGet(ArtifactSourceFile.class);
                    if (sourceFile.isPresent()) {
                        File parentSource = sourceFile.get().toFile();
                        artifactWithinSomeOtherJar.addFact(new ArtifactSourceFile(getChildSourceJar(parentSource, fileName).toPath()));
                    }
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                    reporter.add(artifactWithinSomeOtherJar,
                            MessageType.PROCESSING_FAILURE,
                            "An exception occurred while Child Source resolving:" + e.getMessage());
                }
            }
        }
    }

    private Optional<String> getArtifactFileName(Artifact artifact) {
        final Optional<String> artifactFilename = artifact.askFor(ArtifactFilename.class)
                .map(ArtifactFilename::getFilename);
        if(! artifactFilename.isPresent()) {
            reporter.add(MessageType.PROCESSING_FAILURE, "No filename available as ArtifactIdentifier of '" + artifact + "' is null.");
        }
        return artifactFilename;
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
