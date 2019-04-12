/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.bundle;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaExecutionException;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Requests jar files for artifacts by using Maven Invoker. Use of this class
 * requires Maven to be installed locally.
 * See http://maven.apache.org/shared/maven-invoker/usage.html
 */
public class MavenInvokerRequester extends IArtifactRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenInvokerRequester.class);
    public static final String POM_FILENAME = "pom.xml";

    private static final String MVN_ARG_GROUP_ID = "\"-DgroupId=%s\"";
    private static final String MVN_ARG_ARTIFACT_ID = "\"-DartifactId=%s\"";
    private static final String MVN_ARG_VERSION = "\"-Dversion=%s\"";
    private static final String MVN_ARG_DEST = "\"-Ddest=%s\"";
    private static final String MVN_ARG_CLASSIFIER = "\"-Dclassifier=%s\"";
    private static final String MVN_ARG_REPOS = "\"-DremoteRepositories=%s\"";
    private static final String MVN_DOWNLOAD_CMD = "dependency:get --quiet";

    private DefaultInvoker defaultInvoker;
    private Optional<URL> sourceRepositoryUrl;

    public MavenInvokerRequester(AntennaContext context) {
        this(context, new DefaultInvoker(), Optional.empty());
    }

    public MavenInvokerRequester(AntennaContext context, URL sourceRepositoryUrl) {
        this(context, new DefaultInvoker(), Optional.of(sourceRepositoryUrl));
    }

    public MavenInvokerRequester(AntennaContext context, DefaultInvoker defaultInvoker, Optional<URL> sourceRepositoryUrl) {
        super(context);
        this.defaultInvoker = defaultInvoker;
        if (System.getenv("M2_HOME") != null) {
            defaultInvoker.setMavenExecutable(new File(System.getenv("M2_HOME")));
        } else {
            LOGGER.warn("Variable M2_HOME is undefined. If you have any problems using the MavenInvokerRequester, please set this variable.");
        }
        this.sourceRepositoryUrl = sourceRepositoryUrl;
    }

    @Override
    public Optional<File> requestFile(MavenCoordinates coordinates, Path targetDirectory, ClassifierInformation classifierInformation)
            throws AntennaExecutionException {

        File expectedJarFile = getExpectedJarFile(coordinates, targetDirectory, classifierInformation);

        if (expectedJarFile.exists()) {
            LOGGER.info("The file " + expectedJarFile + " already exists and won't be downloaded again");
            return Optional.of(expectedJarFile);
        }

        LOGGER.debug("Requesting artifact with id " + coordinates.getArtifactId());
        boolean requestSuccessful = callMavenInvoker(coordinates, targetDirectory, classifierInformation.classifier);

        String jarType = classifierInformation.isSource ? "sources jar" : classifierInformation.classifier + " jar";
        if (!requestSuccessful) {
            LOGGER.warn("Failed to find " + jarType + ": Artifact " + coordinates.getName() + " not found in repo.");
            return Optional.empty();
        } else if (!expectedJarFile.exists()) {
            LOGGER.warn("Failed to find " + jarType + ": Maven call succeeded but Artifact was not generated in the expected place.");
            return Optional.empty();
        }

        return Optional.of(getExpectedJarFile(coordinates, targetDirectory, classifierInformation));
    }

    private boolean callMavenInvoker(MavenCoordinates coordinates, Path targetDirectory, String classifier) {
        final List<String> mvnDownloadCmd = buildBasicMvnDownloadCmd(coordinates, targetDirectory);
        if (sourceRepositoryUrl.isPresent()) {
            mvnDownloadCmd.add(String.format(MVN_ARG_REPOS, sourceRepositoryUrl.get().toString()));
        }
        if (!classifier.isEmpty()) {
            mvnDownloadCmd.add(String.format(MVN_ARG_CLASSIFIER, classifier));
        }
        InvocationRequest request = buildInvocationRequest(mvnDownloadCmd);
        return callMavenInvocationRequest(request);
    }

    private List<String> buildBasicMvnDownloadCmd(MavenCoordinates coordinates, Path targetDirectory) {
        List<String> mvnDownloadCmd = new ArrayList<>();

        mvnDownloadCmd.add(String.format(MVN_ARG_GROUP_ID, coordinates.getGroupId()));
        mvnDownloadCmd.add(String.format(MVN_ARG_ARTIFACT_ID, coordinates.getArtifactId()));
        mvnDownloadCmd.add(String.format(MVN_ARG_VERSION, coordinates.getVersion()));
        mvnDownloadCmd.add(String.format(MVN_ARG_DEST, targetDirectory));
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

    private File getExpectedJarFile(MavenCoordinates coordinates, Path targetDirectory, ClassifierInformation classifierInformation) {
        String jarBaseName = getExpectedJarBaseName(coordinates, classifierInformation);
        return new File(targetDirectory.toFile(), jarBaseName);
    }

    private boolean callMavenInvocationRequest(InvocationRequest request) throws AntennaExecutionException {
        try {
            LOGGER.info("Calling Maven Invoker with command " + String.join(", ", request.getGoals()));
            return defaultInvoker.execute(request)
                    .getExitCode() == 0;
        } catch (MavenInvocationException e) {
            throw new AntennaExecutionException("Error when getting jar: " + e);
        }

    }
}
