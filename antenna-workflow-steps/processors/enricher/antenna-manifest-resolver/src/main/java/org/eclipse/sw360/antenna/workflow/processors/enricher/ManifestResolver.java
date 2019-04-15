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

import org.apache.commons.io.FileUtils;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.util.AntennaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class scans all filepaths of the artifacts, if a jar file is found and
 * contains a Manifest file with bundle coordinates this coordinates are added
 * to the artifact which belongs to the scanned filepath. The jar is added to
 * the artifact as well.
 */

public class ManifestResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestResolver.class);

    public ManifestResolver() {
        this.workflowStepOrder = 900;
    }

    /**
     * Scans all file paths of the artifacts, if a jar file is found and
     * contains a Manifest file with bundle coordinates this coordinates are
     * added to the artifact which belongs to the scanned file path. The jar is
     * added to the artifact as well.
     *
     * @param artifacts List of artifacts, which will be resolved
     */
    private void resolveManifest(Collection<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            final Optional<List<String>> pathnames = artifact.askForGet(ArtifactPathnames.class);
            if (pathnames.isPresent() && pathnames.get().size() > 0) {
                resolveManifest(pathnames.get().get(0), artifact);
            }
        }
    }

    private void resolveManifest(String pathname, Artifact artifact){
        LOGGER.debug("Resolving {}", pathname);
        Path jarPath = context.getProject().getBasedir().toPath().resolve(pathname);

        try {
            final Path jar = resolveJarFile(jarPath);

            try (JarFile jarFile = new JarFile(jar.toFile())) {
                setBundleCoordinates(artifact, jarFile.getManifest());
            }

            artifact.addFact(new ArtifactFile(jar));
        } catch (IOException e) {
            LOGGER.error("Unable to process \"{}\" because of {}", jarPath,
                    e.getMessage());
            this.reporter.add(artifact,
                    MessageType.PROCESSING_FAILURE,
                    "An exeption occured while Manifest resolving:" + e.getMessage());
        }
    }

    private Path resolveJarFile(Path jarPath) throws IOException {
        Iterator<Path> jarPaths = AntennaUtils.getJarPathIteratorFromPath(jarPath);

        Path topLevelJar = jarPaths.next();

        if(!jarPaths.hasNext()){
            return topLevelJar;
        }

        Path targetJar = computeFinalJarFileName(jarPath);
        Files.createDirectories(targetJar.getParent());
        try(FileInputStream fis = new FileInputStream(topLevelJar.toFile())){
            extractDeepNestedJar(fis, jarPaths, targetJar);
        }
        return targetJar;
    }

    /**
     * walks recursively into nested jar/war/ear/zip files and extracts the innerest one
     */
    private void extractDeepNestedJar(InputStream zippedInputStream, Iterator<Path> nestedJars, Path finalPath) throws IOException {
        if(!nestedJars.hasNext()){
            FileUtils.copyInputStreamToFile(zippedInputStream, finalPath.toFile());
        }else {
            Path nextJarName = nestedJars.next();

            try (ZipInputStream zipInputStream = new ZipInputStream(zippedInputStream) ) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String nextJarNameWithoutLeadingSeparator = nextJarName.normalize().toString().replaceAll("^" + Pattern.quote(File.separator), "");
                    if (nextJarNameWithoutLeadingSeparator.equals(Paths.get(entry.getName()).normalize().toString())) {
                        extractDeepNestedJar(zipInputStream, nestedJars, finalPath);
                        return;
                    }
                }
            }
            throw new IOException("Unable to find \""+nextJarName+"\"");
        }
    }

    private Path computeFinalJarFileName(Path jarPath){
        Path cleanedUpPath = AntennaUtils.computeInnerReplacementJarPath(jarPath);
        return cleanedUpPath.toAbsolutePath();
    }

    /**
     * Adds the result of getAttribute to the artifacts bundle values.
     */
    private void setBundleCoordinates(Artifact artifact, Manifest manifest) {
        final Optional<String> symbolicName = getAttribute(manifest, "Bundle-SymbolicName");
        final Optional<String> version = getAttribute(manifest, "Bundle-Version");
        if(symbolicName.isPresent() || version.isPresent()) {
            artifact.addFact(new BundleCoordinates(symbolicName.orElse(null), version.orElse(null)));
        }
    }

    /**
     * @param manifest      JarFile of which the Manifest file shall be resolved.
     * @param attributeName Name of the attribute which should be found in the Manifest
     *                      file.
     * @return Returns the value of
     */
    private Optional<String> getAttribute(Manifest manifest, String attributeName) {
        return Optional.ofNullable(manifest)
                .map(Manifest::getMainAttributes)
                .flatMap(ma -> Optional.ofNullable(ma.getValue(attributeName)))
                .map(av -> av.split(";"))
                .map(av -> av[0]); // TODO: why is only the first value used?
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.info("Resolve manifest...");
        resolveManifest(artifacts);
        LOGGER.info("Resolve manifest... done");
        return artifacts;
    }
}
