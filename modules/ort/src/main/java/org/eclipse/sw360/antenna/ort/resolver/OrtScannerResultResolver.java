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
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactSourceUrl;
import org.eclipse.sw360.antenna.model.artifact.facts.CopyrightStatement;
import org.eclipse.sw360.antenna.model.artifact.facts.ObservedLicenseInformation;
import org.eclipse.sw360.antenna.model.util.Utils;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrtScannerResultResolver extends AbstractOrtResultResolver {
    @Override
    public Artifact apply(JsonNode result) {
        Artifact a = super.apply(result);
        mapCopyrights(result).ifPresent(a::addFact);
        mapObservedLicense(result).ifPresent(a::addFact);
        return a;
    }

    @Override
    public Optional<ArtifactSourceUrl> mapSourceUrl(JsonNode result) {
        final Stream<String> keys = Stream.of("vcs_info", "source_artifact", "original_vcs_info");

        return Optional.of(result.get("results").elements())
                .filter(Iterator::hasNext)
                .map(i -> i.next().get("provenance"))
                .flatMap(jsonNode -> keys
                        .map(key -> Optional.ofNullable(jsonNode.get(key))
                                .map(value -> value.get("url").textValue()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                )
                .map(ArtifactSourceUrl::new);
    }

    private Optional<Stream<JsonNode>> iterateOverLicenseFindingsFromJsonNode(JsonNode result) {
        return Optional.of(result.get("results"))
                .filter(JsonNode::isArray)
                .map(JsonNode::elements)
                .map(Utils::iteratorToCollection)
                .map(Collection::stream)
                .map(s -> s.map(e -> e.get("summary").get("license_findings"))
                        .map(JsonNode::elements)
                        .map(Utils::iteratorToCollection)
                        .flatMap(Collection::stream));
    }

    private Optional<ObservedLicenseInformation> mapObservedLicense(JsonNode result) {
        return iterateOverLicenseFindingsFromJsonNode(result)
                .map(s -> s.map(node -> node.get("license").textValue())
                        .collect(Collectors.toList()))
                .map(LicenseSupport::mapLicenses)
                .map(ObservedLicenseInformation::new);
    }

    private Optional<CopyrightStatement> mapCopyrights(JsonNode result) {
        return iterateOverLicenseFindingsFromJsonNode(result)
                .flatMap(s -> s.flatMap(node -> Utils.iteratorToCollection(node.get("copyrights").elements())
                        .stream())
                        .map(JsonNode::textValue)
                        .map(CopyrightStatement::new)
                        .reduce(CopyrightStatement::mergeWith)
                );
    }
}
