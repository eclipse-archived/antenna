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

import org.apache.commons.lang.StringUtils;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.exceptions.FailedToDownloadException;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Requests jar files for artifacts by making HTTP requests.
 */
public class HttpRequester extends IArtifactRequester {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequester.class);
    public static final String GROUP_ID_PLACEHOLDER = "{groupId}";
    public static final String ARTIFACT_ID_PLACEHOLDER = "{artifactId}";
    public static final String VERSION_PLACEHOLDER = "{version}";
    private static final String MAVEN_CENTRAL_URL = "http://repo2.maven.org/maven2/" + GROUP_ID_PLACEHOLDER + "/" + ARTIFACT_ID_PLACEHOLDER + "/" + VERSION_PLACEHOLDER + "/";

    private HttpHelper httpHelper;

    public HttpRequester(AntennaContext context) {
        super(context);
        httpHelper = new HttpHelper(context);
    }

    @Override
    public Optional<File> requestFile(MavenCoordinates coordinates, Path targetDirectory, boolean isSource) {
        String jarBaseName = getExpectedJarBaseName(coordinates, isSource);
        File localJarFile = targetDirectory.resolve(jarBaseName).toFile();

        if (localJarFile.exists()) {
            LOGGER.info("The file " + localJarFile + " already exists and won't be downloaded again");
            return Optional.of(localJarFile);
        }

        String jarUrl = getJarUrl(coordinates, jarBaseName);

        try {
            return Optional.ofNullable(httpHelper.downloadFile(jarUrl, targetDirectory, jarBaseName));
        } catch (FailedToDownloadException | IOException e) {
            LOGGER.warn("Failed to find jar: " + e.getMessage(), e);
            return Optional.empty();
        }
    }

    private String getJarUrl(MavenCoordinates coordinates, String remoteFileName) {
        String repoTemplate = getJarUrlTemplate();

        // Construct URL (substitute in groupID, artifactID and version
        // NOTE: There should be no dots in the groupID. Dots delimit
        // directories, so are converted to slashes.
        String repo = repoTemplate
                .replace(GROUP_ID_PLACEHOLDER, coordinates.getGroupId()
                        .replace('.', '/'))
                .replace(ARTIFACT_ID_PLACEHOLDER, coordinates.getArtifactId())
                .replace(VERSION_PLACEHOLDER, coordinates.getVersion());

        if (!repo.substring(repo.length() - 1).equals("/")) {
            repo = repo + "/";
        }

        return repo + remoteFileName;
    }

    private String getJarUrlTemplate() {
        ToolConfiguration toolConfig = context.getToolConfiguration();
        String repoTemplate = toolConfig.getSourcesRepositoryUrl();

        if (StringUtils.isBlank(repoTemplate)) {
            repoTemplate = MAVEN_CENTRAL_URL;
        } else if (!isValidJarUrlTemplate(repoTemplate)) {
            LOGGER.warn("The sourcesRepositoryUrl does not contain all the required placeholders: " +
                    GROUP_ID_PLACEHOLDER + ", " + ARTIFACT_ID_PLACEHOLDER + ", " + VERSION_PLACEHOLDER +
                    ". Falling back to using Maven Central.");
            repoTemplate = MAVEN_CENTRAL_URL;
        }
        return repoTemplate;
    }

    private boolean isValidJarUrlTemplate(String sourceRepoTemplate) {
        return sourceRepoTemplate.contains(GROUP_ID_PLACEHOLDER)
                && sourceRepoTemplate.contains(ARTIFACT_ID_PLACEHOLDER)
                && sourceRepoTemplate.contains(VERSION_PLACEHOLDER);
    }
}
