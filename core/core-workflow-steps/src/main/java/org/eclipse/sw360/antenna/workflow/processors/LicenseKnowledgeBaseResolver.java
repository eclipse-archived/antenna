/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 * Copyright (c) Bosch.IO GmbH 2020.
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
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.knowledgebase.LicenseKnowledgeBaseFactory;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactLicenseInformation;
import org.eclipse.sw360.antenna.model.license.License;
import org.eclipse.sw360.antenna.model.license.LicenseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * This LicenseKnowledgeBaseResolver adds values of the specified
 * CSVBasedLicenseKnowledgeBase to a list of artifacts.
 */
public class LicenseKnowledgeBaseResolver extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseKnowledgeBaseResolver.class);
    private static final String CHOSEN_LICENSE_MANAGER_KEY = "chosen.license.manager";

    private Supplier<List<ILicenseManagementKnowledgeBase>> knowledgeBaseSupplier;
    private ILicenseManagementKnowledgeBase knowledgeBase;
    private String chosenManager;

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
        String licenseId = this.knowledgeBase.getLicenseIdForAlias(license.getId());
        if (licenseId != null) {
            license.setId(licenseId);
        } else {
            licenseId = license.getId();
        }
        setLongName(license, licenseId);
    }

    /**
     * Replaces the longName that belongs to the given licenseId to the license, if
     * it is found in the knowledgeBase.
     */
    private void setLongName(License license, String licenseId) {
        String configuredLongName = license.getCommonName();
        if (StringUtils.isEmpty(configuredLongName)) {
            String longName = this.knowledgeBase.getLicenseNameForId(licenseId);
            if (longName != null) {
                license.setCommonName(longName);
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
            String id = license.getId();
            Optional.ofNullable(knowledgeBase.getTextForId(id))
                .ifPresent(license::setText);
        }
    }


    private void setThreatGroup(License license) {
        Optional<String> threatGroupOfLicense = license.getThreatGroup();
        if (!threatGroupOfLicense.isPresent() || threatGroupOfLicense.get().isEmpty()) {
            String threatGroupOfKb = this.knowledgeBase.getThreatGroupForId(license.getId());
            license.setThreatGroup(threatGroupOfKb);
        }
    }

    private void setClassification(License license) {
        Optional<String> classificationOfLicense = license.getClassification();
        if (!classificationOfLicense.isPresent() || classificationOfLicense.get().isEmpty()) {
            String classificationOfKb = this.knowledgeBase.getClassificationById(license.getId());
            license.setClassification(classificationOfKb);
        }
    }

    public LicenseKnowledgeBaseResolver(ILicenseManagementKnowledgeBase knowledgeBase,
                                        Supplier<List<ILicenseManagementKnowledgeBase>> supplier) {
        this.knowledgeBase = knowledgeBase;
        this.knowledgeBaseSupplier = supplier;
    }

    public ILicenseManagementKnowledgeBase getKnowledgeBase() {
        return this.knowledgeBase;
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
        this.chosenManager = getConfigValue(CHOSEN_LICENSE_MANAGER_KEY, configMap, "");

        if (knowledgeBaseSupplier == null) {
            knowledgeBaseSupplier = new LicenseKnowledgeBaseFactory(context);
        }

        List<ILicenseManagementKnowledgeBase> licenseManagers = knowledgeBaseSupplier.get();

        this.knowledgeBase = Optional.ofNullable(knowledgeBase)
                .orElseGet(() -> licenseManagers.stream()
                        .filter(l -> chosenManager.equals(l.getId()))
                        .findFirst()
                        .orElseGet(() -> licenseManagers.stream()
                                .sorted(Comparator.comparing(ILicenseManagementKnowledgeBase::getPriority).reversed())
                                .findFirst()
                                .orElseThrow(() -> new ConfigurationException("Was not able to find any " +
                                        "implementation for the ILicenseManagementKnowledgeBase interface."))));
    }
}