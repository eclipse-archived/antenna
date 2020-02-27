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

package org.eclipse.sw360.antenna.model;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.FromXmlArtifactBuilder;
import org.eclipse.sw360.antenna.model.xml.generated.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains values of the configuration file.
 */
public class Configuration {
    private List<ArtifactSelector> ignoreForSourceResolving = new ArrayList<>();
    private Map<ArtifactSelector, Artifact> override = new HashMap<>();
    private List<ArtifactSelector> validForMissingSources = new ArrayList<>();
    private List<ArtifactSelector> validForIncompleteSources = new ArrayList<>();
    private List<ArtifactSelector> removeArtifact = new ArrayList<>();
    private List<ArtifactSelector> preferP2 = new ArrayList<>();
    private List<Artifact> addArtifact = new ArrayList<>();
    private Map<ArtifactSelector,Issues> securityIssues = new HashMap<>();
    private Map<String, Map<ArtifactSelector, GregorianCalendar>> suppressedSecurityIssues;
    private Map<ArtifactSelector, LicenseInformation> finalLicenses = new HashMap<>();
    private boolean failOnIncompleteSources = false;
    private boolean failOnMissingSources = false;

    public final static BinaryOperator<Map<ArtifactSelector,GregorianCalendar>> suppressedSecurityIssuesConflictResolver =
            (suppressMap1,suppressMap2) -> Stream.concat(
                    suppressMap1.entrySet().stream(),
                    suppressMap2.entrySet().stream())
                    .collect(Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue,
                            (date1, date2) -> date1.compareTo(date2) < 0 ? date1 : date2));

    public Configuration() {
    }

    /**
     * Creates a Configuration object with the values of the given AntennaConfig
     * object.
     *
     * @param antennaConfig
     *            AntennaConfig from which the values are used
     */
    public Configuration(final AntennaConfig antennaConfig) {
        if (antennaConfig != null) {
            setRemoveArtifact(createListOfArtifactSelector(antennaConfig.getRemoveArtifact().getArtifactSelector()));
            createHandleAsValid(antennaConfig.getSourceValidation());
            setOverride(createOverride(antennaConfig.getOverrides().getOverride()));
            setFinalLicenses(createFinalLicenses(antennaConfig.getSetFinalLicenses().getSetFinalLicense()));
            if (antennaConfig.getSourceResolving().getIgnoreForSourceResolving() != null) {
                setIgnoreForSourceResolving(createListOfArtifactSelector(
                        antennaConfig.getSourceResolving().getIgnoreForSourceResolving().getArtifactSelector()));
            }
            setFailOnIncompleteSources(antennaConfig.getSourceValidation().isFailOnIncompleteSources());
            setFailOnMissingSources(antennaConfig.getSourceValidation().isFailOnMissingSources());
            if (antennaConfig.getSourceResolving().getPreferP2() != null) {
                setPrefereP2(createListOfArtifactSelector(
                        antennaConfig.getSourceResolving().getPreferP2().getArtifactSelector()));
            }
            setAddArtifact(antennaConfig.getAddArtifact().getArtifact().stream()
                    .map(FromXmlArtifactBuilder::build)
                    .collect(Collectors.toList()));
            setSecurityIssuesByList(antennaConfig.getSecurityIssues().getSecurityIssue());
            setSecurityIssueSuppressesByList(antennaConfig.getSecurityIssues().getSuppress());
        }
    }

    private List<ArtifactSelector> createListOfArtifactSelector(final List<FromXmlArtifactSelectorBuilder> generatedSelectors) {
        return generatedSelectors.stream()
                .map(FromXmlArtifactSelectorBuilder::build)
                .collect(Collectors.toList());
    }

    private void createHandleAsValid(final SourceValidation sourceValidation) {
        final List<IgnoreForSourceValidation> list = sourceValidation.getHandleSourceAsValid();
        for (final IgnoreForSourceValidation ignore : list) {
            if (ignore.isIncompleteSources()) {
                validForIncompleteSources.add(ignore.getArtifactSelector().build());
            }
            if (ignore.isMissingSources()) {
                validForMissingSources.add(ignore.getArtifactSelector().build());
            }
        }
    }

    private Map<ArtifactSelector, LicenseInformation> createFinalLicenses(final List<SetFinalLicense> setFinalLicenses) {
        final Map<ArtifactSelector, LicenseInformation> finalLicenses = new HashMap<>();
        for(SetFinalLicense entry: setFinalLicenses) {
            finalLicenses.put(entry.getArtifactSelector().build(), entry.getLicenseInfo().getValue());
        }


        return finalLicenses;
    }

    /**
     * Creates a map which contains all values that will be overridden.
     *
     * @param overrideList
     * @return map which contains all values that will be overridden
     */
    private Map<ArtifactSelector, Artifact> createOverride(final List<AttributeOverride> overrideList) {
        final Map<ArtifactSelector, Artifact> overrideMap = new HashMap<>();
        for (final AttributeOverride override : overrideList) {
            overrideMap.put(override.getArtifactSelector().build(),
                    override.getOverrideValue().getArtifact().build());
        }
        return overrideMap;
    }

    public List<ArtifactSelector> getIgnoreForSourceResolving() {
        return ignoreForSourceResolving;
    }

    /**
     * Sets ignore for Source resolving and adds list to validForMissingSources.
     *
     * @param ignoreForSourceResolving
     *            Artifacts list to be resolved.
     */
    public void setIgnoreForSourceResolving(final List<ArtifactSelector> ignoreForSourceResolving) {
        this.ignoreForSourceResolving = ignoreForSourceResolving;
        for (final ArtifactSelector artifactSelector : ignoreForSourceResolving) {
            getValidForMissingSources().add(artifactSelector);
            getValidForIncompleteSources().add(artifactSelector);
        }
    }

    public Map<ArtifactSelector, Artifact> getOverride() {
        return override;
    }

    public void setOverride(final Map<ArtifactSelector, Artifact> override) {
        this.override = override;
    }

    public List<ArtifactSelector> getRemoveArtifact() {
        return removeArtifact;
    }

    public void setRemoveArtifact(final List<ArtifactSelector> list) {
        removeArtifact = list;
    }

    public List<ArtifactSelector> getValidForMissingSources() {
        return validForMissingSources;
    }

    public void setValidForMissingSources(final List<ArtifactSelector> validForMissingSources) {
        this.validForMissingSources = validForMissingSources;
    }

    public List<ArtifactSelector> getValidForIncompleteSources() {
        return validForIncompleteSources;
    }

    public void setValidForIncompleteSources(final List<ArtifactSelector> validForIncompleteSources) {
        this.validForIncompleteSources = validForIncompleteSources;
    }

    public Map<ArtifactSelector, LicenseInformation> getFinalLicenses() {
        return finalLicenses;
    }

    public void setFinalLicenses(final Map<ArtifactSelector, LicenseInformation> finalLicenses) {
        this.finalLicenses = finalLicenses;
    }

    public boolean isFailOnIncompleteSources() {
        return failOnIncompleteSources;
    }

    public void setFailOnIncompleteSources(final boolean failOnIncompleteSources) {
        this.failOnIncompleteSources = failOnIncompleteSources;
    }

    public boolean isFailOnMissingSources() {
        return failOnMissingSources;
    }

    public void setFailOnMissingSources(final boolean failOnMissingSources) {
        this.failOnMissingSources = failOnMissingSources;
    }

    public List<ArtifactSelector> getPrefereP2() {
        return preferP2;
    }

    public void setPrefereP2(final List<ArtifactSelector> prefereP2) {
        this.preferP2 = prefereP2;
    }

    public List<Artifact> getAddArtifact() {
        return addArtifact;
    }

    public void setAddArtifact(final List<Artifact> addArtifact) {
        this.addArtifact = addArtifact;
    }

    public Map<ArtifactSelector, Issues> getSecurityIssues() { return securityIssues; }

    public void setSecurityIssues(Map<ArtifactSelector, Issues> securityIssues) {
        this.securityIssues = securityIssues;
    }

    private void setSecurityIssuesByList(List<SecurityIssue> securityIssues) {
        this.securityIssues = securityIssues.stream()
                .collect(Collectors.toMap(securityIssue -> securityIssue.getArtifactSelector().build(),
                        SecurityIssue::getIssues));
    }

    public Map<String, Map<ArtifactSelector, GregorianCalendar>> getSuppressedSecurityIssues() {
        return suppressedSecurityIssues;
    }

    public void setSuppressedSecurityIssues(Map<String, Map<ArtifactSelector, GregorianCalendar>> suppressedSecurityIssues) {
        this.suppressedSecurityIssues = suppressedSecurityIssues;
    }

    private void setSecurityIssueSuppressesByList(List<Suppress> suppresses) {
        Function<Suppress,String> keyMapper = Suppress::getReference;

        Function<Suppress,Map<ArtifactSelector, GregorianCalendar>> valueMapper =
                suppress -> Collections.singletonMap(suppress.getArtifactSelector().build(),
                        suppress.getUntil().toGregorianCalendar());

        this.suppressedSecurityIssues = suppresses.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper, suppressedSecurityIssuesConflictResolver));
    }
}
