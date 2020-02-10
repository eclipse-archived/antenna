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

package org.eclipse.sw360.antenna.maven.workflow.processors.enricher;

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Processor scans all artifacts whether they are p2 based bundles. This is done by searching
 * in the jar file of the artifact for a Manifest file with bundle coordinates. These coordinates are added
 * to the artifact as P2 coordinates
 */

public class ManifestResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestResolver.class);

    public ManifestResolver() {
        this.workflowStepOrder = 900;
    }

    private Path basePath;

    @Override
    public void setAntennaContext(AntennaContext context) {
        super.setAntennaContext(context);
        this.basePath = context.getProject().getBasedir().toPath();
    }

    /**
     * Scans all a artifacts, if a jar file is found and
     * contains a Manifest file with bundle coordinates this coordinates are
     * added to the artifact.
     *
     * @param artifacts List of artifacts, which will be resolved
     */
    private void resolveManifest(Collection<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            final Optional<Path> pathname = artifact.askForGet(ArtifactFile.class);
            if (pathname.isPresent() && pathname.get().toFile().exists()) {
                resolveManifest(pathname.get(), artifact);
            }
        }
    }

    private void resolveManifest(Path pathname, Artifact artifact){
        LOGGER.debug("Resolving {}", pathname);

        try (JarFile jarFile = new JarFile(basePath.resolve(pathname).toFile())) {
            setBundleCoordinates(artifact, jarFile.getManifest());
        } catch (IOException e) {
            LOGGER.error("Unable to process \"{}\" because of {}", pathname,
                    e.getMessage());
            this.reporter.add(artifact,
                    MessageType.PROCESSING_FAILURE,
                    "An exeption occured while Manifest resolving:" + e.getMessage());
        }
    }

    /**
     * Adds the result of getAttribute to the artifacts bundle values.
     */
    private void setBundleCoordinates(Artifact artifact, Manifest manifest) {
        final Optional<String> symbolicName = getAttribute(manifest, "Bundle-SymbolicName");
        final Optional<String> version = getAttribute(manifest, "Bundle-Version");
        if(symbolicName.isPresent() || version.isPresent()) {
            artifact.addCoordinate(new Coordinate(Coordinate.Types.P2, symbolicName.orElse(null), version.orElse(null)));
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
                .map(av -> av[0]); // Use first value, because attributes contain further information not needed here after ';'
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.debug("Resolve manifest...");
        resolveManifest(artifacts);
        LOGGER.debug("Resolve manifest... done");
        return artifacts;
    }
}
