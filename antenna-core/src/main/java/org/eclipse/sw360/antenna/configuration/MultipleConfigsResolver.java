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

package org.eclipse.sw360.antenna.configuration;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.Report;
import org.eclipse.sw360.antenna.report.Reporter;

/**
 * Merges a list of configurations if possible. If a conflict occurs it is
 * written to the temporary reporter. With the checkReport() method it can be
 * checked if this reporter contains messages. If yes an Exception is thrown and
 * the conflicts must be solved.
 */
public class MultipleConfigsResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleConfigsResolver.class);

    private ConfigurationReader configurationReader;
    private Path antennaTargetDirectory;
    private Reporter tempReporter;

    /**
     *
     * Merges a list of configurations if possible. If a conflict occurs it is
     * written to the temporary reporter. With the checkReport() method it can
     * be checked if this reporter contains messages. If yes an Exception is
     * thrown and the conflicts must be solved.
     */

    public Configuration resolveConfigs(ToolConfiguration toolConfiguration) throws AntennaConfigurationException {
        return resolveConfigs(toolConfiguration, true);
    }

    protected Configuration resolveConfigs(ToolConfiguration toolConfiguration, boolean checkReport) throws AntennaConfigurationException {
        this.configurationReader = new ConfigurationReader(toolConfiguration.getEncoding());
        this.antennaTargetDirectory = toolConfiguration.getAntennaTargetDirectory();
        this.tempReporter = new Reporter(antennaTargetDirectory, toolConfiguration.getEncoding());

        List<File> configFiles = toolConfiguration.getConfigFiles();
        List<URI> configFileUris = toolConfiguration.getConfigFileUris();

        List<Configuration> configurations = new ArrayList<>();
        if (configFiles != null && !configFiles.isEmpty()) {
            configurations.addAll(resolveConfigs(configFiles));
        }

        if (configFileUris != null && !configFileUris.isEmpty()) {
            configurations.addAll(resolveUris(configFileUris));
        }

        Configuration resolvedConfigs = mergeConfigurations(configurations);
        LOGGER.debug("List of configurations merged.");

        if(checkReport) {
            checkReport();
        }

        return resolvedConfigs;
    }

    private ArrayList<Configuration> resolveConfigs(List<File> configs) throws AntennaConfigurationException {
        LOGGER.debug("Resolve list of configurations.");
        ArrayList<Configuration> configurations = new ArrayList<>();
        for (File config : configs) {
            Configuration configuration = this.configurationReader.readConfigFromFile(config, this.antennaTargetDirectory);
            configurations.add(configuration);
        }
        return configurations;
    }

    /**
     * Check processing reports for conflict messages.
     */
    private void checkReport() throws AntennaConfigurationException {
        Report processingReport = tempReporter.getProcessingReport();
        if (processingReport.getMessageList().size() > 0) {
            tempReporter.writeReport(System.out);
            tempReporter.writeReportToReportPath();

            String msg = "There are conflicting configurations. Please have a look at the processing Report and resolve them.";
            LOGGER.error(msg);
            throw new AntennaConfigurationException(msg);
        }
    }

    private List<Configuration> resolveUris(List<URI> uris) throws AntennaConfigurationException {
        LOGGER.debug("Resolve list of configuration file uris.");
        List<Configuration> configurations = new ArrayList<>();
        for (URI uri : uris) {
            Configuration configuration = this.configurationReader.readConfigFromUri(uri, this.antennaTargetDirectory);
            configurations.add(configuration);

        }
        return configurations;
    }

    private Configuration mergeConfigurations(List<Configuration> configurations) {
        if (configurations.size() == 0) {
            return new Configuration(null);
        }

        Configuration mergedConfig = new Configuration();
        mergeIgnoreForSourceResolving(configurations, mergedConfig);
        mergeOverride(configurations, mergedConfig);
        mergeValidForMissingSources(configurations, mergedConfig);
        mergeValidForIncompleteSources(configurations, mergedConfig);
        mergeRemoveArtifact(configurations, mergedConfig);
        mergeAddArtifact(configurations, mergedConfig);
        mergeFinalLicenses(configurations, mergedConfig);
        mergeFailOnIncompleteSources(configurations, mergedConfig);
        mergeFailOnMissingSources(configurations, mergedConfig);
        mergeSecurityIssues(configurations, mergedConfig);
        mergeSecurityIssueSuppresses(configurations, mergedConfig);
        return mergedConfig;
    }

    private void mergeSecurityIssues(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setSecurityIssues(configurations.stream()
                .map(Configuration::getSecurityIssues)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private void mergeSecurityIssueSuppresses(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setSuppressedSecurityIssues(configurations.stream()
                .map(Configuration::getSuppressedSecurityIssues)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Configuration.suppressedSecurityIssuesConflictResolver)));
    }

    private void mergeAddArtifact(List<Configuration> configurations, Configuration mergedConfig) {
        List<Artifact> mergeAddArtifact = new ArrayList<>();
        for (Configuration configuration : configurations) {
            List<Artifact> addArtifact = configuration.getAddArtifact();
            for (Artifact artifact : addArtifact) {
                if (!mergeAddArtifact.contains(artifact)) {
                    mergeAddArtifact.add(artifact);
                }
            }

        }
        mergedConfig.setAddArtifact(mergeAddArtifact);
    }

    private void mergeFinalLicenses(List<Configuration> configurations, Configuration mergedConfig) {
        Map<ArtifactSelector, LicenseInformation> mergedFinalLicenses = new HashMap<>();
        for (Configuration configuration : configurations) {
            Map<ArtifactSelector, LicenseInformation> setFinalLicense = configuration.getFinalLicenses();
            for (ArtifactSelector selector : setFinalLicense.keySet()) {
                if (!mergedFinalLicenses.containsKey(selector)) {
                    mergedFinalLicenses.put(selector, setFinalLicense.get(selector));
                } else {
                    LicenseInformation merged = mergedFinalLicenses.get(selector);
                    LicenseInformation actual = setFinalLicense.get(selector);
                    if (!merged.equals(actual)) {
                        tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                                "Conflicting configurations in the \"set final license\" section, the declared licenses are not the same. " +
                                        "(artifact selector was=[" + selector.toString() + "])");
                    }
                }
            }
        }
        mergedConfig.setFinalLicenses(mergedFinalLicenses);
    }

    private void mergeFailOnIncompleteSources(List<Configuration> configurations, Configuration mergedConfig) {
        boolean equal = configurations.get(0).isFailOnIncompleteSources();
        for (Configuration configuration : configurations) {

            if (!(equal == configuration.isFailOnIncompleteSources())) {
                tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                        "Conflicting configurations for the attribute \"failOnIncompleteSources\".");
            }
        }
        mergedConfig.setFailOnIncompleteSources(equal);
    }

    private void mergeFailOnMissingSources(List<Configuration> configurations, Configuration mergedConfig) {
        boolean equal = configurations.get(0).isFailOnMissingSources();
        for (Configuration configuration : configurations) {
            if (!(equal == configuration.isFailOnMissingSources())) {
                tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                        "Conflicting configurations for the attribute \"failOnMissingSources\".");
                return;
            }
        }
        mergedConfig.setFailOnMissingSources(equal);
    }

    private void mergeRemoveArtifact(List<Configuration> configurations, Configuration mergedConfig) {
        List<ArtifactSelector> mergedRemove = new ArrayList<>();
        for (Configuration configuration : configurations) {
            List<ArtifactSelector> removeArtifact = configuration.getRemoveArtifact();
            mergedRemove.addAll(removeArtifact);
        }
        mergedConfig.setremoveArtifact(mergedRemove);
    }

    private void mergeValidForIncompleteSources(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setValidForIncompleteSources(configurations.stream()
                .map(Configuration::getValidForIncompleteSources)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void mergeValidForMissingSources(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setValidForMissingSources(configurations.stream()
                .map(Configuration::getValidForMissingSources)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void mergeOverride(List<Configuration> configurations, Configuration mergedConfig) {
        Map<ArtifactSelector, Artifact> mergedOverride = new HashMap<>();
        for (Configuration configuration : configurations) {
            Map<ArtifactSelector, Artifact> override = configuration.getOverride();
            Set<ArtifactSelector> keySet = override.keySet();
            for (ArtifactSelector artifactSelector : keySet) {
                Artifact compare = override.get(artifactSelector);
                if (mergedOverride.containsKey(artifactSelector)) {
                    Artifact generatedArtifact = mergedOverride.get(artifactSelector);
                    if (!generatedArtifact.equals(compare)) {
                        tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                                "Conflicting configurations in the override section at artifact: "
                                        + "the override values are not equal. (artifact selector was=[" + artifactSelector.toString() + "])");
                        return;
                    }
                } else {
                    mergedOverride.put(artifactSelector, compare);
                }
            }
        }
        mergedConfig.setOverride(mergedOverride);
    }

    private void mergeIgnoreForSourceResolving(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setIgnoreForSourceResolving(configurations.stream()
                .map(Configuration::getIgnoreForSourceResolving)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList()));
    }

    public Reporter getReporter() {
        return this.tempReporter;
    }
}
