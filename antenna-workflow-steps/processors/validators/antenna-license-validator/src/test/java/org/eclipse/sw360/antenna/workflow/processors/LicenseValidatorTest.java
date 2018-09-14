/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.processors;

import static org.eclipse.sw360.antenna.workflow.processors.LicenseValidator.*;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.exceptions.AntennaConfigurationException;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.eclipse.sw360.antenna.model.Artifact;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.report.Reporter;
import org.junit.rules.TemporaryFolder;

public class LicenseValidatorTest extends AntennaTestWithMockedContext {
    private License allowedLicense;
    private License forbiddenLicense;
    private License emptyTextLicense;
    private License emptyLicense;
    private LicenseValidator validator;
    private Map<String,String> configMap;

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Before
    public void init() {

        allowedLicense = new License();
        allowedLicense.setName("allowedLicense");
        allowedLicense.setText("text of allowedLicense");

        forbiddenLicense = new License();
        forbiddenLicense.setName("forbiddenLicense");
        forbiddenLicense.setText("text of forbiddenLicense");

        emptyTextLicense = new License();
        emptyTextLicense.setName("licenseWithoutText");

        emptyLicense = new License();

        validator = new LicenseValidator();
        validator.setAntennaContext(antennaContextMock);

        configMap = new HashMap<>();
        configMap.put(FORBIDDEN_LICENSES_KEY, forbiddenLicense.getName());
    }

    public Artifact mkArtifact(License decladerLicense) {
        Artifact artifact = new Artifact();
        artifact.setDeclaredLicenses(decladerLicense);
        artifact.setConfiguredLicense(emptyLicense);
        artifact.setObservedLicenses(emptyLicense);
        artifact.setOverriddenLicenses(emptyLicense);
        return artifact;
    }

    @Test
    public void validLicense() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(allowedLicense);

        validator.configure(Collections.emptyMap());
        assertThat(validator.validate(artifact).size()).isEqualTo(0);
    }

    @Test
    public void validLicenseWithConfig() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(allowedLicense);

        configMap.put(FORBIDDEN_LICENSE_SEVERITY_KEY, "FAIL");
        configMap.put(MISSING_LICENSE_INFORMATION_SEVERITY_KEY, "FAIL");
        configMap.put(MISSING_LICENSE_TEXT_SEVERITY_KEY, "FAIL");

        validator.configure(configMap);
        assertThat(validator.validate(artifact).size()).isEqualTo(0);
    }

    @Test
    public void testForbiddenLicenseFail() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(forbiddenLicense);

        configMap.put(FORBIDDEN_LICENSE_SEVERITY_KEY, "FAIL");

        validator.configure(configMap);
        final List<IEvaluationResult> validate = validator.validate(artifact);

        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.stream()
                .map(IEvaluationResult::getSeverity)
                .allMatch(IEvaluationResult.Severity.FAIL::equals));
    }

    @Test
    public void testForbiddenLicenseWarn() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(forbiddenLicense);

        configMap.put(FORBIDDEN_LICENSE_SEVERITY_KEY, "WARN");

        validator.configure(configMap);
        final List<IEvaluationResult> validate = validator.validate(artifact);

        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.stream()
                .map(IEvaluationResult::getSeverity)
                .allMatch(IEvaluationResult.Severity.WARN::equals));
    }

    @Test
    public void testLicenseWithoutTextTestFail() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(emptyTextLicense);

        configMap.put(MISSING_LICENSE_TEXT_SEVERITY_KEY, "WARN");

        validator.configure(configMap);
        final List<IEvaluationResult> validate = validator.validate(artifact);

        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.stream()
                .map(IEvaluationResult::getSeverity)
                .allMatch(IEvaluationResult.Severity.FAIL::equals));
    }

    @Test
    public void testLicenseWithoutTextTestWarn() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(emptyTextLicense);

        configMap.put(MISSING_LICENSE_TEXT_SEVERITY_KEY, "WARN");

        validator.configure(configMap);
        final List<IEvaluationResult> validate = validator.validate(artifact);

        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.stream()
                .map(IEvaluationResult::getSeverity)
                .allMatch(IEvaluationResult.Severity.WARN::equals));
    }

    @Test
    public void testLicenseWithoutLicenseInformationTestFail() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(emptyLicense);

        configMap.put(MISSING_LICENSE_INFORMATION_SEVERITY_KEY, "FAIL");

        validator.configure(configMap);
        final List<IEvaluationResult> validate = validator.validate(artifact);

        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.stream()
                .map(IEvaluationResult::getSeverity)
                .allMatch(IEvaluationResult.Severity.FAIL::equals));
    }

    @Test
    public void testLicenseWithoutLicenseInformationTestWarn() throws AntennaConfigurationException {
        Artifact artifact = mkArtifact(emptyLicense);

        configMap.put(MISSING_LICENSE_INFORMATION_SEVERITY_KEY, "WARN");

        validator.configure(configMap);
        final List<IEvaluationResult> validate = validator.validate(artifact);

        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.stream()
                .map(IEvaluationResult::getSeverity)
                .allMatch(IEvaluationResult.Severity.FAIL::equals));
    }
}
