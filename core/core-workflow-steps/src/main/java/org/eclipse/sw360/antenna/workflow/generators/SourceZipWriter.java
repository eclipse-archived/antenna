/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017, 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.workflow.generators;

import org.eclipse.sw360.antenna.analysis.filter.AllowAllArtifactsFilter;
import org.eclipse.sw360.antenna.analysis.filter.MatchStateArtifactFilter;
import org.eclipse.sw360.antenna.api.IArtifactFilter;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.workflow.AbstractGenerator;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The SourceZipWriter creates a zip file containing the given Sources.
 */
public class SourceZipWriter extends AbstractGenerator {
    private static final String SOURCE_ZIP_PATH_KEY = "source.zip.path";

    private IArtifactFilter notAllowed;
    private IArtifactFilter unknownMatchStateFilter;
    private Path sourceZipPath;

    public SourceZipWriter() {
        this.workflowStepOrder = 700;
    }

    @Override
    public Map<String, IAttachable> produce(Collection<Artifact> artifacts) {
        return new SourceZipWriterImpl(sourceZipPath, notAllowed, unknownMatchStateFilter, reporter)
                .produce(artifacts);
    }

    @Override
    public void configure(Map<String, String> configMap) {
        this.reporter = context.getProcessingReporter();
        this.notAllowed = new AllowAllArtifactsFilter();
        Set<MatchState> blacklistUnknown = new HashSet<>();
        blacklistUnknown.add(MatchState.UNKNOWN);
        this.unknownMatchStateFilter = new MatchStateArtifactFilter(blacklistUnknown);

        if (configMap.containsKey(SOURCE_ZIP_PATH_KEY)) {
            this.sourceZipPath = Paths.get(getConfigValue(SOURCE_ZIP_PATH_KEY, configMap));
        } else {
            this.sourceZipPath = context.getToolConfiguration().getAntennaTargetDirectory().resolve("sources.zip");
        }
    }
}
