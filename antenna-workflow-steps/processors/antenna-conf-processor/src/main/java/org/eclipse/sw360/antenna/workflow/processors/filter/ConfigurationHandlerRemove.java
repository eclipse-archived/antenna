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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sw360.antenna.analysis.filter.BlacklistFilter;
import org.eclipse.sw360.antenna.analysis.filter.ProprietaryArtifactFilter;
import org.eclipse.sw360.antenna.api.IArtifactFilter;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.ArtifactSelector;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.reporting.MessageType;

/**
 * This class removes artifact from the list if artifact is proprietary or if it
 * is defined in the remove artifacts section.
 */
public class ConfigurationHandlerRemove extends AbstractProcessor {
    private final AntennaContext context;
    private IProcessingReporter reporter;

    public ConfigurationHandlerRemove(AntennaContext context) {
        this.context = context;
        this.reporter = context.getProcessingReporter();
    }

    private void removeProprietaryArtifacts(Collection<Artifact> artifacts, Configuration configuration) {
        IArtifactFilter isProprietaryFilter = new ProprietaryArtifactFilter();
        List<Artifact> remove = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            boolean overridePropertyValue = getProprietaryOverrideValue(configuration, artifact);
            if (!isProprietaryFilter.passed(artifact)) {
                if (overridePropertyValue) {
                    reporter.addProcessingMessage(artifact.getArtifactIdentifier(), MessageType.ARTIFACT_IS_PROPRIETARY,
                            "Artifact is removed from the artifacts list for processing, since it was classified as proprietary.");
                } else {
                    remove.add(artifact);
                }
            }
        }
        artifacts.removeAll(remove);
    }

    private void removeArtifacts(Collection<Artifact> artifacts, Configuration configuration) {
        List<ArtifactSelector> removeArtifact = configuration.getRemoveArtifact();
        List<Artifact> remove = new ArrayList<>();
        BlacklistFilter configFilter = new BlacklistFilter(removeArtifact);
        for (Artifact artifact : artifacts) {
            if (!configFilter.passed(artifact)) {
                remove.add(artifact);
                reporter.addProcessingMessage(artifact.getArtifactIdentifier(), MessageType.REMOVE_ARTIFACT,
                        "Artifact is removed from artifacts list for processing.");
            }
        }
        artifacts.removeAll(remove);
    }

    @SuppressWarnings("boxing")
    private boolean getProprietaryOverrideValue(Configuration configuration, Artifact artifact) {
        Map<ArtifactSelector, Artifact> override = configuration.getOverride();
        Set<ArtifactSelector> keySet = override.keySet();
        Iterator<ArtifactSelector> iterator = keySet.iterator();
        Artifact generatedArtifact = null;
        while (iterator.hasNext()) {
            ArtifactSelector artifactExample = iterator.next();
            if (artifactExample.matches(artifact)) {
                generatedArtifact = override.get(artifactExample);
            }
        }
        boolean proprietary = artifact.isProprietary();
        if (null != generatedArtifact) {
            proprietary = generatedArtifact.isProprietary();
        }
        return proprietary;
    }

    @Override
    public void cleanup() {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        Configuration config = context.getConfiguration();
        removeArtifacts(intermediates, config);
        removeProprietaryArtifacts(intermediates, config);
        return intermediates;
    }

}
