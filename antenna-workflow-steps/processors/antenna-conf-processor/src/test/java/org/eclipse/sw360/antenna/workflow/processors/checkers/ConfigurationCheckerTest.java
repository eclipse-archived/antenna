/*
 * Copyright (c) Bosch Software Innovations GmbH 2014,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors.checkers;

import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorAndSet;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.java.MavenCoordinates;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.ProcessingMessage;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.report.Reporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationCheckerTest {

    private TemporaryFolder folder;
    private Artifact artifact;
    private ArtifactSelector selector;
    private List<Artifact> artifacts;
    private Configuration config;
    private Reporter reporter;

    @Before
    public void init() throws IOException {
        folder = new TemporaryFolder();
        folder.create();
        File file = folder.newFile();
        artifact = new Artifact();

        artifact.addFact(new ArtifactFilename("artifact"));
        artifact.addFact(new MavenCoordinates("artifact", null, null));

        selector = new ArtifactSelectorAndSet(
                new ArtifactFilename("artifact"),
                new MavenCoordinates("artifact", null, null)
        );

        config = new Configuration(null);
        artifacts = new ArrayList<>();
        reporter = new Reporter(file.toPath());
    }

    @Test
    public void testUnneccesaryRemoveArtifact() {
        List<ArtifactSelector> list = new ArrayList<>();
        list.add(selector);
        config.setremoveArtifact(list);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        checker.process(artifacts);
        List<ProcessingMessage> messageList = reporter.getProcessingReport().getMessageList();
        assertThat(messageList).isNotEmpty();
        assertThat(messageList.get(0).getMessageType()).isEqualTo(MessageType.UNNECESSARY_CONFIG);
    }

    @Test
    public void testNeccesaryRemoveArtifact() throws IOException {
        List<ArtifactSelector> list = new ArrayList<>();
        list.add(selector);
        artifacts.add(artifact);
        config.setremoveArtifact(list);
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        checker.process(artifacts);
        List<ProcessingMessage> messageList = reporter.getProcessingReport().getMessageList();
        assertThat(messageList).isEmpty();

    }

    @Test
    public void testUnnecessaryOverride() {
        Map<ArtifactSelector, Artifact> override = new HashMap<>();
        override.put(selector, null);
        config.setOverride(override);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isNotEmpty();
    }

    @Test
    public void testNecessaryOverride() {
        Map<ArtifactSelector, Artifact> override = new HashMap<>();
        override.put(selector, null);
        config.setOverride(override);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        artifacts.add(this.artifact);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isEmpty();
    }

    @Test
    public void testUnnecessaryIgnoreForSourceResolving() {
        List<ArtifactSelector> ignoreForSourceResolving = new ArrayList<>();
        ignoreForSourceResolving.add(selector);
        config.setIgnoreForSourceResolving(ignoreForSourceResolving);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isNotEmpty();
    }

    @Test
    public void testNecessarySetFinalLicense() {
        Map<ArtifactSelector, LicenseInformation> finalLicenses = new HashMap<>();
        finalLicenses.put(selector, null);
        config.setFinalLicenses(finalLicenses);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        artifacts.add(this.artifact);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isEmpty();
    }

    @Test
    public void testUnnecessarySetFinalLicense() {
        Map<ArtifactSelector, LicenseInformation> finalLicenses = new HashMap<>();
        finalLicenses.put(selector, null);
        config.setFinalLicenses(finalLicenses);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isNotEmpty();
    }

    @Test
    public void testNecessaryIgnoreForSourceResolving() {
        List<ArtifactSelector> ignoreForSourceResolving = new ArrayList<>();
        ignoreForSourceResolving.add(selector);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        artifacts.add(this.artifact);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isEmpty();
    }

    @Test
    public void testUnnecessaryLicenseValidation() {
        List<ArtifactSelector> licenseValidation = new ArrayList<>();
        licenseValidation.add(selector);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        artifacts.add(this.artifact);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isEmpty();
    }

    @Test
    public void testNecessaryLicenseValidation() {
        List<ArtifactSelector> licenseValidation = new ArrayList<>();
        licenseValidation.add(selector);
        ConfigurationChecker checker = new ConfigurationChecker(reporter, config);
        artifacts.add(this.artifact);
        checker.process(artifacts);
        assertThat(reporter.getProcessingReport().getMessageList()).isEmpty();
    }

    @After
    public void delete() {
        this.folder.delete();
    }
}
