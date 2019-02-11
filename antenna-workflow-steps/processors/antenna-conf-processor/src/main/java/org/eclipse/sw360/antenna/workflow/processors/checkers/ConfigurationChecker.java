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

package org.eclipse.sw360.antenna.workflow.processors.checkers;

import org.eclipse.sw360.antenna.api.IProcessingReporter;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.xml.generated.Issues;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Checks if a Configuration has unnecessary elements.
 */
public class ConfigurationChecker extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private IProcessingReporter reporter;
    private Configuration config;

    public ConfigurationChecker(IProcessingReporter reporter, Configuration config) {
        this.reporter = reporter;
        this.config = config;
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> intermediates) {
        LOGGER.info("Check for unnecessary configuration configuration ...");
        checkForUnnecessaryInformation(intermediates);
        LOGGER.info("Check for unnecessary configuration configuration... done.");
        return intermediates;
    }

    /**
     * Checks if a Configuration has unnecessary elements.
     *
     * @param artifacts
     *            List of artifacts, used as reference list for artifacts in
     *            configuration
     */
    private void checkForUnnecessaryInformation(Collection<Artifact> artifacts) {
        checkRemoveArtifact(artifacts);
        checkOverrideArtifact(artifacts);
        checkHandleSourceAsValid(artifacts);
        checkSetFinalLicense(artifacts);
        checkSourceResolving(artifacts);
        checkSecurityIssue(artifacts);
    }

    private void checkSourceResolving(Collection<Artifact> artifacts) {
        List<ArtifactSelector> ignoreForSourceResolving = config.getIgnoreForSourceResolving();
        check(artifacts, ignoreForSourceResolving, "source Resolving");
    }

    private void checkSecurityIssue(Collection<Artifact> artifacts) {
        Map<ArtifactSelector, Issues> securityIssues = config.getSecurityIssues();
        checkSecIssues(artifacts, securityIssues, "add security issues");
    }

    private void checkSetFinalLicense(Collection<Artifact> artifacts) {
        Map<ArtifactSelector, LicenseInformation> finalLicenses = config.getFinalLicenses();
        check(artifacts, finalLicenses, "set final license");
    }

    private void checkHandleSourceAsValid(Collection<Artifact> artifacts) {
        List<ArtifactSelector> validForIncompleteSources = config.getValidForIncompleteSources();
        check(artifacts, validForIncompleteSources, "source validation (incomplete sources)");
        List<ArtifactSelector> validForMissingSources = config.getValidForMissingSources();
        check(artifacts, validForMissingSources, "source validation (missing sources)");
    }

    private void checkOverrideArtifact(Collection<Artifact> artifacts) {
        Map<ArtifactSelector, Artifact> override = config.getOverride();
        check(artifacts, override, "override section");
    }

    private void checkRemoveArtifact(Collection<Artifact> artifacts) {
        List<ArtifactSelector> removeArtifact = config.getRemoveArtifact();
        check(artifacts, removeArtifact, "remove Artifact");
    }

    private <T> void check(Collection<Artifact> artifacts, Map<ArtifactSelector,T>  selectors, String type) {
        check(artifacts, selectors.keySet(), type);
    }

    private void checkSecIssues(Collection<Artifact> artifacts, Map<ArtifactSelector, Issues> securityIssues, String type) {
        List<ArtifactSelector> extractedSelectors = securityIssues.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        check(artifacts, extractedSelectors, type);
    }

    private void check(Collection<Artifact> artifacts, Collection<ArtifactSelector> selectors, String type) {
        String message = "This artifact is not needed in the \""+type+"\" section of the configuration file as it does not exist in the artifacts list";
        for (ArtifactSelector selector : selectors) {
            if (artifacts.stream().noneMatch(selector::matches)) {
                reporter.add(MessageType.UNNECESSARY_CONFIG,
                        selector.toString() + ": " + message);
            }
        }
    }
}
