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

package org.eclipse.sw360.antenna.workflow.processors.enricher;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.api.workflow.AbstractProcessor;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.facts.ConfiguredLicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.model.artifact.Artifact;

/**
 * Maps the values of a LicenseDocument to the license attributes of the
 * artifacts list.
 */
public class LicenseResolver extends AbstractProcessor {
    private Map<ArtifactSelector, LicenseInformation> configuredLicenses;
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseResolver.class);

    /**
     * Adds the license information from the license document to the artifacts
     * list.
     *
     * @param artifacts
     *            List of artifacts that shall be enriched with the license
     *            information.
     */
    private void resolveLicenses(Collection<Artifact> artifacts) {
        for (Artifact artifact : artifacts) {
            findConfiguredLicense(artifact, configuredLicenses)
                    .ifPresent(lic -> artifact.addFact(new ConfiguredLicenseInformation(lic)));
        }
    }

    /**
     * If the given Configuration contains information about the final license
     * this information is added to the artifact.
     *
     * @param artifact
     * @param configuration
     * @return
     */

    private Optional<LicenseInformation> findConfiguredLicense(Artifact artifact, Map<ArtifactSelector, LicenseInformation> configuration) {
        for (Entry<ArtifactSelector, LicenseInformation> entry : configuration.entrySet()) {
            ArtifactSelector selector = entry.getKey();
            if (selector.matches(artifact)) {
                return Optional.ofNullable(entry.getValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<Artifact> process(Collection<Artifact> artifacts) {
        LOGGER.info("Resolve licenses...");
        resolveLicenses(artifacts);
        LOGGER.info("Resolve licenses... done");
        return artifacts;
    }

    @Override
    public void configure(Map<String,String> configMap) throws AntennaConfigurationException {
        super.configure(configMap);
        this.configuredLicenses = context.getConfiguration().getFinalLicenses();
    }
}
