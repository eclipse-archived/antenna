/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.workflow.processors;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.*;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360CoordinateKeysToArtifactCoordinates;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360ReleaseEmbedded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SW360Enricher extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360Enricher.class);
    private IProcessingReporter reporter;

    private static final String REST_SERVER_URL_KEY = "rest.server.url";
    private static final String AUTH_SERVER_URL_KEY = "auth.server.url";
    private static final String USERNAME_KEY = "user.id";
    private static final String PASSWORD_KEY = "user.password";
    private static final String CLIENT_USER_KEY = "client.id";
    private static final String CLIENT_PASSWORD_KEY = "client.password";
    private static final String PROXY_USE = "proxy.use";

    private SW360MetaDataReceiver connector;

    public SW360Enricher() {
        this.workflowStepOrder = 1300;
    }

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);

        reporter = context.getProcessingReporter();

        String sw360RestServerUrl = getConfigValue(REST_SERVER_URL_KEY, configMap);
        String sw360AuthServerUrl = getConfigValue(AUTH_SERVER_URL_KEY, configMap);
        String sw360User = getConfigValue(USERNAME_KEY, configMap);
        String sw360Password = getConfigValue(PASSWORD_KEY, configMap);
        String sw360ClientUser = getConfigValue(CLIENT_USER_KEY, configMap);
        String sw360ClientPassword = getConfigValue(CLIENT_PASSWORD_KEY, configMap);
        boolean sw360ProxyUse = Boolean.parseBoolean(getConfigValue(PROXY_USE, configMap, "false"));
        String sw360ProxyHost = context.getToolConfiguration().getProxyHost();
        int sw360ProxyPort = context.getToolConfiguration().getProxyPort();
        connector = new SW360MetaDataReceiver(sw360RestServerUrl, sw360AuthServerUrl, sw360User, sw360Password, sw360ClientUser, sw360ClientPassword, sw360ProxyUse, sw360ProxyHost, sw360ProxyPort);
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) throws AntennaException {
        for (Artifact artifact : intermediates) {
            Optional<SW360Release> release = connector.findReleaseForArtifact(artifact);
            if (release.isPresent()) {
                updateLicenses(artifact, release.get());
                addSourceUrlIfAvailable(artifact, release.get());
                addCPEIdIfAvailable(artifact, release.get());
                addClearingStateIfAvailable(artifact, release.get());
                mapExternalIdsOnSW360Release(release.get(), artifact);
            } else {
                warnAndReport(artifact, "No SW360 release found for artifact.");
            }
        }
        return intermediates;
    }

    private void addClearingStateIfAvailable(Artifact artifact, SW360Release release) {
        if (release.getClearingState() != null && !release.getClearingState().isEmpty()) {
            artifact.addFact(new ArtifactClearingState(
                    ArtifactClearingState.ClearingState.valueOf(release.getClearingState())));
        }
    }

    private void mapExternalIdsOnSW360Release(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getExternalIds() != null) {

            mapCoordinates(sw360Release, artifact);
            mapDeclaredLicense(sw360Release, artifact);
            mapObservedLicense(sw360Release, artifact);
            mapOriginalRepository(sw360Release, artifact);
            mapSwhId(sw360Release, artifact);
            mapHashes(sw360Release, artifact);
            mapChangeStatus(sw360Release, artifact);
            mapCopyrights(sw360Release, artifact);
        }
    }

    private void mapCopyrights(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getAdditionalData().containsKey(SW360Attributes.RELEASE_ADDITIONAL_DATA_COPYRIGHTS)) {
            String copyrights = sw360Release.getAdditionalData().get(SW360Attributes.RELEASE_ADDITIONAL_DATA_COPYRIGHTS);

            artifact.addFact(new CopyrightStatement(copyrights));
        }
    }

    private void mapChangeStatus(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getAdditionalData().containsKey(SW360Attributes.RELEASE_ADDITIONAL_DATA_CHANGESTATUS)) {
            String string_change_status = sw360Release.getAdditionalData().get(SW360Attributes.RELEASE_ADDITIONAL_DATA_CHANGESTATUS);

            ArtifactChangeStatus.ChangeStatus changeStatus = ArtifactChangeStatus.ChangeStatus.valueOf(string_change_status);

            artifact.addFact(new ArtifactChangeStatus(changeStatus));
        }
    }

    private void mapHashes(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getExternalIds().containsKey(SW360Attributes.RELEASE_EXTERNAL_ID_HASHES + "1")) {
            Set<String> hashesSet = new HashSet<>();

            sw360Release.getExternalIds().forEach((key, value) -> {
                if (key.startsWith(SW360Attributes.RELEASE_EXTERNAL_ID_HASHES)) {
                    hashesSet.add(value);
                }
            });
            hashesSet.forEach(hash ->
                    artifact.addFact(new ArtifactFilename(sw360Release.getName(), hash)));
        }
    }

    private void mapSwhId(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getExternalIds().containsKey(SW360Attributes.RELEASE_EXTERNAL_ID_SWHID)) {
            String swhId = sw360Release.getExternalIds().get(SW360Attributes.RELEASE_EXTERNAL_ID_SWHID);

            try {
                artifact.addFact(new ArtifactSoftwareHeritageID.Builder(swhId).build());
            } catch (AntennaException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    private void mapOriginalRepository(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getExternalIds().containsKey(SW360Attributes.RELEASE_EXTERNAL_ID_OREPO)) {
            artifact.addFact(new ArtifactReleaseTagURL(sw360Release.getExternalIds().get(SW360Attributes.RELEASE_EXTERNAL_ID_OREPO)));
        }
    }

    private void mapObservedLicense(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getAdditionalData().containsKey(SW360Attributes.RELEASE_ADDITIONAL_DATA_OLICENSES)) {
            License licenseStatement = makeLicenseStatementFromString(
                    sw360Release.getAdditionalData().get(SW360Attributes.RELEASE_ADDITIONAL_DATA_OLICENSES));

            artifact.addFact(new ObservedLicenseInformation(licenseStatement));
        }
    }

    private void mapDeclaredLicense(SW360Release sw360Release, Artifact artifact) {
        if (sw360Release.getAdditionalData().containsKey(SW360Attributes.RELEASE_ADDITIONAL_DATA_DLICENSES)) {
            License licenseStatement = makeLicenseStatementFromString(
                    sw360Release.getAdditionalData().get(SW360Attributes.RELEASE_ADDITIONAL_DATA_DLICENSES));

            artifact.addFact(new DeclaredLicenseInformation(licenseStatement));
        }
    }

    private License makeLicenseStatementFromString(String license) {
        License license1 = new License();
        license1.setName(license);

        return license1;
    }

    private void mapCoordinates(SW360Release sw360Release, Artifact artifact) {
        Set<String> keySet = sw360Release.getExternalIds().keySet();

        for (Class<? extends ArtifactCoordinates> key : SW360CoordinateKeysToArtifactCoordinates.getKeys()) {
            String coordinateType = SW360CoordinateKeysToArtifactCoordinates.get(key);
            if (keySet.contains(coordinateType)) {
                String coordinateValue = sw360Release.getExternalIds().get(coordinateType);
                String[] splitCoordiantes = coordinateValue.split(":");

                ArtifactCoordinates coordinates;
                if (splitCoordiantes.length == 3) {
                    coordinates = SW360CoordinateKeysToArtifactCoordinates.createArtifactCoordinates(splitCoordiantes[0], sw360Release.getName(), sw360Release.getVersion(), key);
                } else {
                    coordinates = SW360CoordinateKeysToArtifactCoordinates.createArtifactCoordinates("", sw360Release.getName(), sw360Release.getVersion(), key);
                }
                artifact.addFact(coordinates);
            }
        }
    }

    private void addSourceUrlIfAvailable(Artifact artifact, SW360Release release) {
        if (release.getDownloadurl() != null && !release.getDownloadurl().isEmpty()) {
            artifact.addFact(new ArtifactSourceUrl(release.getDownloadurl()));
        }
    }

    private void updateLicenses(Artifact artifact, SW360Release release) {
        List<License> artifactLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact).getLicenses();
        final SW360ReleaseEmbedded embedded = release.get_Embedded();
        if (embedded == null) {
            return;
        }
        List<SW360SparseLicense> releaseLicenses = embedded.getLicenses();

        if (!artifactLicenses.isEmpty()) {
            if (releaseLicenses.isEmpty()) {
                LOGGER.info("License information available in antenna but not in SW360.");
            } else {
                if (hasDifferentLicenses(artifactLicenses, releaseLicenses)) {
                    warnAndReport(artifact, "Licenses are different between artifact and SW360. Overwriting with licenses from SW360.");
                    logLicenseDifference(artifactLicenses, releaseLicenses);

                    setLicensesForArtifact(artifact, releaseLicenses);
                }
            }
        } else {
            if (!releaseLicenses.isEmpty()) {
                LOGGER.info("License information is missing in artifact. Adding licenses from SW360.");
                logLicenseDifference(artifactLicenses, releaseLicenses);

                setLicensesForArtifact(artifact, releaseLicenses);
            }
        }
    }

    private boolean hasDifferentLicenses(List<License> artifactLicenses, List<SW360SparseLicense> releaseLicenses) {
        List<String> artifactLicenseNames = artifactLicenses.stream()
                .map(License::getName)
                .collect(Collectors.toList());
        List<String> releaseLicenseNames = releaseLicenses.stream()
                .map(SW360SparseLicense::getShortName)
                .collect(Collectors.toList());
        return !(releaseLicenseNames.containsAll(artifactLicenseNames) && artifactLicenseNames.containsAll(releaseLicenseNames));
    }

    private License makeLicenseFromLicenseDetails(SW360License licenseDetails) {
        License license = new License();
        license.setName(licenseDetails.getShortName());
        license.setLongName(licenseDetails.getFullName());
        license.setText(licenseDetails.getText());
        return license;
    }

    private Optional<License> enrichSparseLicenseFromSW360(SW360SparseLicense sparseLicense) throws AntennaException {
        Optional<License> licenseDetails = connector.getLicenseDetails(sparseLicense)
                .map(this::makeLicenseFromLicenseDetails);
        if (!licenseDetails.isPresent()) {
            LOGGER.warn("Could not get details for license " + sparseLicense.getFullName());
        }
        return licenseDetails;
    }

    private LicenseStatement appendToLicenseStatement(LicenseStatement licenseStatement, License license) {
        LicenseStatement newLicenseStatement = new LicenseStatement();
        newLicenseStatement.setLeftStatement(licenseStatement);
        newLicenseStatement.setOp(LicenseOperator.AND);
        newLicenseStatement.setRightStatement(license);
        return newLicenseStatement;
    }

    private void setLicensesForArtifact(Artifact artifact, List<SW360SparseLicense> licenses) {
        LicenseStatement licenseStatement = new LicenseStatement();
        for (SW360SparseLicense sparseLicense : licenses) {
            try {
                Optional<License> license = enrichSparseLicenseFromSW360(sparseLicense);
                if (license.isPresent()) {
                    licenseStatement = appendToLicenseStatement(licenseStatement, license.get());
                }
            } catch (AntennaException e) {
                LOGGER.error("Exception while getting license details from SW360 for license: " + sparseLicense.getFullName() + "(" + sparseLicense.getShortName() + ")", e);
            }
        }
        artifact.addFact(new ConfiguredLicenseInformation(licenseStatement));
    }

    private void warnAndReport(Artifact artifact, String message) {
        LOGGER.warn(message);
        reporter.add(
                artifact,
                MessageType.PROCESSING_FAILURE,
                message);
    }

    private void logLicenseDifference(List<License> artifactLicenses, List<SW360SparseLicense> releaseLicenses) {
        List<String> artifactLicenseNames = artifactLicenses.stream()
                .map(License::getName)
                .collect(Collectors.toList());
        List<String> releaseLicenseNames = releaseLicenses.stream()
                .map(SW360SparseLicense::getShortName)
                .collect(Collectors.toList());

        LOGGER.info("Artifact: '" + String.join("', '", artifactLicenseNames)
                + "' <-> SW360: '" + String.join("', '", releaseLicenseNames) + "'");
    }

    private void addCPEIdIfAvailable(Artifact artifact, SW360Release release) {
        final String CPE_PREFIX = "cpe:2.3:";
        final String OLD_CPE_PREFIX = "cpe:/";
        String cpeId = release.getCpeid();
        if (cpeId != null && (cpeId.startsWith(CPE_PREFIX) || cpeId.startsWith(OLD_CPE_PREFIX))) {
            artifact.addFact(new ArtifactCPE(cpeId));
        }
    }
}
