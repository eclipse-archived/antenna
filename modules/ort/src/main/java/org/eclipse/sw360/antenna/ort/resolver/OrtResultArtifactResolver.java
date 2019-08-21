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

import com.here.ort.model.*;
import com.here.ort.model.Package;
import com.here.ort.model.config.PathExclude;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.artifact.facts.dotnet.DotNetCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.javaScript.JavaScriptCoordinates;
import org.eclipse.sw360.antenna.model.xml.generated.MatchState;
import org.eclipse.sw360.antenna.util.LicenseSupport;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrtResultArtifactResolver implements Function<Package, Artifact> {
    private SortedMap<Identifier, Map<LicenseFinding, List<PathExclude>>> licenseFindings;

    public OrtResultArtifactResolver(OrtResult result) {
        licenseFindings = result.collectLicenseFindings(false);
    }

    @Override
    public Artifact apply(Package pkg) {
        Artifact a = new Artifact("OrtResult")
                .addFact(new ArtifactMatchingMetadata(MatchState.EXACT));

        mapCoordinates(pkg).ifPresent(a::addFact);
        mapSourceUrl(pkg).ifPresent(a::addFact);

        mapDeclaredLicense(pkg).ifPresent(a::addFact);
        mapFilename(pkg).ifPresent(a::addFact);
        mapHomepage(pkg).ifPresent(a::addFact);

        mapObservedLicense(pkg).ifPresent(a::addFact);
        mapCopyrights(pkg).ifPresent(a::addFact);

        return a;
    }

    private static Optional<ArtifactCoordinates> mapCoordinates(Package pkg) {
        String namespace = pkg.getId().getNamespace();
        String name = pkg.getId().getName();
        String version = pkg.getId().getVersion();

        switch (pkg.getId().getType().toLowerCase()) {
            case "nuget":
            case "dotnet":
                return Optional.of(mapDotNetCoordinates(name, version));
            case "maven":
                return Optional.of(mapMavenCoordinates(namespace, name, version));
            case "npm":
                return Optional.of(mapJavaScriptCoordinates(name, version));
            default:
                return Optional.of(mapSimpleCoordinates(name, version));
        }
    }

    private static ArtifactCoordinates mapDotNetCoordinates(String name, String version) {
        return new DotNetCoordinates.DotNetCoordinatesBuilder()
                .setPackageId(name)
                .setVersion(version)
                .build();
    }

    private static ArtifactCoordinates mapMavenCoordinates(String namespace, String name, String version) {
        return new MavenCoordinates.MavenCoordinatesBuilder()
                .setGroupId(namespace)
                .setVersion(version)
                .setArtifactId(name)
                .build();
    }

    private static ArtifactCoordinates mapJavaScriptCoordinates(String name, String version) {
        return new JavaScriptCoordinates.JavaScriptCoordinatesBuilder()
                .setName(name)
                .setVersion(version)
                .setArtifactId(name + "-" + version)
                .build();
    }

    private static ArtifactCoordinates mapSimpleCoordinates(String name, String version) {
        return new GenericArtifactCoordinates(name, version);
    }

    private static Optional<ArtifactSourceUrl> mapSourceUrl(Package pkg) {
        final Stream<String> urls = Stream.of(pkg.getVcsProcessed().getUrl(), pkg.getSourceArtifact().getUrl());

        return urls.filter(u -> !u.isEmpty())
                .findFirst()
                .map(ArtifactSourceUrl::new);
    }

    private static Optional<DeclaredLicenseInformation> mapDeclaredLicense(Package pkg) {
        return Optional.of(pkg.getDeclaredLicenses())
                .map(LicenseSupport::mapLicenses)
                .map(DeclaredLicenseInformation::new);
    }

    private static Optional<ArtifactFilename> mapFilename(Package pkg) {
        final Stream<RemoteArtifact> artifacts = Stream.of(pkg.getSourceArtifact(), pkg.getBinaryArtifact());

        return artifacts
                .filter(a -> !a.getUrl().isEmpty())
                .findFirst()
                .map(a -> {
                    String fileName = a.getUrl();
                    String hash = a.getHash().getValue();
                    String hashAlgorithm = a.getHash().getAlgorithm().toString();
                    return new ArtifactFilename(fileName, hash, hashAlgorithm);
                });
    }

    private static Optional<ArtifactHomepage> mapHomepage(Package pkg) {
        return Optional.of(pkg.getHomepageUrl())
                .map(ArtifactHomepage::new);
    }

    private Optional<ObservedLicenseInformation> mapObservedLicense(Package pkg) {
        Collection<String> licenses = Optional.ofNullable(licenseFindings.get(pkg.getId()))
                .map(Map::keySet)
                .orElse(Collections.emptySet()).stream()
                .map(LicenseFinding::getLicense)
                .collect(Collectors.toList());

        return Optional.of(LicenseSupport.mapLicenses(licenses))
                .map(ObservedLicenseInformation::new);
    }

    private Optional<CopyrightStatement> mapCopyrights(Package pkg) {
        return Optional.ofNullable(licenseFindings.get(pkg.getId()))
                .map(Map::keySet)
                .orElse(Collections.emptySet()).stream()
                .map(LicenseFinding::getCopyrights)
                .flatMap(Collection::stream)
                .map(CopyrightFinding::getStatement)
                .map(CopyrightStatement::new)
                .reduce(CopyrightStatement::mergeWith);
    }
}
