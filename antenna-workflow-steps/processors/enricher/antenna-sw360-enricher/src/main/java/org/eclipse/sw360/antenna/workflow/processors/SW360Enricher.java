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

package org.eclipse.sw360.antenna.workflow.processors;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.AntennaException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseOperator;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseStatement;
import org.eclipse.sw360.antenna.sw360.SW360MetaDataReceiver;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SW360Enricher extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360Enricher.class);
    private IProcessingReporter reporter;

    private static final String REST_SERVER_URL_KEY = "rest.server.url";
    private static final String AUTH_SERVER_URL_KEY = "auth.server.url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    private SW360MetaDataReceiver connector;

    @Override
    public void configure(Map<String, String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);

        reporter = context.getProcessingReporter();

        String sw360RestServerUrl = getConfigValue(REST_SERVER_URL_KEY, configMap);
        String sw360AuthServerUrl = getConfigValue(AUTH_SERVER_URL_KEY, configMap);
        String sw360User = getConfigValue(USERNAME_KEY, configMap);
        String sw360Password = getConfigValue(PASSWORD_KEY, configMap);
        connector = new SW360MetaDataReceiver(sw360RestServerUrl, sw360AuthServerUrl, sw360User, sw360Password);
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) throws AntennaException {
        for (Artifact artifact : intermediates) {
            Optional<SW360Release> release = connector.findReleaseForArtifact(artifact);
            if (release.isPresent()) {
                updateLicenses(artifact, release.get());
            } else {
                warnAndReport(artifact, "No SW360 release found for artifact.");
            }
        }
        return intermediates;
    }

    private void updateLicenses(Artifact artifact, SW360Release release) {
        List<License> artifactLicenses = artifact.getFinalLicenses().getLicenses();
        List<SW360SparseLicense> releaseLicenses = release.get_Embedded().getLicenses();

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

    private void setLicensesForArtifact(Artifact artifact, List<SW360SparseLicense> licenses) {
        LicenseStatement licenseStatement = new LicenseStatement();
        for (SW360SparseLicense sparseLicense : licenses) {
            try {
                Optional<SW360License> licenseDetails = connector.getLicenseDetails(sparseLicense);
                if (!licenseDetails.isPresent()) {
                    LOGGER.warn("Could not get details for license " + sparseLicense.getFullName());
                    continue;
                }
                License license = new License();
                license.setName(licenseDetails.get().getShortName());
                license.setLongName(licenseDetails.get().getFullName());
                license.setText(licenseDetails.get().getText());

                LicenseStatement newLicenseStatement = new LicenseStatement();
                newLicenseStatement.setLeftStatement(licenseStatement);
                newLicenseStatement.setOp(LicenseOperator.AND);
                newLicenseStatement.setRightStatement(license);

                licenseStatement = newLicenseStatement;
            } catch (AntennaException e) {
                LOGGER.error("Exception while getting license details from SW360", e);
            }
        }
        artifact.setConfiguredLicense(licenseStatement);
    }

    private void warnAndReport(Artifact artifact, String message) {
        LOGGER.warn(message);
        reporter.addProcessingMessage(
                artifact.getArtifactIdentifier(),
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
}