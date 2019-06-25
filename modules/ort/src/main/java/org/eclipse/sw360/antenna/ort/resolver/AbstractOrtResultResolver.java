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
package org.eclipse.sw360.antenna.ort.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactMatchingMetadata;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.GenericArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;

import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractOrtResultResolver implements Function<JsonNode, Artifact> {

    @Override
    public Artifact apply(JsonNode result) {
        Artifact a =  new Artifact("OrtResult")
                .addFact(new ArtifactMatchingMetadata(MatchState.EXACT));
        mapCoordinates(result.get("id").textValue()).ifPresent(a::addFact);
        mapSourceUrl(result).ifPresent(a::addFact);
        return a;
    }

    public abstract Optional<ArtifactSourceUrl> mapSourceUrl(JsonNode result);

    public Optional<ArtifactCoordinates> mapCoordinates(String ortIdentifier) {
        String[] ortIdentifierSeparate = ortIdentifier.split(":");
        if (ortIdentifierSeparate.length < 3) {
            return Optional.empty();
        }
        String name = ortIdentifierSeparate[ortIdentifierSeparate.length - 2];
        String version = ortIdentifierSeparate[ortIdentifierSeparate.length - 1];
        switch (ortIdentifierSeparate[0]) {
            case "nuget":
            case "dotnet":
                return Optional.of(mapDotNetCoordinates(name, version));
            case "Maven":
                return Optional.of(mapMavenCoordinates(ortIdentifierSeparate[1], name, version));
            case "NPM":
                return Optional.of(mapJavaScriptCoordinates(name, version));
            default:
                return Optional.of(mapSimpleCoordinates(name, version));
        }
    }

    static private ArtifactCoordinates mapDotNetCoordinates(String name, String version) {
        DotNetCoordinates.DotNetCoordinatesBuilder c = new DotNetCoordinates.DotNetCoordinatesBuilder();
        c.setPackageId(name);
        c.setVersion(version);
        return c.build();
    }

    static private ArtifactCoordinates mapMavenCoordinates(String namespace, String name, String version) {
        MavenCoordinates.MavenCoordinatesBuilder c = new MavenCoordinates.MavenCoordinatesBuilder();
        c.setGroupId(namespace);
        c.setVersion(version);
        c.setArtifactId(name);
        return c.build();
    }

    static private ArtifactCoordinates mapJavaScriptCoordinates(String name, String version) {
        JavaScriptCoordinates.JavaScriptCoordinatesBuilder c = new JavaScriptCoordinates.JavaScriptCoordinatesBuilder();
        c.setName(name);
        c.setVersion(version);
        c.setArtifactId(name + "-" + version);
        return c.build();
    }

    static private ArtifactCoordinates mapSimpleCoordinates(String name, String version) {
        return new GenericArtifactCoordinates(name, version);
    }
}
