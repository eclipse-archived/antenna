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
package org.eclipse.sw360.antenna.bundle;

import org.apache.maven.shared.invoker.*;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Requests jar files for artifacts by using Maven Invoker. Use of this class
 * requires Maven to be installed locally.
 * See http://maven.apache.org/shared/maven-invoker/usage.html
 */
public class MavenInvokerRequester extends IArtifactRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenInvokerRequester.class);
    public static final String POM_FILENAME = "pom.xml";

    private final String MVN_ARG_GROUP_ID = "\"-DgroupId=%s\"";
    private final String MVN_ARG_ARTIFACT_ID = "\"-DartifactId=%s\"";
    private final String MVN_ARG_VERSION = "\"-Dversion=%s\"";
    private final String MVN_ARG_DEST = "\"-Ddest=%s\"";
    private final String MVN_ARG_CLASSIFIER = "\"-Dclassifier=sources\"";
    private final String MVN_DOWNLOAD_CMD = "dependency:get --quiet";

    private DefaultInvoker defaultInvoker;

    public MavenInvokerRequester(AntennaContext context) {
        super(context);
        defaultInvoker = new DefaultInvoker();
    }

    public MavenInvokerRequester(AntennaContext context, DefaultInvoker defaultInvoker) {
        super(context);
        this.defaultInvoker = defaultInvoker;
    }

    private List<String> buildMvnDownloadCmd(MavenCoordinates coordinates, Path targetDirectory, boolean isSource) {
        List<String> mvnDownloadCmd = new ArrayList<>();

        mvnDownloadCmd.add(String.format(MVN_ARG_GROUP_ID, coordinates.getGroupId()));
        mvnDownloadCmd.add(String.format(MVN_ARG_ARTIFACT_ID, coordinates.getArtifactId()));
        mvnDownloadCmd.add(String.format(MVN_ARG_VERSION, coordinates.getVersion()));
        mvnDownloadCmd.add(String.format(MVN_ARG_DEST, targetDirectory));
        if (isSource) {
            mvnDownloadCmd.add(MVN_ARG_CLASSIFIER);
        }
        mvnDownloadCmd.add(MVN_DOWNLOAD_CMD);

        return mvnDownloadCmd;
    }

    protected File getPomFileFromContext() {
        final File basedir = context.getProject().getBasedir();
        return new File(basedir, POM_FILENAME);
    }

    private InvocationRequest buildInvocationRequest(List<String> mvnDownloadCmd) {
        InvocationRequest request = new DefaultInvocationRequest();

        request.setPomFile(getPomFileFromContext());
        request.setGoals(mvnDownloadCmd);
        request.setOutputHandler(LOGGER::debug);

        return request;
    }

    private File getExpectedJarFile(MavenCoordinates coordinates, Path targetDirectory, boolean isSource) {
        String jarBaseName = getExpectedJarBaseName(coordinates, isSource);
        return new File(targetDirectory.toFile(), jarBaseName);
    }

    private void callMavenInvocationRequest(InvocationRequest request)
            throws MavenArtifactDoesNotExistException, AntennaExecutionException {
        try {
            InvocationResult result = defaultInvoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new MavenArtifactDoesNotExistException("Artifact not found in repo.");
            }
        } catch (MavenInvocationException e) {
            throw new AntennaExecutionException("Error when getting jar: " + e);
        }

    }

    @Override
    public File requestFile(MavenCoordinates coordinates, Path targetDirectory, boolean isSource)
            throws MavenArtifactDoesNotExistException, AntennaExecutionException {

        File expectedJarFile = getExpectedJarFile(coordinates, targetDirectory, isSource);

        if (expectedJarFile.exists()) {
            LOGGER.info("The file " + expectedJarFile + " already exists and won't be downloaded again");
            return expectedJarFile;
        }

        final List<String> mvnDownloadCmd = buildMvnDownloadCmd(coordinates, targetDirectory, isSource);
        InvocationRequest request = buildInvocationRequest(mvnDownloadCmd);
        callMavenInvocationRequest(request);

        if (!expectedJarFile.exists()) {
            throw new MavenArtifactDoesNotExistException("Maven call succeeded but Artifact was not generated in the expected place.");
        }

        return getExpectedJarFile(coordinates, targetDirectory, isSource);
    }
}
