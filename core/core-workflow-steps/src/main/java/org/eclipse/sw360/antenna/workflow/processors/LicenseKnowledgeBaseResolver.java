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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.knowledgebase.LicenseKnowledgeBaseFactory;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactLicenseInformation;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This LicenseKnowledgeBaseResolver adds values of the specified
 * CSVBasedLicenseKnowledgeBase to a list of artifacts.
 */
public class LicenseKnowledgeBaseResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseKnowledgeBaseResolver.class);
    private ILicenseManagementKnowledgeBase knowledgeBase;

    public LicenseKnowledgeBaseResolver() {
        this.workflowStepOrder = 9000;
    }

    public LicenseKnowledgeBaseResolver(ILicenseManagementKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    /**
     * Adds a license Identifier, a license long name and the text to the
     * licenses of the artifact list.
     *
     * @param artifacts
     *            List of artifacts which will be resolved
     */
    private void resolveKnowledgeBase(Collection<Artifact> artifacts) {
        artifacts.stream()
                .map(artifact -> artifact.askForAll(ArtifactLicenseInformation.class))
                .flatMap(List::stream)
                .map(ArtifactLicenseInformation::get)
                .map(LicenseInformation::getLicenses)
                .flatMap(List::stream)
                .forEach(license -> {
                    aliasToIdentifier(license);
                    setText(license);
                    setThreatGroup(license);
                    setClassification(license);
                });
    }

    /**
     * Replaces the aliases with the license Identifier.
     * If it is found in the licenseKnowledgeBase.
     */
    private void aliasToIdentifier(License license) {
        String licenseId = this.knowledgeBase.getLicenseIdForAlias(license.getName());
        if (licenseId != null) {
            license.setName(licenseId);
        } else {
            licenseId = license.getName();
        }
        setLongName(license, licenseId);
    }

    /**
     * Replaces the longName that belongs to the given licenseId to the license, if
     * it is found in the knowledgeBase.
     */
    private void setLongName(License license, String licenseId) {
        String configuredLongName = license.getLongName();
        if (StringUtils.isEmpty(configuredLongName)) {
            String longName = this.knowledgeBase.getLicenseNameForId(licenseId);
            if (longName != null) {
                license.setLongName(longName);
            }            
        }
    }

    /**
     * Sets text in License of artifact and extends idTextMap in
     * LicenseKnowledgeBase.
     *
     * @param license
     *            List of licenses for which the text will be set
     */
    private void setText(License license) {
        String configuredText = license.getText();
        if (StringUtils.isEmpty(configuredText)) {
            String id = license.getName();
            Optional.ofNullable(knowledgeBase.getTextForId(id))
                .ifPresent(license::setText);
        }
    }


    private void setThreatGroup(License license) {
        Optional<String> threatGroupOfLicense = license.getThreatGroup();
        if (!threatGroupOfLicense.isPresent() || threatGroupOfLicense.get().isEmpty()) {
            String threatGroupOfKb = this.knowledgeBase.getThreatGroupForId(license.getName());
            license.setThreatGroup(threatGroupOfKb);
        }
    }

    private void setClassification(License license) {
        Optional<String> classificationOfLicense = license.getClassification();
        if (!classificationOfLicense.isPresent() || classificationOfLicense.get().isEmpty()) {
            String classificationOfKb = this.knowledgeBase.getClassificationById(license.getName());
            license.setClassification(classificationOfKb);
        }
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.debug("Resolve knowledge base...");
        resolveKnowledgeBase(artifacts);
        LOGGER.debug("Resolve knowledge base... done");
        return artifacts;
    }

    @Override
    public void configure(Map<String,String> configMap) {
        super.configure(configMap);
        this.knowledgeBase = new LicenseKnowledgeBaseFactory(context).get();
    }
}