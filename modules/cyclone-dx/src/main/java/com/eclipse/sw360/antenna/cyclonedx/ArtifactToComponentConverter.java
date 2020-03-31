/*
 * Copyright (c) Robert Bosch Manufacturing Solutions GmbH 2020.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package com.eclipse.sw360.antenna.cyclonedx;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;

import org.cyclonedx.model.Component;
import org.cyclonedx.model.Hash;
import org.cyclonedx.model.LicenseChoice;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Converts an {@link Artifact} to a {@link Component}.
 */
public final class ArtifactToComponentConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ArtifactToComponentConverter.class);

    private ArtifactToComponentConverter(){
        // only static methods - no instance needed
    }

    /**
     * Converts an {@link Artifact} to a {@link Component}.
     *
     * It turns the supported coordinates from Antenna into CycloneDX information. This includes package URL, license information and hashes.
     * As Antenna will have to deal with incomplete information from scan sources, this may lead to incomplete information in the created {@link Component} as well.
     * If a meaningful {@link Component} cannot be created this will throw an exception.
     *
     * @param artifact A non-null artifact.
     * @return a component with matching information.
     */
    public static Component toComponent(Artifact artifact) {
        if (artifact == null) {
            throw new IllegalArgumentException("Artifact must not be null");
        }

        Component c = new Component();
        c.setType(Component.Type.LIBRARY);

        setCoordinatesForComponent(artifact, c);

        LicenseInformation licenses = ArtifactLicenseUtils.getFinalLicenses(artifact);
        addLicensesToComponent(licenses.getLicenses(), c);

        addHashesToComponent(artifact, c);

        if (c.getPurl() == null) {
            createGenericPurl(c, artifact);
        }

        return c;
    }

    private static void createGenericPurl(Component c, Artifact artifact) {
        try {
            ArtifactFilename artifactFilename = artifact.askFor(ArtifactFilename.class)
                    .orElseThrow(() -> new IllegalStateException("No artifactFilename available for artifact: " + artifact));

            ArtifactFilename.ArtifactFilenameEntry filenameEntry = artifactFilename.getBestFilenameEntryGuess()
                    .orElseThrow(() -> new IllegalStateException("No filename entry found for artifact: " + artifact));

            c.setName(filenameEntry.getFilename());
            PackageURL pUrl = PackageURLBuilder.aPackageURL()
                    .withType(PackageURL.StandardTypes.GENERIC)
                    .withName(filenameEntry.getFilename())
                    .withQualifier("download_url", "file://" + filenameEntry.getFilename())
                    .withQualifier("checksum", filenameEntry.getHashAlgorithm() + ":" + filenameEntry.getHash())
                    .build();
            c.setPurl(pUrl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalStateException("Unable to create a generic packageUrl for artifact " + artifact, e);
        }
    }

    private static void addHashesToComponent(Artifact artifact, Component c) {
        List<Hash> hashes = new ArrayList<>();
        c.setHashes(hashes);

        artifact.askFor(ArtifactFilename.class).ifPresent((artifactFilename) -> {
            Set<ArtifactFilename.ArtifactFilenameEntry> entries = artifactFilename.getArtifactFilenameEntries();
            for (ArtifactFilename.ArtifactFilenameEntry entry : entries) {
                createHashFor(entry).ifPresent(hashes::add);
            }
        });
    }

    protected static Optional<Hash> createHashFor(ArtifactFilename.ArtifactFilenameEntry entry) {
        Hash.Algorithm algorithm = matchAlgorithm(entry.getHashAlgorithm());
        return algorithm != null ? Optional.of(new Hash(algorithm, entry.getHash())) : Optional.empty();
    }

    private static Hash.Algorithm matchAlgorithm(String hashAlgorithm) {
        for (Hash.Algorithm alg : Hash.Algorithm.values()) {
            if (alg.getSpec().equalsIgnoreCase(hashAlgorithm)) {
                return alg;
            }
        }
        LOG.debug("Could not match hash algorithm {} to a supported one in CycloneDX.", hashAlgorithm);
        return null;
    }

    private static void setCoordinatesForComponent(Artifact artifact, Component component) {
        Optional<Coordinate> coordinate = artifact.askFor(ArtifactCoordinates.class).map(ArtifactCoordinates::getMainCoordinate);
        if (coordinate.isPresent()) {
            Coordinate co = coordinate.get();
            component.setName(co.getName());
            component.setGroup(co.getNamespace());
            component.setVersion(co.getVersion());
            component.setPurl(co.getPackageURL().toString());
        }
    }

    private static void addLicensesToComponent(List<License> antennaLicenses, Component c) {
        LicenseChoice licenseChoice = new LicenseChoice();
        c.setLicenseChoice(licenseChoice);
        for (License antennaLicense : antennaLicenses) {
            org.cyclonedx.model.License license = new org.cyclonedx.model.License();
            license.setName(antennaLicense.getCommonName());
            license.setId(antennaLicense.getId());
            licenseChoice.addLicense(license);
        }
    }

}
