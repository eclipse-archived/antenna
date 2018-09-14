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

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

/**
 * This class resolves the configuration, specified in the configuration file in
 * the artifacts list. - remove artifact from list - add artifact to list -
 * change attribute of artifact if specified in config.xml
 */
public class ConfigurationHandlerAdd extends AbstractProcessor {

    private final AntennaContext context;
    private IProcessingReporter reporter;

    /**
     * Adds the artifacts from the addArtifact section to an artifacts list.
     */
    public ConfigurationHandlerAdd(AntennaContext context) {
        this.context = context;
        this.reporter = context.getProcessingReporter();
    }

    private void addConfiguredArtifacts(Configuration configuration, Collection<Artifact> artifacts,
            IProcessingReporter reporter) {
        configuration.getAddArtifact()
                .stream()
                .map(this::cleanupArtifact)
                .peek(artifact -> reporter.addProcessingMessage(artifact.getArtifactIdentifier(), MessageType.ADD_ARTIFACT,
                        "Artifact was added to artifacts list."))
                .forEach(artifacts::add);
    }

    private Artifact cleanupArtifact(Artifact artifact) {
        if (artifact.getPathnames() == null) {
            artifact.setPathnames(new String[0]);
        }
        if (artifact.getDeclaredLicenses() == null) {
            artifact.setDeclaredLicenses(new License());
        }
        if (artifact.getObservedLicenses() == null) {
            artifact.setObservedLicenses(new License());
        }
        if (artifact.getOverriddenLicenses() == null) {
            artifact.setOverriddenLicenses(new License());
        }
        if (artifact.getMatchState() == null) {
            artifact.setMatchState(MatchState.EXACT);
        }
        return artifact;
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        addConfiguredArtifacts(context.getConfiguration(), intermediates, this.reporter);
        return intermediates;
    }
}
