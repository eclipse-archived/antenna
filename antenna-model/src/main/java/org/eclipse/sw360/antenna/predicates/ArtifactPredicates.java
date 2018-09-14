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
package org.eclipse.sw360.antenna.predicates;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.BundleCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MavenCoordinates;

public class ArtifactPredicates {
    private ArtifactPredicates() {
        // only statics
    }

    public static boolean hasNoCoordinates(Artifact artifact) {
        return !(hasMavenCoordinates(artifact) ||
                hasBundleCoordinates(artifact) ||
                hasJavaScriptCoordinates(artifact));
    }

    public static boolean hasMavenCoordinates(Artifact artifact) {
        final MavenCoordinates mavenCoordinates = artifact.getArtifactIdentifier().getMavenCoordinates();
        return mavenCoordinates.getGroupId() != null &&
                mavenCoordinates.getArtifactId() != null &&
                mavenCoordinates.getVersion() != null;
    }

    public static boolean hasBundleCoordinates(Artifact artifact) {
        final BundleCoordinates bundleCoordinates = artifact.getArtifactIdentifier().getBundleCoordinates();
        return bundleCoordinates.getBundleVersion() != null &&
                bundleCoordinates.getSymbolicName() != null;
    }

    public static boolean hasJavaScriptCoordinates(Artifact artifact) {
        final JavaScriptCoordinates javaScriptCoordinates = artifact.getArtifactIdentifier().getJavaScriptCoordinates();
        return javaScriptCoordinates.getArtifactId() != null &&
                javaScriptCoordinates.getName() != null &&
                javaScriptCoordinates.getVersion() != null;
    }

    public static boolean hasSources(Artifact artifact) {
        return artifact.getMvnSourceJar() != null || artifact.getP2SourceJar() != null;
    }

    public static boolean hasNoSources(Artifact artifact) {
        return ! hasSources(artifact);
    }
}
