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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;

/**
 * Requests jar files for artifacts by making HTTP requests.
 */
public class HttpRequester extends IArtifactRequester {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequester.class);
    public static final String GROUP_ID_PLACEHOLDER = "{groupId}";
    public static final String ARTIFACT_ID_PLACEHOLDER = "{artifactId}";
    public static final String VERSION_PLACEHOLDER = "{version}";
    private static final String MAVEN_CENTRAL_URL = "http://repo2.maven.org/maven2/"+GROUP_ID_PLACEHOLDER+"/"+ARTIFACT_ID_PLACEHOLDER+"/"+VERSION_PLACEHOLDER+"/";

    public HttpRequester(AntennaContext context) {
        super(context);
    }

    private boolean isValidJarUrlTemplate(String sourceRepoTemplate) {
        return sourceRepoTemplate.contains(GROUP_ID_PLACEHOLDER)
                && sourceRepoTemplate.contains(ARTIFACT_ID_PLACEHOLDER)
                && sourceRepoTemplate.contains(VERSION_PLACEHOLDER);
    }

    private String getJarUrlTemplate() {
        ToolConfiguration toolConfig = context.getToolConfiguration();
        String repoTemplate = toolConfig.getSourcesRepositoryUrl();

        if (StringUtils.isBlank(repoTemplate)) {
            repoTemplate = MAVEN_CENTRAL_URL;
        } else if (! isValidJarUrlTemplate(repoTemplate)) {
            LOGGER.warn("The sourcesRepositoryUrl does not contain all the required placeholders: " +
                    GROUP_ID_PLACEHOLDER + ", " + ARTIFACT_ID_PLACEHOLDER + ", " + VERSION_PLACEHOLDER +
                    ". Falling back to using Maven Central.");
            repoTemplate = MAVEN_CENTRAL_URL;
        }
        return repoTemplate;
    }

    protected String getJarUrl(MavenCoordinates coordinates, String remoteFileName) {
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

    private CloseableHttpClient getConfiguretHttpClient() {
        ToolConfiguration tc = context.getToolConfiguration();

        if (tc.useProxy()) {
            LOGGER.info("Using proxy on host {} and port {}", tc.getProxyHost(), tc.getProxyPort());
            return HttpClients.custom()
                    .useSystemProperties()
                    .setProxy(new HttpHost(tc.getProxyHost(), tc.getProxyPort()))
                    .build();
        } else {
            return HttpClients.createDefault();
        }
    }

    private CloseableHttpResponse callJarUrl(String jarUrl)
            throws ArtifactDoesNotExistException, IOException {
        CloseableHttpClient httpClient = getConfiguretHttpClient();

        try {
            HttpGet getRequest = new HttpGet(jarUrl);
            LOGGER.info("Downloading {}", jarUrl);
            return httpClient.execute(getRequest);
        } catch (IOException e) {
            throw new IOException(
                    "Error when trying to contact source repository (" + jarUrl + "): " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ArtifactDoesNotExistException("No valid URL could be constructed from the artifact info");
        }
    }


    @Override
    public File requestFile(MavenCoordinates coordinates, Path targetDirectory, boolean isSource)
            throws ArtifactDoesNotExistException, IOException {
        String jarBaseName = getExpectedJarBaseName(coordinates, isSource);
        File localJarFile = targetDirectory.resolve(jarBaseName).toFile();

        if(localJarFile.exists()) {
            LOGGER.info("The file " + localJarFile + " already exists and won't be downloaded again");
            return localJarFile;
        }

        String jarUrl = getJarUrl(coordinates, jarBaseName);

        try (CloseableHttpResponse response = callJarUrl(jarUrl)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new ArtifactDoesNotExistException("File not found on URL=["+jarUrl+"]");
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream is = entity.getContent()) {
                    FileUtils.copyInputStreamToFile(is, localJarFile);
                }
            }

            if (! localJarFile.exists()) {
                throw new ArtifactDoesNotExistException("Expected Artifact was not generated");
            }

            return localJarFile;
        } catch (IOException e) {
            throw new IOException("Encountered a problem while processing the url=["+jarUrl+"]: " + e.getMessage());
        }
    }
}
