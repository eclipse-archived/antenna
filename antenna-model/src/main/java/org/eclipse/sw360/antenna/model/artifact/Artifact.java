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

package org.eclipse.sw360.antenna.model.artifact;

import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import java.nio.file.Path;
import java.util.Optional;

/*
 * This class wraps ArtifactCore for convenience
 * All information should still be stored in ArtifactCore, and this class should only expose methods
 */
public class Artifact extends ArtifactCore {
    public static final String IS_PROPRIETARY_FLAG_KEY = "isProprietary";
    public static final String IS_MODIFIED_BY_CONFIGURATION_FLAG_KEY = "isModifiedByConfiguration";
    public static final String IS_IGNORE_FOR_DOWNLOAD_KEY = "isIgnoreForDownload";

    public Artifact() {
    }

    public Artifact(String analysisSource) {
        super(analysisSource);
    }

    @Override
    public Artifact addFact(ArtifactFact artifactFact) {
        return (Artifact) super.addFact(artifactFact);
    }

    @Override
    public Artifact addFact(ArtifactFactBuilder artifactFactBuilder) {
        return (Artifact) super.addFact(artifactFactBuilder);
    }

    @Override
    public Artifact setFlag(String key) {
        return (Artifact) super.setFlag(key);
    }

    @Override
    public Artifact setFlag(String key, boolean value) {
        return (Artifact) super.setFlag(key, value);
    }


    public boolean isProprietary() {
        return getFlag(IS_PROPRIETARY_FLAG_KEY);
    }

    public Artifact setProprietary(boolean isProprietary) {
        return setFlag(IS_PROPRIETARY_FLAG_KEY, isProprietary);
    }

    public Optional<Path> getFile() {
        return askForGet(ArtifactFile.class);
    }

    public Optional<Path> getSourceFile() {
        return askForGet(ArtifactSourceFile.class);
    }

    public void setMatchState(MatchState matchState) {
        addFact(new ArtifactMatchingMetadata(matchState));
    }

    public MatchState getMatchState() {
        return askFor(ArtifactMatchingMetadata.class)
                .map(ArtifactMatchingMetadata::getMatchState)
                .orElse(MatchState.UNKNOWN);
    }

    public boolean isPotentialDuplicateOf(Artifact compareA) {
        return this.getArtifactIdentifiers().stream()
                .anyMatch(identifier ->
                        compareA.getArtifactIdentifiers().stream()
                                .anyMatch(compareIdentifier ->
                                        compareIdentifier.matches(identifier)
                                                || identifier.matches(compareIdentifier)
                                )
                );
    }
}
