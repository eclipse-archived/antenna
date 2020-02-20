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

import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.http.config.ProxySettings;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MavenArtifactResolver extends AbstractProcessor {
    private static final String PREFERRED_SOURCE_QUALIFIER = "preferredSourceClassifier";
    private static final String SOURCES_REPOSITORY_URL = "sourcesRepositoryUrl";
    private Path dependencyTargetDirectory;
    private List<ArtifactSelector> sourceResolvingBlacklist;
    private String preferredSourceQualifier;
    private URL sourcesRepositoryUrl;

    public MavenArtifactResolver() {
        this.workflowStepOrder = 300;
    }


    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        ToolConfiguration toolConfig = context.getToolConfiguration();
        ProxySettings proxySettings = new ProxySettings(
                toolConfig.useProxy(), toolConfig.getProxyHost(), toolConfig.getProxyPort());
        return new MavenArtifactResolverImpl(
                proxySettings,
                context.getGeneric(RepositorySystem.class),
                context.getGeneric(MavenProject.class),
                context.getGeneric(LegacySupport.class),
                dependencyTargetDirectory,
                sourceResolvingBlacklist,
                preferredSourceQualifier,
                sourcesRepositoryUrl,
                context.getProcessingReporter(),
                toolConfig.isMavenInstalled(),
                context.getProject().getBasedir())
                .process(artifacts);
    }

    @Override
    public void configure(Map<String, String> configMap) {
        dependencyTargetDirectory = context.getToolConfiguration().getDependenciesDirectory();
        sourceResolvingBlacklist = context.getConfiguration().getIgnoreForSourceResolving();
        String sourcesUrlString = configMap.get(SOURCES_REPOSITORY_URL);
        if (sourcesUrlString != null) {
            try {
                sourcesRepositoryUrl = new URL(sourcesUrlString);
            } catch (MalformedURLException e) {
                throw new ConfigurationException("The URL in 'sourcesRepositoryUrl' is not valid.");
            }
        }
        preferredSourceQualifier = configMap.get(PREFERRED_SOURCE_QUALIFIER);
    }
}