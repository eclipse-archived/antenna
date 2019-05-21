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

import org.eclipse.sw360.antenna.analysis.filter.BlacklistFilter;
import org.eclipse.sw360.antenna.analysis.filter.ProprietaryArtifactFilter;
import org.eclipse.sw360.antenna.api.IArtifactFilter;
import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.reporting.MessageType;

import java.util.*;

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
                    reporter.add(artifact, MessageType.ARTIFACT_IS_PROPRIETARY,
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
                reporter.add(artifact, MessageType.REMOVE_ARTIFACT,
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
        if (null != generatedArtifact) {
            return generatedArtifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY);
        }
        return artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY);
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
