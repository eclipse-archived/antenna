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

package org.eclipse.sw360.antenna.validators.workflow.processors;

import org.eclipse.sw360.antenna.api.IEvaluationResult;
import org.eclipse.sw360.antenna.api.IPolicyEvaluation;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.util.ArtifactLicenseUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.workflow.stubs.AbstractComplianceChecker;
import org.eclipse.sw360.antenna.workflow.stubs.DefaultPolicyEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The Licenses validator can check if the licenses of a list of artifacts is
 * valid or not. A license is declared as invalid if:
 *
 * <pre>
 *  - is it has no license text and failOnMissingLicenseText = true
 *  - it does not pass the forbiddenLicenseFilter and failOnForbiddenLicenses = true
 *  - it has no licenseInformation and failOnMissingLicenseInformation = true
 * </pre>
 */
public class LicenseValidator extends AbstractComplianceChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseValidator.class);
    public static final String FORBIDDEN_LICENSE_SEVERITY_KEY = "forbiddenLicenseSeverity";
    public static final String MISSING_LICENSE_INFORMATION_SEVERITY_KEY = "missingLicenseInformationSeverity";
    public static final String MISSING_LICENSE_TEXT_SEVERITY_KEY = "missingLicenseTextSeverity";
    public static final String FORBIDDEN_LICENSES_KEY = "forbiddenLicenses";
    public static final String IGNORED_LICENSES_KEY = "ignoredLicenses";
    private IEvaluationResult.Severity missingLicenseInformationSeverity = IEvaluationResult.Severity.WARN;
    private IEvaluationResult.Severity missingLicenseTextSeverity = IEvaluationResult.Severity.WARN;
    private IEvaluationResult.Severity forbiddenLicenseSeverity = IEvaluationResult.Severity.FAIL;
    private List<String> forbiddenLicenseIds;
    private List<String> ignoredLicenseIds;

    public LicenseValidator() {
        this.workflowStepOrder = VALIDATOR_BASE_ORDER + 200;
    }

    /**
     * Validates the licenses of the given list of artifacts. If a license is
     * not valid a ProcessingMessage is added to the reporter. If a license is
     * valid or not depends on the configuration of the class
     * {@link LicenseValidator}.
     *
     * @return True if all licenses are declared as valid.
     */
    public List<IEvaluationResult> validate(Artifact artifact) {
        List<IEvaluationResult> results = new ArrayList<>();
        if (! artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY)) {
            LicenseInformation finalLicenses = ArtifactLicenseUtils.getFinalLicenses(artifact);
            if (finalLicenses.isEmpty()) {
                results.add(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                        "LicenseValidator::noLicense", "No License information found for the artifact.", missingLicenseInformationSeverity, artifact));
                return results;
            }
            for (License license : finalLicenses.getLicenses()) {
                if(ignoredLicenseIds.contains(license.getName())){
                    LOGGER.debug("Do not validate license=[" + license.getName() + "], since it is ignored for validation");
                    continue;
                }
                if (forbiddenLicenseIds.contains(license.getName())) {
                    results.add(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                            "LicenseValidator::forbiddenLicense",
                            artifact
                            + " is licensed under the forbidden license " + license.getName(),
                            forbiddenLicenseSeverity, artifact));
                }
                if (license.getText() == null || "".equals(license.getText())) {
                    results.add(new DefaultPolicyEvaluation.DefaultEvaluationResult(
                            "LicenseValidator::noLicenseText",
                            "License contains no Text", missingLicenseTextSeverity, artifact));
                }
            }
        }
        return results;
    }

    @Override
    public IPolicyEvaluation evaluate(Collection<Artifact> artifacts) {
        DefaultPolicyEvaluation policyEvaluation = new DefaultPolicyEvaluation();

        artifacts.stream()
                .filter(artifact -> !artifact.getFlag(Artifact.IS_PROPRIETARY_FLAG_KEY))
                .forEach(artifact -> validate(artifact)
                        .forEach(policyEvaluation::addEvaluationResult));

        return policyEvaluation;
    }

    @Override
    public String getRulesetDescription() {
        return "License Validator";
    }

    @Override
    public void configure(Map<String, String> configMap) throws ConfigurationException {
        super.configure(configMap);

        this.reporter = context.getProcessingReporter();

        forbiddenLicenseIds = getCommaSeparatedConfigValue(FORBIDDEN_LICENSES_KEY, configMap);
        ignoredLicenseIds = getCommaSeparatedConfigValue(IGNORED_LICENSES_KEY, configMap);

        forbiddenLicenseSeverity = getSeverityFromConfig(FORBIDDEN_LICENSE_SEVERITY_KEY, configMap, forbiddenLicenseSeverity);
        missingLicenseInformationSeverity = getSeverityFromConfig(MISSING_LICENSE_INFORMATION_SEVERITY_KEY, configMap, missingLicenseInformationSeverity);
        missingLicenseTextSeverity = getSeverityFromConfig(MISSING_LICENSE_TEXT_SEVERITY_KEY, configMap, missingLicenseTextSeverity);
    }
}
