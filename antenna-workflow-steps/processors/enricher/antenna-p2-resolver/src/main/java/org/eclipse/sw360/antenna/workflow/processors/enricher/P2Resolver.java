/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
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
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.bundle.ArtifactAttacher;
import org.eclipse.sw360.antenna.bundle.ProductInstaller;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.java.BundleCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.eclipse.sw360.antenna.bundle.EclipseProcessBuilder.setupEclipseProcess;

public class P2Resolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(P2Resolver.class);

    private static final String DEPENDENCY_REPOSITORY = "repositories";
    private static final String ANTENNA_ECLIPSE_APP = "org.eclipse.sw360.antenna.p2.app";

    private List<String> repositories;

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        repositories = Arrays.asList(getConfigValue(DEPENDENCY_REPOSITORY, configMap).split(";"));
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) throws AntennaException {
        File productInstallationArea = createTempDirectory();
        File artifactDownloadArea = createTempDirectory();

        List<Artifact> actionableIntermediates = intermediates.stream()
                .filter(artifact -> artifact.askFor(BundleCoordinates.class).isPresent())
                .filter(artifact -> !(artifact.getFile().isPresent() && artifact.getSourceFile().isPresent()))
                .collect(Collectors.toList());

        // We do P2 extraction by running an eclipse RCP product headless on the command line.
        // To find the product at runtime, it resides in the antenna-p2-resolver jar file and is extracted and
        // installed to a temporary location (the productInstallationArea)
        ProductInstaller.create()
                .installEclipseProductForP2Resolution(productInstallationArea.toString());

        // The product is run as a subprocess. Output is captured and printed to the console.
        // To pass information to the product, we use command line arguments as described in the EclipseProcessBuilder.
        // In particular:
        //  - we run the executable in the productInstallationArea
        //  - we download artifacts into the downloadArea
        //  - and we resolve by repositories given through the config.
        runEclipseProduct(productInstallationArea, artifactDownloadArea, actionableIntermediates);

        // The Eclipse process will download artifacts into the temporary artifactDownloadArea
        // Since the name is always unique, we can recreate it ourselves and if necessary attach the files to the artifact
        // and copy them to a different location where they will not be deleted after the resolver finishes.
        attachArtifacts(artifactDownloadArea, actionableIntermediates);

        deleteTemporaryDirectory(productInstallationArea);
        deleteTemporaryDirectory(artifactDownloadArea);

        return intermediates;
    }

    private void attachArtifacts(File artifactDownloadArea, List<Artifact> actionableIntermediates) throws AntennaException {
        try {
            ArtifactAttacher attacher = new ArtifactAttacher(context.getToolConfiguration().getDependenciesDirectory());

            attacher.copyDependencies(artifactDownloadArea, actionableIntermediates);
        } catch (IOException e) {
            throw new AntennaException("Error while copying File.", e);
        }
    }

    private void runEclipseProduct(File productInstallationArea, File artifactDownloadArea, List<Artifact> actionableIntermediates) throws AntennaException {
        try {
            Process process = setupEclipseProcess(
                    productInstallationArea, artifactDownloadArea, actionableIntermediates, repositories).start();

            loggingResolverLogOutput(process);
        } catch (IOException e) {
            throw new AntennaException("Error while using external product " + ANTENNA_ECLIPSE_APP, e);
        }
    }

    private void loggingResolverLogOutput(Process process) throws IOException {
        try (
                InputStream errorIs = process.getErrorStream();
                InputStreamReader errorReader = new InputStreamReader(errorIs);
                BufferedReader errorBufferedReader = new BufferedReader(errorReader);
                InputStream outputIs = process.getInputStream();
                InputStreamReader outputReader = new InputStreamReader(outputIs);
                BufferedReader outputBufferedReader = new BufferedReader(outputReader);
        ) {

            String line;
            while ((line = errorBufferedReader.readLine()) != null) {
                LOGGER.error(line);
            }

            while ((line = outputBufferedReader.readLine()) != null) {
                LOGGER.info(line);
            }
        }
    }

    private static File createTempDirectory() throws AntennaException {
        try {
            final File tempDir = File.createTempFile("equinox", "");
            if (!(tempDir.delete() && tempDir.mkdirs())) {
                throw new AntennaException("Could not create temp dir " + tempDir);
            }
            return tempDir;
        } catch (IOException e) {
            throw new AntennaException(e.getLocalizedMessage(), e);
        }
    }

    private static void deleteTemporaryDirectory(File temporaryDirectory) throws AntennaException {
        try {
            FileUtils.forceDelete(temporaryDirectory);
        } catch (IOException e) {
            LOGGER.error("Could not delete temporary directory " + temporaryDirectory.toString() + ", reason:", e);
        }
    }
}
