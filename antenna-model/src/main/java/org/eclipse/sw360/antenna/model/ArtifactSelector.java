/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.model;

import org.eclipse.sw360.antenna.model.xml.generated.*;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ArtifactSelector to compare artifacts.
 */
public class ArtifactSelector {

    private ArtifactIdentifier artifactIdentifier;

    public ArtifactSelector(ArtifactIdentifier artifactIdentifier) {
        this.artifactIdentifier = artifactIdentifier;
    }

    public ArtifactSelector(XmlArtifactSelector xmlArtifactSelector) {
        this.artifactIdentifier = xmlArtifactSelector.getArtifactIdentifier();
    }

    public boolean matches(Artifact inputArtifact) {
        ArtifactIdentifier inputArtifactIdentifier = inputArtifact.getArtifactIdentifier();
        return matches(inputArtifactIdentifier);
    }

    public boolean matches(ArtifactIdentifier inputArtifactIdentifier) {
        return compareStringFieldsAsRegex(artifactIdentifier, inputArtifactIdentifier,
                Stream.of((Function<ArtifactIdentifier,String>) ArtifactIdentifier::getFilename,
                        ArtifactIdentifier::getHash)
                        .collect(Collectors.toList())) &&
                matches(inputArtifactIdentifier.getMavenCoordinates()) &&
                matches(inputArtifactIdentifier.getBundleCoordinates()) &&
                matches(inputArtifactIdentifier.getJavaScriptCoordinates());
    }

    private boolean compareStringsAsRegex(String regex, String input) {
        if(regex == null) {
            return true;
        }
        return Pattern.matches(
                regex.trim().replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*?"),
                input != null ? input : "");
    }

    private <T> boolean compareStringFieldsAsRegex(T needle, T input, List<Function<T,String>> getters){
        return getters.stream()
                .allMatch(getter -> compareStringsAsRegex(getter.apply(needle), getter.apply(input)));
    }

    private boolean matches(MavenCoordinates inputMavenCoordinates) {
        MavenCoordinates mavenCoordinates = artifactIdentifier.getMavenCoordinates();
        if(mavenCoordinates == null) {
            return  true;
        }
        return compareStringFieldsAsRegex(mavenCoordinates, inputMavenCoordinates,
                Stream.of((Function<MavenCoordinates,String>) MavenCoordinates::getGroupId,
                        MavenCoordinates::getArtifactId,
                        MavenCoordinates::getVersion)
                        .collect(Collectors.toList()));
    }


    private boolean matches(BundleCoordinates inputBundleCoordinates) {
        BundleCoordinates bundleCoordinates = artifactIdentifier.getBundleCoordinates();
        if(bundleCoordinates == null) {
            return true;
        }
        return compareStringFieldsAsRegex(bundleCoordinates, inputBundleCoordinates,
                Stream.of((Function<BundleCoordinates,String>) BundleCoordinates::getSymbolicName,
                        BundleCoordinates::getBundleVersion)
                        .collect(Collectors.toList()));
    }

    private boolean matches(JavaScriptCoordinates inputJavaScriptCoordinates) {
        JavaScriptCoordinates javaScriptCoordinates = artifactIdentifier.getJavaScriptCoordinates();
        if(javaScriptCoordinates == null) {
            return true;
        }
        return compareStringFieldsAsRegex(javaScriptCoordinates, inputJavaScriptCoordinates,
                Stream.of((Function<JavaScriptCoordinates,String>) JavaScriptCoordinates::getArtifactId,
                        JavaScriptCoordinates::getName,
                        JavaScriptCoordinates::getVersion)
                        .collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return artifactIdentifier.toString();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof ArtifactSelector) {
            ArtifactSelector otherArtifactSelector = (ArtifactSelector) other;
            return this.artifactIdentifier.equals(otherArtifactSelector.artifactIdentifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return artifactIdentifier.hashCode() * 31;
    }
}
