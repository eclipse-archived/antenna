/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFactWithPayload;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SW360AttachmentAdapterUtils {

    public static Map<Path, String> getAttachmentsFromArtifact(Artifact artifact) {
        Map<Path, String> attachments = new HashMap<>();

        getSourceFile(artifact).map(sourceFile -> attachments.put(sourceFile, "SOURCE"));

        return attachments;
    }

    private static Optional<Path> getSourceFile(Artifact artifact) {
        Optional<ArtifactSourceFile> artifactSourceFile = artifact.askFor(ArtifactSourceFile.class);
        return artifactSourceFile.map(ArtifactFactWithPayload::get);
    }
}
