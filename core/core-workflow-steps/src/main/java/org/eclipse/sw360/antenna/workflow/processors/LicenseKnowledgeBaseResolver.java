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

import org.eclipse.sw360.antenna.api.ILicenseManagementKnowledgeBase;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.knowledgebase.LicenseKnowledgeBaseFactory;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
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
        return new LicenseKnowledgeBaseResolverImpl(knowledgeBase).resolveKnowledgeBase(artifacts);
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