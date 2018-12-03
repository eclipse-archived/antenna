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

package org.eclipse.sw360.antenna.workflow.processors.filter;

import java.util.Collection;
import java.util.List;

import org.eclipse.sw360.antenna.analysis.filter.BlacklistFilter;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.reporting.MessageType;

public class ConfigurationHandlerOverride extends AbstractProcessor {

    private final AntennaContext context;
    private IProcessingReporter reporter;

    /**
     * Resolves the values which are overridden in the config.xml. If an
     * attribute of an artifact is overridden in the config, the value is
     * changed in the attribute. If a value is changed a ProcessingMessage with
     * the Type: OVERRIDE_ARTIFACTVALUES is added to the report.
     *
     */
    public ConfigurationHandlerOverride(AntennaContext context) {
        this.context = context;
        this.reporter = context.getProcessingReporter();
    }

    /**
     * Resolves the values which are overridden in the config.xml. If an
     * attribute of an artifact is overridden in the config, the value is
     * changed in the attribute. If a value is changed a ProcessingMessage with
     * the Type: OVERRIDE_ARTIFACTVALUES is added to the report.
     *
     * @param configuration
     * @param artifacts
     * @param reporter
     */
    private void resolveOverride(Configuration configuration, Collection<Artifact> artifacts,
            IProcessingReporter reporter) {
        configureIgnoreForDownload(configuration, artifacts, reporter);

        configuration.getOverride()
                .forEach((key, override) -> artifacts.stream()
                        .filter(key::matches)
                        .forEach(artifact -> overrideArtifact(artifact, override)));
    }

    private void overrideArtifact(Artifact artifact, Artifact override) {
        artifact.overrideWith(override);
    }

    /**
     * Sets the attribute "IgnoreForDownload" of an artifact true if it the
     * artifact is part of the IgnoreForSourceResolving list of the given
     * configuration.
     *
     * @param configuration
     * @param artifacts
     * @param reporter2
     */
    private void configureIgnoreForDownload(Configuration configuration, Collection<Artifact> artifacts,
            IProcessingReporter reporter2) {
        List<ArtifactSelector> blackList = configuration.getIgnoreForSourceResolving();
        if (blackList != null) {
            BlacklistFilter configFilter = new BlacklistFilter(blackList);
            for (Artifact artifact : artifacts) {
                if (!configFilter.passed(artifact)) {
                    artifact.setFlag(Artifact.IS_IGNORE_FOR_DOWNLOAD_KEY);
                    reporter2.add(artifact,
                            MessageType.IGNORE_FOR_ARTIFACTRESOLVING, "The Artifact will not be downloaded.");
                }
            }
        }
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        resolveOverride(context.getConfiguration(), intermediates, this.reporter);
        return intermediates;
    }
}
