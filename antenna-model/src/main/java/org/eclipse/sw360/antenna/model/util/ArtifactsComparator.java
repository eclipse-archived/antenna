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

package org.eclipse.sw360.antenna.model.util;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.ArtifactIdentifier;

import java.util.Comparator;

/**
 * Used to compare two artifacts.
 */
public class ArtifactsComparator implements Comparator<Artifact> {

    private String getNameOf(Artifact artifact) {
        ArtifactIdentifier artifactIdentifier = artifact.getArtifactIdentifier();

        final String filename = artifactIdentifier.getFilename();
        if(filename != null) {
            return filename;
        }

        if(artifactIdentifier.getMavenCoordinates() != null) {
            final String mavenArtifactId = artifactIdentifier.getMavenCoordinates().getArtifactId();
            if(mavenArtifactId != null) {
                return mavenArtifactId;
            }
        }

        if(artifactIdentifier.getBundleCoordinates() != null) {
            final String bundleArtifactId = artifactIdentifier.getBundleCoordinates().getSymbolicName();
            if(bundleArtifactId != null) {
                return bundleArtifactId;
            }
        }

        return "";
    }

    /**
     * Compares the filenames of the given artifacts using the compare method of
     * String. If the filename is null, the artifactIdentifier is taken as the
     * CompareValue;
     *
     * @return value of default compare method.
     */
    @Override
    public int compare(Artifact value1, Artifact value2) {
        String value1Name = getNameOf(value1);
        String value2Name = getNameOf(value2);
        int compareValue = String.CASE_INSENSITIVE_ORDER.compare(value1Name, value2Name);
        return (compareValue != 0) ? compareValue : value1Name.compareTo(value2Name);
    }
}
