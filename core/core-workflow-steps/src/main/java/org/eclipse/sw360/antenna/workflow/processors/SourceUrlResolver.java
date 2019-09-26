/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.util.HttpHelper;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


public class SourceUrlResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceUrlResolver.class);
    private HttpHelper httpHelper;
    private Path dependencyTargetDirectory;

    public SourceUrlResolver() {
        this.workflowStepOrder = 1500;
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.info("Resolve source urls...");
        resolveSourceUrls(artifacts);
        LOGGER.info("Resolve source urls... done");
        return artifacts;
    }

    private void resolveSourceUrls(Collection<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            Optional<String> sourceUrl = artifact.askForGet(ArtifactSourceUrl.class);
            if (sourceUrl.isPresent()) {
                try {
                    File file = httpHelper.downloadFile(sourceUrl.get(), dependencyTargetDirectory);
                    artifact.addFact(new ArtifactSourceFile(file.toPath()));
                } catch (IOException e) {
                    LOGGER.warn("Issue during download of artifact sources", e);
                }
            }
        }
    }

    @Override
    public void configure(Map<String,String> configMap) {
        super.configure(configMap);
        ToolConfiguration toolConfig = context.getToolConfiguration();
        ProxySettings proxySettings = new ProxySettings(toolConfig.useProxy(), toolConfig.getProxyHost(), toolConfig.getProxyPort());
        httpHelper = new HttpHelper(proxySettings);
        dependencyTargetDirectory = toolConfig.getDependenciesDirectory();
    }
}
