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
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactHomepage;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.util.Utils;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrtAnalyzerResultResolver extends AbstractOrtResultResolver {
    @Override
    public Artifact apply(JsonNode result) {
        Artifact a = super.apply(result);
        mapDeclaredLicense(result).ifPresent(a::addFact);
        mapFilename(result).ifPresent(a::addFact);
        mapHomepage(result).ifPresent(a::addFact);
        return a;
    }


    private Optional<DeclaredLicenseInformation> mapDeclaredLicense(JsonNode result) {
        return Optional.of(result.get("declared_licenses"))
                .filter(JsonNode::isArray)
                .map(JsonNode::elements)
                .map(this::parseJsonElementToLicenseInformation)
                .map(DeclaredLicenseInformation::new);
    }

    private LicenseInformation parseJsonElementToLicenseInformation(Iterator<JsonNode> jsonNodeIterator) {
        return LicenseSupport.mapLicenses(Utils.iteratorToCollection(jsonNodeIterator).stream()
                .map(JsonNode::textValue)
                .collect(Collectors.toSet()));
    }

    @Override
    public Optional<ArtifactSourceUrl> mapSourceUrl(JsonNode result) {
        return Optional.ofNullable(result.get("source_artifact"))
                .map(v -> v.get("url").textValue())
                .map(ArtifactSourceUrl::new);
    }

    private Optional<ArtifactFilename> mapFilename(JsonNode result) {
        final Stream<String> keys = Stream.of("source_artifact", "binary_artifact");

        return keys.map(key ->
                Optional.ofNullable(result.get(key))
                        .filter(node -> !node.get("url").textValue().equals("")))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(ortArtifact -> {
                    String fileName = ortArtifact.get("url").textValue();
                    String hash = ortArtifact.get("hash").textValue();
                    String hashAlgorithm = ortArtifact.get("hash_algorithm").textValue();
                    return new ArtifactFilename(fileName, hash, hashAlgorithm);
                });
    }

    private Optional<ArtifactHomepage> mapHomepage(JsonNode result) {
        return Optional.ofNullable(result.get("homepage_url"))
                .map(JsonNode::textValue)
                .map(ArtifactHomepage::new);
    }
}
